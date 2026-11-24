package ma.campuslab.reservationsservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologie RabbitMQ : identique dans Réservations et Inventaire.
 * Déclarer deux fois les mêmes éléments avec les mêmes paramètres est sans risque.
 * EX_COMMANDES : Réservations -> Inventaire (direct)
 * EX_RESULTATS : Inventaire -> Réservations (direct)
 * EX_SUIVI : Inventaire -> Suivi Reservations (direct)
 * EX_SUIVI_CHECK : Suivi Reservations -> Inventaire (direct)
 * EX_INVENTAIRE : Réservations -> Inventaire (direct) pour la liste du matériel et le reset du matériel
 * EX_RESULTATS_INVENTAIRE : Inventaire -> Réservations (direct) pour la liste du matériel et le reset du matériel
 */
@Configuration
public class RabbitConfig {

    // Exchanges
    public static final String EX_COMMANDES  = "campuslab.commandes";   // direct Reservations -> Inventaire
    public static final String EX_RESULTATS  = "campuslab.resultats";   // direct Inventaire -> Reservations
    public static final String EX_SUIVI = "campuslab.suivi";  // direct Inventaire -> Suivi Reservations
    public static final String EX_SUIVI_CHECK = "campuslab.suivi.check"; // direct Inventaire -> Suivi Reservations (check)
    public static final String EX_CHECK_RESULTATS = "campuslab.check.resultats"; // direct Suivi Reservations -> Inventaire (check)
    public static final String EX_INVENTAIRE = "campuslab.inventaire";  // direct Reservations -> Inventaire (liste du matériel, reset du matériel)
    public static final String EX_RESULTATS_INVENTAIRE = "campuslab.resultats.inventaire"; // direct Inventaire -> Reservations (liste du matériel, reset du matériel)

    // Files
    public static final String Q_COMMANDES = "inventaire.commandes"; // File pour les commandes de réservation
    public static final String Q_RESULTATS = "reservations.resultats"; // File pour les résultats de réservation
    public static final String Q_SUIVI     = "suivi.etat"; // File pour les messages de suivi de l'état des réservations
    public static final String Q_SUIVI_CHECK     = "suivi.check.etat"; // File pour les messages de suivi de l'état des réservations(inventaire)
    public static final String Q_CHECK_RESULTATS = "check.resultats"; // File pour les messages de suivi de l'état des réservations(inventaire)
    public static final String Q_LISTE_MATERIEL = "liste.materiel"; // File pour la liste du matériel
    public static final String Q_PENDING_MATERIEL = "pending.materiel"; // File pour les demandes de matériel en attente
    public static final String Q_RESET_MATERIEL = "reset.materiel"; // File pour le reset du matériel
    public static final String Q_PENDING_RESET     = "pending.reset"; // File pour les demandes de reset en attente

    // Clés de routage
    public static final String RK_RESERVER = "reserver"; // Clé de routage pour les commandes de réservation
    public static final String RK_LIBERER  = "liberer"; // Clé de routage pour les commandes de libération de réservation
    public static final String RK_RESULTAT = "resultat"; // Clé de routage pour les résultats de réservation
    public static final String RK_SUIVI    = "suivi"; // Clé de routage pour les messages de suivi de l'état des réservations
    public static final String RK_SUIVI_CHECK = "suivi.check"; // Clé de routage pour les messages de suivi de l'état des réservations (check)
    public static final String RK_CHECK_RESULTAT = "check.resultat"; // Clé de routage pour les messages de suivi de l'état des réservations (check)
    public static final String RK_LISTE_MATERIEL = "listeMateriel"; // Clé de routage pour la liste du matériel
    public static final String RK_PENDING_MATERIEL = "pendingMateriel"; // Clé de routage pour les demandes de matériel en attente
    public static final String RK_PENDING_RESET = "pendingReset"; // Clé de routage
    public static final String RK_RESET_MATERIEL = "resetMateriel"; // Clé de routage pour le reset du matériel

    // Types de messages
    public static final String T_RESERVER_MATERIEL = "ReserverMateriel"; // Type de message pour les commandes de réservation
    public static final String T_MATERIEL_RESERVE  = "MaterielReserve"; // Type de message pour les résultats de réservation (confirmee)
    public static final String T_RESERVATION_REFUSEE = "ReservationMaterielRefusee"; // Type de message pour les résultats de réservation (refusee)
    public static final String T_LIBERER_RESERVATION = "LibererReservation"; // Type de message pour les commandes de libération de réservation (annulation en cours)
    public static final String T_RESERVATION_LIBEREE = "ReservationLiberee"; // Type de message pour les résultats de libération de réservation (annulee)
    public static final String T_ETAT_MODIFIE = "EtatReservationModifie"; // Type de message pour les messages de suivi de l'état des réservations
    public static final String T_ETAT_CHECK = "EtatReservationCheck"; // Type de message pour les messages de suivi de l'état des réservations (check)
    public static final String T_LISTE_MATERIEL = "ListeMateriel"; // Type de message pour la liste du matériel
    public static final String T_PENDING_MATERIEL = "PendingMateriel"; // Type de message pour les demandes de matériel en attente
    public static final String T_PENDING_RESET = "PendingReset"; // Type de message pour les demandes de reset en attente
    public static final String T_RESET_MATERIEL = "ResetMateriel"; // Type de message pour le reset du matériel

    @Bean DirectExchange commandesExchange()  { return new DirectExchange(EX_COMMANDES, true, false); }
    @Bean DirectExchange resultatsExchange()  { return new DirectExchange(EX_RESULTATS, true, false); }
    @Bean DirectExchange suiviExchange() { return new DirectExchange(EX_SUIVI, true, false); }
    @Bean DirectExchange suiviCheckExchange() { return new DirectExchange(EX_SUIVI_CHECK, true, false); }
    @Bean DirectExchange checkResultatsExchange() { return new DirectExchange(EX_CHECK_RESULTATS, true, false); }
    @Bean DirectExchange inventaireExchange() { return new DirectExchange(EX_INVENTAIRE, true, false); }
    @Bean DirectExchange resultatsInventaireExchange() { return new DirectExchange(EX_RESULTATS_INVENTAIRE, true, false); }

    // durable = la file survit à un redémarrage du broker
    @Bean Queue commandesQueue() { return QueueBuilder.durable(Q_COMMANDES).build(); }
    @Bean Queue resultatsQueue() { return QueueBuilder.durable(Q_RESULTATS).build(); }
    @Bean Queue suiviQueue()     { return QueueBuilder.durable(Q_SUIVI).build(); }
    @Bean Queue suiviCheckQueue()     { return QueueBuilder.durable(Q_SUIVI_CHECK).build(); }
    @Bean Queue checkResultatsQueue()     { return QueueBuilder.durable(Q_CHECK_RESULTATS).build(); }
    @Bean Queue listeMaterielQueue() { return QueueBuilder.durable(Q_LISTE_MATERIEL).build(); }
    @Bean Queue pendingMaterielQueue() { return QueueBuilder.durable(Q_PENDING_MATERIEL).build(); }
    @Bean Queue pendingResetQueue() { return QueueBuilder.durable(Q_PENDING_RESET).build(); }
    @Bean Queue resetMaterielQueue() { return QueueBuilder.durable(Q_RESET_MATERIEL).build(); }

    // Bindings : lier les files aux exchanges avec les clés de routage
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
        return BindingBuilder.bind(suiviQueue()).to(suiviExchange()).with(RK_SUIVI);
    }
    @Bean Binding suiviCheckBinding() {
        return BindingBuilder.bind(suiviCheckQueue()).to(suiviCheckExchange()).with(RK_SUIVI_CHECK);
    }
    @Bean Binding checkResultatBinding() {
        return BindingBuilder.bind(checkResultatsQueue()).to(checkResultatsExchange()).with(RK_CHECK_RESULTAT);
    }
    @Bean Binding listeMaterielBinding() {
        return BindingBuilder.bind(listeMaterielQueue()).to(inventaireExchange()).with(RK_LISTE_MATERIEL);
    }
    @Bean Binding resetMaterielBinding() {
        return BindingBuilder.bind(resetMaterielQueue()).to(inventaireExchange()).with(RK_RESET_MATERIEL);
    }
    @Bean Binding pendingMaterielBinding() {
        return BindingBuilder.bind(pendingMaterielQueue()).to(resultatsInventaireExchange()).with(RK_PENDING_MATERIEL);
    }
    @Bean Binding pendingResetBinding() {
        return BindingBuilder.bind(pendingResetQueue()).to(resultatsInventaireExchange()).with(RK_PENDING_RESET);
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