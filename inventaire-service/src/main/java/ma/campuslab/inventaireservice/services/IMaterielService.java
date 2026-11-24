package ma.campuslab.inventaireservice.services;

import ma.campuslab.inventaireservice.entities.Materiel;
import ma.campuslab.inventaireservice.messaging.MessageEnvelope;

import java.util.List;
import java.util.Optional;

public interface IMaterielService {
    public Optional<Materiel> findMaterielById(String idMateriel);
    public List<Materiel> findAllMateriel();
    public Optional<MessageEnvelope> traiterReservation(MessageEnvelope message);
    public Optional<MessageEnvelope> traiterLiberation(MessageEnvelope message);
    public String resetAllMateriel();
}
