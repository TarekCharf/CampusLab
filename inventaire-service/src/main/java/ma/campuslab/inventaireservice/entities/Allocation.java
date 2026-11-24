package ma.campuslab.inventaireservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Unités bloquées par une réservation confirmée. Une seule ligne par réservation. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Allocation {

    public enum Etat { ALLOUEE, LIBEREE }

    @Id
    private String idReservation;

    private String idMateriel;
    private int quantite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private Etat etat;

    public Allocation(String idReservation, String idMateriel, int quantite) {
        this.idReservation = idReservation;
        this.idMateriel = idMateriel;
        this.quantite = quantite;
        this.etat = Etat.ALLOUEE;
    }
}