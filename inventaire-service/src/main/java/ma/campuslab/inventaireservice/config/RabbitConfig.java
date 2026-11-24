package ma.campuslab.inventaireservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologie RabbitMQ : identique dans Réservations et Inventaire.
 * Déclarer deux fois les mêmes éléments avec les mêmes paramètres est sans risque.
 */
@Configuration
public class RabbitConfig {

    // Exchanges
    public static final String EX_COMMANDES  = "campuslab.commandes";   // direct Reservations -> Inventaire
    public static final String EX_RESULTATS  = "campuslab.resultats";   // direct Inventaire -> Reservations
    public static final String EX_EVENEMENTS = "campuslab.evenements";  // fanout -> Suivi Reservations
    public static final String EX_INVENTAIRE = "campuslab.inventaire";  // direct  Reservations -> Inventaire (liste du matériel, reset du matériel)
    public static final String EX_RESULTATS_INVENTAIRE = "campuslab.resultats.inventaire"; // direct Inventaire -> Reservations (liste du matériel, reset du matériel)

    // Files
    public static final String Q_COMMANDES = "inventaire.commandes";
    public static final String Q_RESULTATS = "reservations.resultats";
    public static final String Q_SUIVI     = "suivi.etat";
    public static final String Q_LISTE_MATERIEL = "liste.materiel"; // File pour la liste du matériel
    public static final String Q_PENDING_MATERIEL = "pending.materiel"; // File pour les demandes de matériel en attente
    public static final String Q_RESET_MATERIEL = "reset.materiel"; // File pour le reset du matériel
    public static final String Q_PENDING_RESET     = "pending.reset";

    //Test
    public static final String Q_TEST    = "test.queue";
    public static final String RK_TEST     = "test.routing.key";
    public static final String T_TEST     = "TestMessageType";
    public static final String Q_TEST_R    = "test.resultats.queue";
    public static final String RK_TEST_R     = "test.resultats.routing.key";
    public static final String T_TEST_R     = "TestResultatMessageType";

    // Clés de routage
    public static final String RK_RESERVER = "reserver";
    public static final String RK_LIBERER  = "liberer";
    public static final String RK_RESULTAT = "resultat";
    public static final String RK_LISTE_MATERIEL = "listeMateriel"; // Clé de routage pour la liste du matériel
    public static final String RK_PENDING_MATERIEL = "pendingMateriel"; // Clé de routage pour les demandes de matériel en attente
    public static final String RK_PENDING_RESET = "pendingReset"; // Clé de routage
    public static final String RK_RESET_MATERIEL = "resetMateriel"; // Clé de routage pour le reset du matériel

    // Types de messages
    public static final String T_RESERVER_MATERIEL = "ReserverMateriel";
    public static final String T_MATERIEL_RESERVE  = "MaterielReserve";
    public static final String T_RESERVATION_REFUSEE = "ReservationMaterielRefusee";
    public static final String T_LIBERER_RESERVATION = "LibererReservation";
    public static final String T_RESERVATION_LIBEREE = "ReservationLiberee";
    public static final String T_ETAT_MODIFIE = "EtatReservationModifie";
    public static final String T_LISTE_MATERIEL = "ListeMateriel";
    public static final String T_PENDING_MATERIEL = "PendingMateriel"; // Type de message pour les demandes de matériel en attente
    public static final String T_PENDING_RESET = "PendingReset"; // Type de message pour les
    public static final String T_RESET_MATERIEL = "ResetMateriel";

    @Bean DirectExchange commandesExchange()  { return new DirectExchange(EX_COMMANDES, true, false); }
    @Bean DirectExchange resultatsExchange()  { return new DirectExchange(EX_RESULTATS, true, false); }
    @Bean FanoutExchange evenementsExchange() { return new FanoutExchange(EX_EVENEMENTS, true, false); }
    @Bean DirectExchange inventaireExchange() { return new DirectExchange(EX_INVENTAIRE, true, false); }
    @Bean DirectExchange resultatsInventaireExchange() { return new DirectExchange(EX_RESULTATS_INVENTAIRE, true, false); }

    // durable = la file survit à un redémarrage du broker
    @Bean Queue commandesQueue() { return QueueBuilder.durable(Q_COMMANDES).build(); }
    @Bean Queue resultatsQueue() { return QueueBuilder.durable(Q_RESULTATS).build(); }
    @Bean Queue suiviQueue()     { return QueueBuilder.durable(Q_SUIVI).build(); }
    @Bean Queue listeMaterielQueue() { return QueueBuilder.durable(Q_LISTE_MATERIEL).build(); } // File pour la liste du matériel
    @Bean Queue pendingMaterielQueue() { return QueueBuilder.durable(Q_PENDING_MATERIEL).build(); } // File pour les demandes de matériel en attente
    @Bean Queue pendingResetQueue() { return QueueBuilder.durable(Q_PENDING_RESET).build(); } // File pour les demandes de reset en attente
    @Bean Queue resetMaterielQueue() { return QueueBuilder.durable(Q_RESET_MATERIEL).build(); } // File pour le reset du matériel

    //Test
    @Bean Queue testQueue()     { return QueueBuilder.durable(Q_TEST).build(); }
    @Bean Binding testBinding() {
        return BindingBuilder.bind(testQueue()).to(commandesExchange()).with(RK_TEST);
    }
    @Bean Queue testResultatsQueue()     { return QueueBuilder.durable(Q_TEST_R).build(); }
    @Bean Binding testResultatsBinding() {
        return BindingBuilder.bind(testResultatsQueue()).to(resultatsExchange()).with(RK_TEST_R);
    }

    @Bean Binding reserverBinding() {
        return BindingBuilder.bind(commandesQueue()).to(commandesExchange()).with(RK_RESERVER);
    }
    @Bean Binding libererBinding() {
        return BindingBuilder.bind(commandesQueue()).to(commandesExchange()).with(RK_LIBERER);
    }
    @Bean Binding resultatBinding() {
        return BindingBuilder.bind(resultatsQueue()).to(resultatsExchange()).with(RK_RESULTAT);
    }
    @Bean Binding suiviBinding() {
        return BindingBuilder.bind(suiviQueue()).to(evenementsExchange());
    }

    @Bean Binding listeMaterielBinding() {
        return BindingBuilder.bind(listeMaterielQueue()).to(inventaireExchange()).with(RK_LISTE_MATERIEL);
    }
    @Bean Binding pendingMaterielBinding() {
        return BindingBuilder.bind(pendingMaterielQueue()).to(resultatsInventaireExchange()).with(RK_PENDING_MATERIEL);
    }
    @Bean Binding pendingResetBinding() {
        return BindingBuilder.bind(pendingResetQueue()).to(resultatsInventaireExchange()).with(RK_PENDING_RESET);
    }
    @Bean Binding resetMaterielBinding() {
        return BindingBuilder.bind(resetMaterielQueue()).to(inventaireExchange()).with(RK_RESET_MATERIEL);
    }

    /**
     * Messages en JSON. INFERRED : le type Java est déduit du paramètre de la méthode @RabbitListener,
     * pas de l'en-tête __TypeId__ (qui contient le nom de classe de l'AUTRE service, introuvable ici).
     */
    @Bean
    MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }
}