package ma.campuslab.reservationsservice.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SuiviReservation {
    @Id
    private String idReservation;

    private String idUsager;
    private String idMateriel;

    private int quantite;
    private String statut;
    private int versionEtat;

    @Nullable
    private String Motif;

    public SuiviReservation(Reservation reservation){
        this.idReservation = reservation.getIdReservation();
        this.idUsager = reservation.getIdUsager();
        this.idMateriel = reservation.getIdMateriel();
        this.quantite = reservation.getQuantite();
        this.statut = reservation.getStatut().name();
        this.versionEtat = reservation.getVersionEtat();
        this.Motif = reservation.getMotif();
    }
}
