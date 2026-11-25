package ma.campuslab.reservationsservice.listeners;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import ma.campuslab.reservationsservice.config.RabbitConfig;
import ma.campuslab.reservationsservice.entities.ReservationStatut;
import ma.campuslab.reservationsservice.entities.SuiviReservation;
import ma.campuslab.reservationsservice.messaging.MessageEnvelope;
import ma.campuslab.reservationsservice.services.ISuiviReservationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SuiviListener {

    private final ISuiviReservationService suiviReservationService;

    public SuiviListener(ISuiviReservationService suiviReservationService) {
        this.suiviReservationService = suiviReservationService;
    }

    @RabbitListener(queues = RabbitConfig.Q_SUIVI)
    public void onSuivi(MessageEnvelope msg, Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("RECU type={} idMessage={} idReservation={}", msg.type(), msg.idMessage(), msg.idReservation());
        try {
            // @Transactional : applique la transition valide (ou ignore un résultat déjà connu),
            // puis publie EtatReservationModifie APRÈS le commit.
            SuiviReservation suiviReservation = new SuiviReservation(
                    msg.idReservation(),
                    String.valueOf(msg.donnees().get("idUsager")),
                    String.valueOf(msg.donnees().get("idMateriel")),
                    ((Number) msg.donnees().get("quantite")).intValue(),
                    String.valueOf(msg.donnees().get("statut")),
                    ((Number) msg.donnees().get("versionEtat")).intValue(),
                    String.valueOf(msg.donnees().get("motif"))
            );
            suiviReservationService.addSuiviReservation(suiviReservation);
            channel.basicAck(tag, false);                 // ack seulement une fois la base à jour
        } catch (Exception e) {
            log.error("ECHEC technique idMessage={} idReservation={} : {}", msg.idMessage(), msg.idReservation(), e.toString());
            channel.basicNack(tag, false, true);          // remis en file, sera retraité
        }
    }
}
