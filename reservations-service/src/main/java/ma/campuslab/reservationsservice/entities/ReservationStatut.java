package ma.campuslab.reservationsservice.entities;

public enum ReservationStatut {
    EN_ATTENTE,
    CONFIRMEE,
    REFUSEE,
    ANNULATION_EN_COURS,
    ANNULEE;

    public String toString() {
        return switch (this) {
            case EN_ATTENTE -> "En attente";
            case CONFIRMEE -> "Confirmée";
            case REFUSEE -> "Refusée";
            case ANNULATION_EN_COURS -> "Annulation en cours";
            case ANNULEE -> "Annulée";
        };
    }
}
