package ma.campuslab.inventaireservice.listeners;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import ma.campuslab.inventaireservice.config.RabbitConfig;
import ma.campuslab.inventaireservice.messaging.MessageEnvelope;
import ma.campuslab.inventaireservice.services.IMaterielService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SuiviListener {
    private final IMaterielService materielService;

    public SuiviListener(IMaterielService materielService) {
        this.materielService = materielService;
    }

    @RabbitListener(queues = RabbitConfig.Q_CHECK_RESULTATS)
    public void onCheckResultats(MessageEnvelope msg, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("RECU type={} idMessage={} idReservation={}", msg.type(), msg.idMessage(), msg.idReservation());
        try {
            //  Traitement des résultats de check
            String versionEtat = materielService.suiviReservationCheck(msg);

            channel.basicAck(tag, false); // ack seulement une fois la base à jour
        } catch (Exception e) {
            log.error("ECHEC technique idMessage={} idReservation={} : {}", msg.idMessage(), msg.idReservation(), e.toString());
            channel.basicNack(tag, false, true); // remis en file, sera retraité
        }

    }
}
