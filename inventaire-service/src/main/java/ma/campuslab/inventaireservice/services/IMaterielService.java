package ma.campuslab.inventaireservice.services;

import ma.campuslab.inventaireservice.entities.Materiel;
import ma.campuslab.inventaireservice.messaging.MessageEnvelope;

import java.util.List;
import java.util.Optional;

public interface IMaterielService {
    Optional<Materiel> findMaterielById(String idMateriel);
    List<Materiel> findAllMateriel();
    String suiviReservationCheck(MessageEnvelope message);
    Optional<MessageEnvelope> traiterReservation(MessageEnvelope message);
    Optional<MessageEnvelope> traiterLiberation(MessageEnvelope message);
    String resetAllMateriel();
}
