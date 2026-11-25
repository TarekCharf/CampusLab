package ma.campuslab.inventaireservice.services;

import lombok.extern.slf4j.Slf4j;
import ma.campuslab.inventaireservice.config.RabbitConfig;
import ma.campuslab.inventaireservice.entities.Allocation;
import ma.campuslab.inventaireservice.entities.Decision;
import ma.campuslab.inventaireservice.entities.Materiel;
import ma.campuslab.inventaireservice.entities.MotifRejet;
import ma.campuslab.inventaireservice.messaging.MessageEnvelope;
import ma.campuslab.inventaireservice.repositories.IAllocationRepository;
import ma.campuslab.inventaireservice.repositories.IDecisionRepository;
import ma.campuslab.inventaireservice.repositories.IMaterielRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class MaterielService implements IMaterielService{

    private final IMaterielRepository materielRepository;
    private final IAllocationRepository allocationRepository;
    private final IDecisionRepository decisionRepository;

    public MaterielService(IMaterielRepository materielRepository,
                           IAllocationRepository allocationRepository,
                           IDecisionRepository decisionRepository) {
        this.materielRepository = materielRepository;
        this.allocationRepository = allocationRepository;
        this.decisionRepository = decisionRepository;
    }
    public Optional<Materiel> findMaterielById(String idMateriel) {
        return materielRepository.findById(idMateriel);
    }

    public String resetAllMateriel() {
        materielRepository.deleteAll();
        decisionRepository.deleteAll();
        allocationRepository.deleteAll();
        materielRepository.saveAll(List.of(
                new Materiel("M01", "Vidéoprojecteur", 3),
                new Materiel("M02", "Microphone", 1),
                new Materiel("M03", "Caméra", 0)));
        return "Tous les matériels ont été réinitialisés. Count : " + materielRepository.count();
    }

    public List<Materiel> findAllMateriel() {
        return materielRepository.findAll();
    }

    @Transactional
    public Optional<MessageEnvelope> traiterReservation(MessageEnvelope cmd) {
        String idReservation = cmd.idReservation();

        // 1) Demande déjà traitée ? On réutilise la décision : AUCUN nouvel effet sur le disponible.
        //    (couvre aussi un vieux message rejoué après une annulation : pas de nouvelle allocation)
        Optional<Decision> existante =
                decisionRepository.findByIdReservationAndAction(idReservation, Decision.Action.RESERVER);
        if (existante.isPresent()) {
            log.info("DOUBLON ignoré type={} idMessage={} idReservation={} : décision déjà connue ({})",
                    cmd.type(), cmd.idMessage(), idReservation, existante.get().getIssue());
            return Optional.of(versMessage(existante.get()));
        }

        // 2) Vérification du matériel et de la quantité demandée
        String idMateriel = cmd.donnees().get("idMateriel").toString();
        Materiel materiel = materielRepository.findById(idMateriel).orElse(null);
        if (materiel == null) {
            return Optional.of(MessageEnvelope.of(
                    RabbitConfig.T_RESERVATION_REFUSEE, cmd.idReservation(),
                    Map.of(
                            "idMateriel", cmd.donnees().get("idMateriel"),
                            "motif", MotifRejet.MATERIEL_INCONNU
                    )
            ));
        }

        int quantite = ((Number)cmd.donnees().get("quantite")).intValue();
        if (materiel.getDisponible() < quantite) {
            return Optional.of(MessageEnvelope.of(
                    RabbitConfig.T_RESERVATION_REFUSEE, cmd.idReservation(),
                    Map.of(
                            "idMateriel", materiel.getIdMateriel(),
                            "quantite", quantite,
                            "motif", MotifRejet.STOCK_INSUFFISANT
                    )
            ));
        }

        // 2) Décision métier
        Decision decision;
        if (!materielRepository.existsById(idMateriel)) {
            decision = new Decision(idReservation, Decision.Action.RESERVER, Decision.Issue.REFUS, "MATERIEL_INCONNU");
        } else if (materielRepository.reserverSiDisponible(idMateriel, quantite) == 1) {
            // UPDATE atomique « ... WHERE disponible >= q » : vérification + décrément en une instruction
            allocationRepository.save(new Allocation(idReservation, idMateriel, quantite));
            decision = new Decision(idReservation, Decision.Action.RESERVER, Decision.Issue.ACCEPTE, null);
        } else {
            decision = new Decision(idReservation, Decision.Action.RESERVER, Decision.Issue.REFUS, "STOCK_INSUFFISANT");
        }
        decision = decisionRepository.save(decision);   // même transaction que le décrément

        log.info("DECISION type={} idMessage={} idReservation={} materiel={} quantite={} -> {} {}",
                cmd.type(), cmd.idMessage(), idReservation, idMateriel, quantite,
                decision.getIssue(), decision.getMotif() == null ? "" : decision.getMotif());
        return Optional.of(versMessage(decision));
    }

    @Transactional
    public Optional<MessageEnvelope> traiterLiberation(MessageEnvelope cmd) {
        String idReservation = cmd.idReservation();

        Optional<Decision> existante =
                decisionRepository.findByIdReservationAndAction(idReservation, Decision.Action.LIBERER);
        if (existante.isPresent()) {
            log.info("DOUBLON ignoré type={} idMessage={} idReservation={} : libération déjà faite, aucune nouvelle libération",
                    cmd.type(), cmd.idMessage(), idReservation);
            return Optional.of(versMessage(existante.get()));
        }

        // La quantité et le matériel viennent de l'ALLOCATION, pas du message (donnees est vide).
        Optional<Allocation> trouvee = allocationRepository.findById(idReservation);
        if (trouvee.isEmpty()) {
            log.warn("IGNORE type={} idMessage={} idReservation={} : aucune allocation à libérer",
                    cmd.type(), cmd.idMessage(), idReservation);
            return Optional.empty();
        }
        Allocation allocation = trouvee.get();

        if (allocation.getEtat() == Allocation.Etat.ALLOUEE) {
            allocation.setEtat(Allocation.Etat.LIBEREE);
            allocationRepository.save(allocation);
            materielRepository.restituer(allocation.getIdMateriel(), allocation.getQuantite());
        }
        Decision decision = decisionRepository.save(
                new Decision(idReservation, Decision.Action.LIBERER, Decision.Issue.LIBERE, null));

        log.info("DECISION type={} idMessage={} idReservation={} materiel={} quantite={} -> LIBERE",
                cmd.type(), cmd.idMessage(), idReservation, allocation.getIdMateriel(), allocation.getQuantite());
        return Optional.of(versMessage(decision));
    }

    /** Reconstruit le message résultat à partir de la décision, avec l'idMessage enregistré. */
    private MessageEnvelope versMessage(Decision d) {
        if (d.getAction() == Decision.Action.RESERVER && d.getIssue() == Decision.Issue.REFUS) {
            return MessageEnvelope.avecId(d.getIdMessageResultat(), RabbitConfig.T_RESERVATION_REFUSEE,
                    d.getIdReservation(), Map.of("motif", d.getMotif()));
        }
        Allocation a = allocationRepository.findById(d.getIdReservation()).orElseThrow();
        String type = d.getAction() == Decision.Action.RESERVER
                ? RabbitConfig.T_MATERIEL_RESERVE
                : RabbitConfig.T_RESERVATION_LIBEREE;
        return MessageEnvelope.avecId(d.getIdMessageResultat(), type, d.getIdReservation(),
                Map.of("idMateriel", a.getIdMateriel(), "quantite", a.getQuantite()));
    }
}