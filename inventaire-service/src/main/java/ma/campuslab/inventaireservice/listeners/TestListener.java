package ma.campuslab.inventaireservice.listeners;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import ma.campuslab.inventaireservice.config.RabbitConfig;
import ma.campuslab.inventaireservice.entities.Materiel;
import ma.campuslab.inventaireservice.messaging.MessageEnvelope;
import ma.campuslab.inventaireservice.messaging.MessagePublisher;
import ma.campuslab.inventaireservice.services.IMaterielService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class TestListener {

    private final IMaterielService materielService;
    private final MessagePublisher messagePublisher;

    public TestListener(IMaterielService materielService, MessagePublisher messagePublisher) {
        this.materielService = materielService;
        this.messagePublisher = messagePublisher;
    }
    @RabbitListener(queues = RabbitConfig.Q_TEST)
    public void onTestMessage(MessageEnvelope msg, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("RECU type={} idMessage={} idReservation={}", msg.type(), msg.idMessage(), msg.idReservation());
        try {
            Materiel materiel = materielService.findMaterielById(String.valueOf(msg.donnees().get("idMateriel"))).orElse(null);
            if (materiel != null) {
                log.info("Materiel trouvé : id={} libelle={} disponible={}", materiel.getIdMateriel(), materiel.getLibelle(), materiel.getDisponible());
            } else {
                log.warn("Materiel non trouvé : id={}", msg.donnees().get("idMateriel"));
            }
            MessageEnvelope response = MessageEnvelope.of(
                    RabbitConfig.T_TEST_R, msg.idReservation(),
                    Map.of("idMateriel", materiel.getIdMateriel(),
                            "libelle", materiel.getLibelle(),
                            "disponible", materiel.getDisponible())
            );
            messagePublisher.publier(RabbitConfig.EX_RESULTATS, RabbitConfig.RK_TEST_R, response);
            channel.basicAck(tag, false); // ack seulement une fois la base à jour
        } catch (Exception e) {
            log.error("ECHEC technique idMessage={} idReservation={} : {}", msg.idMessage(), msg.idReservation(), e.toString());
            channel.basicNack(tag, false, true); // remis en file, sera retraité
        }

    }
}
