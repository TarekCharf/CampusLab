package ma.campuslab.inventaireservice.listeners;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import ma.campuslab.inventaireservice.config.RabbitConfig;
import ma.campuslab.inventaireservice.messaging.MessageEnvelope;
import ma.campuslab.inventaireservice.messaging.MessagePublisher;
import ma.campuslab.inventaireservice.services.IMaterielService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class CommandeListener {

    private final IMaterielService materielService;   // ton service métier (à écrire)
    private final MessagePublisher publisher;

    public CommandeListener(IMaterielService materielService, MessagePublisher publisher) {
        this.materielService = materielService;
        this.publisher = publisher;
    }

    @RabbitListener(queues = RabbitConfig.Q_COMMANDES)
    public void onCommande(MessageEnvelope msg, Channel channel,
                           @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("RECU type={} idMessage={} idReservation={}", msg.type(), msg.idMessage(), msg.idReservation());
        try {
            // 1) Transaction métier : décision + modification du disponible, COMMIT à la sortie de la méthode.
            //    Un message déjà traité ne change pas le disponible : le résultat enregistré est renvoyé.
            Optional<MessageEnvelope> resultat = switch (msg.type()) {
                case RabbitConfig.T_RESERVER_MATERIEL   -> materielService.traiterReservation(msg);
                case RabbitConfig.T_LIBERER_RESERVATION -> materielService.traiterLiberation(msg);
                default -> {
                    log.warn("Type inconnu ignoré : {}", msg.type());
                    yield Optional.empty();
                }
            };

            // 2) Publication du résultat (après le commit)
            resultat.ifPresent(r -> publisher.publier(RabbitConfig.EX_RESULTATS, RabbitConfig.RK_RESULTAT, r));

            // 3) Ack en dernier : si le processus s'arrête avant, le broker redélivre et on rejoue sans double effet
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("ECHEC technique idMessage={} idReservation={} : {}", msg.idMessage(), msg.idReservation(), e.toString());
            channel.basicNack(tag, false, true);          // remis en file
        }
    }
}
