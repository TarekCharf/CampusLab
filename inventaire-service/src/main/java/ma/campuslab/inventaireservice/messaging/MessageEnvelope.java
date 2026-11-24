package ma.campuslab.inventaireservice.messaging;

import java.util.Map;
import java.util.UUID;

/**
 * Enveloppe commune à tous les messages (versionSchema = 1).
 * La même classe est copiée dans chaque service (seul le package change).
 *
 * @param idMessage identifiant unique du message (UUID)
 * @param type Types de messages
 * @param versionSchema version du schéma de l'enveloppe (toujours 1 pour l'instant)
 * @param idReservation identifiant de la réservation concernée
 * @param donnees données spécifiques au type de message (ex. idMateriel, quantité, motif, etc.)
 * @author VotreNom(Claude)
 */
public record MessageEnvelope(
        String idMessage,
        String type,
        int versionSchema,
        String idReservation,
        Map<String, Object> donnees) {

    /** Nouveau message logique : un nouvel idMessage. */
    public static MessageEnvelope of(String type, String idReservation, Map<String, Object> donnees) {
        return new MessageEnvelope(UUID.randomUUID().toString(), type, 1, idReservation, donnees);
    }

    /** Même idMessage conservé (renvoi d'un résultat déjà connu). */
    public static MessageEnvelope avecId(String idMessage, String type, String idReservation,
                                         Map<String, Object> donnees) {
        return new MessageEnvelope(idMessage, type, 1, idReservation, donnees);
    }
}