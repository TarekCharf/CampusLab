package ma.campuslab.reservationsservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class MessagePublisher {

    private final RabbitTemplate rabbit;
    private final MessageConverter converter;

    public MessagePublisher(RabbitTemplate rabbit, MessageConverter converter) {
        this.rabbit = rabbit;
        this.converter = converter;
    }

    /** Publie tout de suite (messages persistants par défaut). */
    public void publier(String exchange, String routingKey, MessageEnvelope msg) {
        // Le JSON complet est journalisé : c'est lui qu'on rejoue dans T04, T08 et T10.
        String json = new String(converter.toMessage(msg, new MessageProperties()).getBody(),
                StandardCharsets.UTF_8);
        log.info("PUBLIE type={} idMessage={} idReservation={} -> {}/{} payload={}",
                msg.type(), msg.idMessage(), msg.idReservation(), exchange, routingKey, json);
        rabbit.convertAndSend(exchange, routingKey, msg);
    }

    /**
     * Publie seulement APRÈS le commit de la transaction en cours :
     * jamais de message pour une écriture finalement annulée.
     */
    public void publierApresCommit(String exchange, String routingKey, MessageEnvelope msg) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publier(exchange, routingKey, msg);
                }
            });
        } else {
            publier(exchange, routingKey, msg);
        }
    }
}