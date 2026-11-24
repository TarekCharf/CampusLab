package ma.campuslab.usagersservice.entities;

public enum EtatUsager {
    ACTIF,
    SUSPENDU;

    public String toString() {
        return switch (this) {
            case ACTIF -> "Actif";
            case SUSPENDU -> "Suspendu";
        };
    }
}
