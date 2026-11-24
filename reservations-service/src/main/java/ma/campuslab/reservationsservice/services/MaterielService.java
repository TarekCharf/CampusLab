package ma.campuslab.reservationsservice.services;

import ma.campuslab.reservationsservice.config.RabbitConfig;
import ma.campuslab.reservationsservice.dto.MaterielDto;
import ma.campuslab.reservationsservice.messaging.MaterielRegistry;
import ma.campuslab.reservationsservice.messaging.MaterielReset;
import ma.campuslab.reservationsservice.messaging.MessageEnvelope;
import ma.campuslab.reservationsservice.messaging.MessagePublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class MaterielService implements IMaterielService {
    private final MessagePublisher messagePublisher;
    private final MaterielRegistry pendingRegistry;
    private final MaterielReset pendingReset;

    public MaterielService(MessagePublisher messagePublisher, MaterielRegistry pendingRegistry, MaterielReset pendingReset) {
        this.messagePublisher = messagePublisher;
        this.pendingRegistry = pendingRegistry;
        this.pendingReset = pendingReset;
    }

    public MessageEnvelope publishMessage(String idMateriel) {
        MessageEnvelope messageEnvelope = MessageEnvelope.of(
                RabbitConfig.T_TEST, "TEST-001",
                Map.of("idMateriel", idMateriel)
        );
        messagePublisher.publier(RabbitConfig.EX_COMMANDES, RabbitConfig.RK_TEST, messageEnvelope);
        return messageEnvelope;
    }

    public List<MaterielDto> listMateriel() {
        String correlationId = "LISTE-MATERIEL-" + UUID.randomUUID();
        CompletableFuture<List<MaterielDto>> future = pendingRegistry.register(correlationId);

        MessageEnvelope messageEnvelope = MessageEnvelope.of(
                RabbitConfig.T_LISTE_MATERIEL, correlationId,
                Map.of()
        );
        try {
            messagePublisher.publier(RabbitConfig.EX_INVENTAIRE, RabbitConfig.RK_LISTE_MATERIEL, messageEnvelope);
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pendingRegistry.fail(correlationId, e);
            throw new IllegalStateException("Timeout en attendant la liste du matériel (" + correlationId + ")", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pendingRegistry.fail(correlationId, e);
            throw new IllegalStateException("Interruption lors de l'attente de la liste du matériel", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Erreur lors de la récupération de la liste du matériel", e.getCause());
        }
    }

    public String resetMateriel() {
        String correlationId = "RESET-MATERIEL-" + UUID.randomUUID();
        CompletableFuture<String> future = pendingReset.register(correlationId);

        MessageEnvelope messageEnvelope = MessageEnvelope.of(
                RabbitConfig.T_RESET_MATERIEL, correlationId,
                Map.of()
        );
        try {
            messagePublisher.publier(RabbitConfig.EX_INVENTAIRE, RabbitConfig.RK_RESET_MATERIEL, messageEnvelope);
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            pendingReset.fail(correlationId, e);
            throw new IllegalStateException("Erreur lors de la réinitialisation du matériel", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<MaterielDto> listResponse(MessageEnvelope message) {
        Object rawMateriels = message.donnees() != null ? message.donnees().get("materiels") : null;
        if (!(rawMateriels instanceof List<?> items)) {
            return List.of();
        }

        return items.stream()
                .filter(item -> item instanceof Map<?, ?>)
                .map(item -> {
                    Map<String, Object> map = (Map<String, Object>) item;
                    String idMateriel = map.get("idMateriel") != null
                            ? String.valueOf(map.get("idMateriel"))
                            : null;
                    String libelle = map.get("libelle") != null
                            ? String.valueOf(map.get("libelle"))
                            : null;
                    Object rawDispo = map.get("disponible");
                    int disponible = rawDispo instanceof Number n ? n.intValue() : 0;

                    return new MaterielDto(idMateriel, libelle, disponible);
                })
                .toList();
    }

    public String resetResponse(MessageEnvelope message) {
        return String.valueOf(message.donnees().get("resultat"));
    }
}
