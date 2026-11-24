package ma.campuslab.inventaireservice.listeners;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import ma.campuslab.inventaireservice.config.RabbitConfig;
import ma.campuslab.inventaireservice.entities.Materiel;
import ma.campuslab.inventaireservice.messaging.MessageEnvelope;
import ma.campuslab.inventaireservice.messaging.MessagePublisher;
import ma.campuslab.inventaireservice.services.MaterielService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InventaireListener {
    private final MessagePublisher messagePublisher;
    private final MaterielService materielService;

    public InventaireListener(MessagePublisher messagePublisher, MaterielService materielService) {
        this.messagePublisher = messagePublisher;
        this.materielService = materielService;
    }

    @RabbitListener(queues = RabbitConfig.Q_LISTE_MATERIEL)
    public void onListeMateriel(MessageEnvelope msg, Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("RECU type={} idMessage={} idReservation={}", msg.type(), msg.idMessage(), msg.idReservation());
        try {
            List<Materiel> materiels = materielService.findAllMateriel();
            if (materiels.isEmpty()) {
                log.info("Aucun materiel trouvé");
            } else {
                log.info("Materiels trouvés : {}", materiels);
            }
            MessageEnvelope response = MessageEnvelope.of(
                    RabbitConfig.T_PENDING_MATERIEL, msg.idReservation(),
                    Map.of("materiels", materiels)
            );
            messagePublisher.publier(RabbitConfig.EX_RESULTATS_INVENTAIRE, RabbitConfig.RK_PENDING_MATERIEL, response);
            channel.basicAck(tag, false); // ack seulement une fois la base à jour
        } catch (Exception e) {
            log.error("ECHEC technique idMessage={} idReservation={} : {}", msg.idMessage(), msg.idReservation(), e.toString());
            channel.basicNack(tag, false, true); // remis en file, sera retraité
        }

    }

    @RabbitListener(queues = RabbitConfig.Q_RESET_MATERIEL)
    public void onResetMateriel(MessageEnvelope msg, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("RECU type={} idMessage={} idReservation={}", msg.type(), msg.idMessage(), msg.idReservation());
        try {
            String resultat = materielService.resetAllMateriel();
            log.info(resultat);

            MessageEnvelope response = MessageEnvelope.of(
                    RabbitConfig.T_PENDING_RESET, msg.idReservation(),
                    Map.of("resultat", resultat)
            );
            messagePublisher.publier(RabbitConfig.EX_RESULTATS_INVENTAIRE, RabbitConfig.RK_PENDING_RESET, response);
            channel.basicAck(tag, false); // ack seulement une fois la base à jour
        } catch (Exception e) {
            log.error("ECHEC technique idMessage={} idReservation={} : {}", msg.idMessage(), msg.idReservation(), e.toString());
            channel.basicNack(tag, false, true); // remis en file, sera retraité
        }

    }
}
