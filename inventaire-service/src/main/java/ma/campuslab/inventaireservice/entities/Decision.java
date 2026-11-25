package ma.campuslab.inventaireservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Décision durable pour (réservation, action). La contrainte d'unicité est le garde-fou final :
 * deux traitements simultanés du même message ne peuvent pas tous deux réussir.
 */
@Entity
@Table(name = "decision", uniqueConstraints =
@UniqueConstraint(name = "uk_decision_reservation_action", columnNames = {"id_reservation", "action_decision"}))
@Getter
@Setter
@NoArgsConstructor
public class Decision {

    public enum Action { RESERVER, LIBERER }
    public enum Issue { ACCEPTE, REFUS, LIBERE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_reservation", nullable = false)
    private String idReservation;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_decision", nullable = false, columnDefinition = "varchar(20)")
    private Action action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private Issue issue;

    private String motif;                 // STOCK_INSUFFISANT ou MATERIEL_INCONNU

    /** idMessage du résultat publié : conservé pour republier le MÊME identifiant en cas de redélivrance. */
    @Column(nullable = false)
    private String idMessageResultat;

    public Decision(String idReservation, Action action, Issue issue, String motif) {
        this.idReservation = idReservation;
        this.action = action;
        this.issue = issue;
        this.motif = motif;
        this.idMessageResultat = UUID.randomUUID().toString();
    }
}