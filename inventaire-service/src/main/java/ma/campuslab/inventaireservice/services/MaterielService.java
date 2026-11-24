package ma.campuslab.inventaireservice.services;

import ma.campuslab.inventaireservice.config.RabbitConfig;
import ma.campuslab.inventaireservice.entities.Materiel;
import ma.campuslab.inventaireservice.messaging.MessageEnvelope;
import ma.campuslab.inventaireservice.repositories.IMaterielRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MaterielService implements IMaterielService{

    private final IMaterielRepository materielRepository;

    public MaterielService(IMaterielRepository materielRepository) {
        this.materielRepository = materielRepository;
    }

    public Optional<Materiel> findMaterielById(String idMateriel) {
        return materielRepository.findById(idMateriel);
    }

    public String resetAllMateriel() {
        materielRepository.deleteAll();
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
    public Optional<MessageEnvelope> traiterReservation(MessageEnvelope message) {
        message.donnees().get("idMateriel");
        Materiel materiel = findMaterielById(String.valueOf(message.donnees().get("idMateriel"))).orElse(null);
        if (materiel == null) {
            return Optional.of(MessageEnvelope.of(
                    RabbitConfig.T_RESERVATION_REFUSEE, message.idReservation(),
                    Map.of(
                            "idMateriel", message.donnees().get("idMateriel"),
                            "motif", "Materiel inconnu"
                    )
            ));
        }

        int quantite = ((Number)message.donnees().get("quantite")).intValue();
        if (materiel.getDisponible() < quantite) {
            return Optional.of(MessageEnvelope.of(
                    RabbitConfig.T_RESERVATION_REFUSEE, message.idReservation(),
                    Map.of(
                            "idMateriel", materiel.getIdMateriel(),
                            "quantite", quantite,
                            "motif", "Stock insuffisant"
                    )
            ));
        }
        materiel.setDisponible(materiel.getDisponible() - quantite);
        materielRepository.save(materiel);

        return Optional.of(MessageEnvelope.of(
                RabbitConfig.T_MATERIEL_RESERVE, message.idReservation(),
                Map.of(
                        "idMateriel", materiel.getIdMateriel(),
                        "quantite", quantite
                )
        ));
    }

    @Transactional
    public Optional<MessageEnvelope> traiterLiberation(MessageEnvelope message) {
        message.donnees().get("idMateriel");
        Materiel materiel = findMaterielById(String.valueOf(message.donnees().get("idMateriel"))).orElse(null);
        if (materiel == null) {
            return Optional.of(MessageEnvelope.of(
                    RabbitConfig.T_RESERVATION_REFUSEE, message.idReservation(),
                    Map.of(
                            "idMateriel", message.donnees().get("idMateriel"),
                            "motif", "Materiel inconnu"
                    )
            ));
        }
        int quantite = ((Number)message.donnees().get("quantite")).intValue();
        materiel.setDisponible(materiel.getDisponible() + quantite);
        materielRepository.save(materiel);

        return Optional.of(MessageEnvelope.of(
                RabbitConfig.T_RESERVATION_LIBEREE, message.idReservation(),
                Map.of(
                        "idMateriel", materiel.getIdMateriel(),
                        "quantite", quantite
                )
        ));
    }
}
