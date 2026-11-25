package ma.campuslab.reservationsservice.services;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import ma.campuslab.reservationsservice.config.RabbitConfig;
import ma.campuslab.reservationsservice.entities.Reservation;
import ma.campuslab.reservationsservice.entities.ReservationStatut;
import ma.campuslab.reservationsservice.exceptions.UsagerNonAdmissibleException;
import ma.campuslab.reservationsservice.exceptions.UsagerObligatoireException;
import ma.campuslab.reservationsservice.grpc.UsagerGrpcClient;
import ma.campuslab.reservationsservice.messaging.MessageEnvelope;
import ma.campuslab.reservationsservice.messaging.MessagePublisher;
import ma.campuslab.reservationsservice.repositories.IReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class ReservationService implements IReservationService {

    private final IReservationRepository reservationRepository;
    private final MessagePublisher messagePublisher;
    private final UsagerGrpcClient usagerGrpcClient;

    public ReservationService(IReservationRepository reservationRepository, MessagePublisher messagePublisher, UsagerGrpcClient usagerGrpcClient) {
        this.reservationRepository = reservationRepository;
        this.messagePublisher = messagePublisher;
        this.usagerGrpcClient = usagerGrpcClient;
    }

    public Reservation findReservationById(String idReservation) {
        return reservationRepository.findById(idReservation).orElseThrow();
    }

    public String deleteAllReservations() {
        reservationRepository.deleteAll();
        return "Toutes les réservations ont été supprimées. Count : " + reservationRepository.count();
    }

    @Transactional
    @Synchronized
    public Reservation addReservation(Reservation reservation) {

        if (reservation.getIdReservation() == null || reservation.getIdReservation().isBlank()) {
            reservation.setIdReservation(genererProchainIdReservation());
        }

        if (reservation.getIdUsager() == null || reservation.getIdUsager().isBlank()) {
            throw new UsagerObligatoireException("L'identifiant de l'usager est obligatoire", new NullPointerException());
        }

        UsagerGrpcClient.Admissibilite admissibilite = usagerGrpcClient.verifier(reservation.getIdUsager());
        if (!admissibilite.admissible()) {
            throw new UsagerNonAdmissibleException(admissibilite.motif(), "L'identifiant de l'usager est " + admissibilite.motif());
        }

        if (reservation.getQuantite() <= 0) {
            throw new IllegalArgumentException("La quantité est obligatoire et doit être positive");
        }

        if (reservation.getIdMateriel() == null || reservation.getIdMateriel().isBlank()) {
            throw new IllegalArgumentException("L'identifiant du matériel est obligatoire");
        }

        reservation.setStatut(ReservationStatut.EN_ATTENTE);
        reservation.setVersionEtat(1); // 1 -> 2

        Reservation saved = reservationRepository.save(reservation);
        log.info("CREATION idReservation={} statut={} versionEtat={}",
                saved.getIdReservation(), saved.getStatut(), saved.getVersionEtat());

        messagePublisher.publierApresCommit(RabbitConfig.EX_COMMANDES, RabbitConfig.RK_RESERVER,
                MessageEnvelope.of(
                        RabbitConfig.T_RESERVER_MATERIEL, saved.getIdReservation(),
                        Map.of(
                                "idMateriel", saved.getIdMateriel(),
                                "quantite", saved.getQuantite()
                        )
                )
        );

        publierEtat(saved);
        return saved;
    }

    @Transactional
    public Reservation annulerReservation(String idReservation) {
        Reservation reservation = reservationRepository.findById(idReservation).orElseThrow();
        if (reservation.getStatut() == ReservationStatut.CONFIRMEE) {
            reservation.setStatut(ReservationStatut.ANNULATION_EN_COURS);
            reservation.setVersionEtat(reservation.getVersionEtat() + 1); // 2 -> 3
            Reservation saved = reservationRepository.save(reservation);

            log.info("CREATION idReservation={} statut={} versionEtat={}",
                    saved.getIdReservation(), saved.getStatut(), saved.getVersionEtat());

            messagePublisher.publierApresCommit(RabbitConfig.EX_COMMANDES, RabbitConfig.RK_LIBERER,
                    MessageEnvelope.of(
                            RabbitConfig.T_LIBERER_RESERVATION, saved.getIdReservation(),
                            Map.of(
                                    "idMateriel", saved.getIdMateriel(),
                                    "quantite", saved.getQuantite()
                            )
                    )
            );

            publierEtat(saved);
            return saved;
        }
        throw new IllegalArgumentException("Impossible d'annuler la réservation");
    }

    @Transactional
    public Reservation appliquerResultat(MessageEnvelope message) {
        Reservation reservation = reservationRepository.findById(message.idReservation()).orElseThrow();

        boolean modifie = false;

        switch (message.type()) {
            case RabbitConfig.T_MATERIEL_RESERVE -> {
                if (reservation.getStatut() == ReservationStatut.EN_ATTENTE) {
                    reservation.setStatut(ReservationStatut.CONFIRMEE);
                    reservation.setVersionEtat(reservation.getVersionEtat() + 1); // 1 -> 2
                    modifie = true;
                }
            }
            case RabbitConfig.T_RESERVATION_REFUSEE -> {
                if (reservation.getStatut() == ReservationStatut.EN_ATTENTE) {
                    reservation.setStatut(ReservationStatut.REFUSEE);
                    reservation.setVersionEtat(reservation.getVersionEtat() + 1); // 1 -> 2
                    reservation.setMotif((String) message.donnees().get("motif"));
                    modifie = true;
                }
            }
            case RabbitConfig.T_RESERVATION_LIBEREE -> {
                if (reservation.getStatut() == ReservationStatut.ANNULATION_EN_COURS) {
                    reservation.setStatut(ReservationStatut.ANNULEE);
                    reservation.setVersionEtat(reservation.getVersionEtat() + 1); // 3 -> 4
                    modifie = true;
                }
            }
            default -> log.warn("Type de résultat ignoré : {}", message.type());
        }
        if (!modifie) {
            log.info("Résultat ignoré (déjà traité ou transition non applicable) idMessage={} idReservation={} statut={}",
                    message.idMessage(), reservation.getIdReservation(), reservation.getStatut());
            return reservation;
        }

        Reservation saved = reservationRepository.save(reservation);
        publierEtat(saved);
        return saved;
    }

    private void publierEtat(Reservation r) {
        messagePublisher.publierApresCommit(RabbitConfig.EX_EVENEMENTS, "",
                MessageEnvelope.of(RabbitConfig.T_ETAT_MODIFIE, r.getIdReservation(),
                        Map.of("idUsager", r.getIdUsager(),
                                "idMateriel", r.getIdMateriel(),
                                "quantite", r.getQuantite(),
                                "statut", r.getStatut().name(),
                                "versionEtat", r.getVersionEtat(),
                                "motif", r.getMotif() == null ? "" : r.getMotif())));
    }

    private String genererProchainIdReservation() {
        int maxNum = reservationRepository.findAll().stream()
                .map(Reservation::getIdReservation)
                .filter(Objects::nonNull)
                .mapToInt(this::extraireNumero)
                .max()
                .orElse(0);

        return String.format("RES-%03d", maxNum + 1);
    }

    private int extraireNumero(String id) {
        String chiffres = id.replaceAll("\\D+", "");
        return chiffres.isEmpty() ? 0 : Integer.parseInt(chiffres);
    }
}
