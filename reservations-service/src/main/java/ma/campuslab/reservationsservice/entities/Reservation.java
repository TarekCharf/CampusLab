package ma.campuslab.reservationsservice.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
public class Reservation {
    @Id
    private String idReservation;

    private String idUsager;
    private String idMateriel;

    private int quantite;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'EN_ATTENTE'")
    private ReservationStatut statut;

    @ColumnDefault("1")
    private int versionEtat;

    @Nullable
    private String Motif;
}
