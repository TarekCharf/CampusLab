package ma.campuslab.reservationsservice.listeners;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import ma.campuslab.reservationsservice.config.RabbitConfig;
import ma.campuslab.reservationsservice.dto.MaterielDto;
import ma.campuslab.reservationsservice.messaging.MaterielRegistry;
import ma.campuslab.reservationsservice.messaging.MaterielReset;
import ma.campuslab.reservationsservice.messaging.MessageEnvelope;
import ma.campuslab.reservationsservice.services.IMaterielService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class InventaireListener {

    private final IMaterielService materielService;
    private final MaterielRegistry pendingRegistry;
    private final MaterielReset pendingReset;

    public InventaireListener(IMaterielService materielService, MaterielRegistry pendingRegistry, MaterielReset pendingReset) {
        this.materielService = materielService;
        this.pendingRegistry = pendingRegistry;
        this.pendingReset = pendingReset;
    }

    @RabbitListener(queues = RabbitConfig.Q_PENDING_MATERIEL)
    public void onListeMateriel(MessageEnvelope msg, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("RECU type={} idMessage={} idReservation={}", msg.type(), msg.idMessage(), msg.idReservation());
        try {
            List<MaterielDto> materiels = materielService.listResponse(msg);
            if (materiels.isEmpty()) {
                log.info("Aucun materiel trouvé");
            } else {
                log.info("Materiels trouvés : {}", materiels);
            }
            pendingRegistry.complete(msg.idReservation(), materiels);
            channel.basicAck(tag, false);                 // ack seulement une fois la base à jour
        } catch (Exception e) {
            log.error("ECHEC technique idMessage={} idReservation={} : {}", msg.idMessage(), msg.idReservation(), e.toString());
            channel.basicNack(tag, false, true);          // remis en file, sera retraité
        }
    }

    @RabbitListener(queues = RabbitConfig.Q_PENDING_RESET)
    public void onResetMateriel(MessageEnvelope msg, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("RECU type={} idMessage={} idReservation={}", msg.type(), msg.idMessage(), msg.idReservation());
        try {
            String resultat = materielService.resetResponse(msg);
            log.info("Résultat du reset du matériel : {}", resultat);
            pendingReset.complete(msg.idReservation(), resultat);
            channel.basicAck(tag, false);                 // ack seulement une fois la base à jour
        } catch (Exception e) {
            log.error("ECHEC technique idMessage={} idReservation={} : {}", msg.idMessage(), msg.idReservation(), e.toString());
            channel.basicNack(tag, false, true);          // remis en file, sera retraité
        }
    }
}
