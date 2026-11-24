package ma.campuslab.reservationsservice.listeners;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import ma.campuslab.reservationsservice.config.RabbitConfig;
import ma.campuslab.reservationsservice.messaging.MessageEnvelope;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class TestResultatListener {
    @RabbitListener(queues = RabbitConfig.Q_TEST_R)
    public void onTestMessage(MessageEnvelope msg, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("RECU type={} idMessage={} idReservation={}", msg.type(), msg.idMessage(), msg.idReservation());
        try {
            // Traitez le message de test ici
            log.info("Traitement du message de test : {}", msg.donnees());

            // Acknowledge le message après traitement
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("ECHEC technique idMessage={} idReservation={} : {}", msg.idMessage(), msg.idReservation(), e.toString());
            channel.basicNack(tag, false, true); // remis en file, sera retraité
        }
    }
}
