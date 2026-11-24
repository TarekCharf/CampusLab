package ma.campuslab.reservationsservice.listeners;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import ma.campuslab.reservationsservice.config.RabbitConfig;
import ma.campuslab.reservationsservice.messaging.MessageEnvelope;
import ma.campuslab.reservationsservice.services.IReservationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class ResultatListener {

    private final IReservationService reservationService;   // ton service métier (à écrire)

    public ResultatListener(IReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @RabbitListener(queues = RabbitConfig.Q_RESULTATS)
    public void onResultat(MessageEnvelope msg, Channel channel,
                           @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("RECU type={} idMessage={} idReservation={}", msg.type(), msg.idMessage(), msg.idReservation());
        try {
            // @Transactional : applique la transition valide (ou ignore un résultat déjà connu),
            // puis publie EtatReservationModifie APRÈS le commit.
            reservationService.appliquerResultat(msg);
            channel.basicAck(tag, false);                 // ack seulement une fois la base à jour
        } catch (Exception e) {
            log.error("ECHEC technique idMessage={} idReservation={} : {}", msg.idMessage(), msg.idReservation(), e.toString());
            channel.basicNack(tag, false, true);          // remis en file, sera retraité
        }
    }
}