package ma.campuslab.usagersservice.entities;

public enum AdmissibiliteUsager {
    ADMISSIBLE,
    NON_ADMISSIBLE;

    public String toString() {
        return switch (this) {
            case ADMISSIBLE -> "Admissible";
            case NON_ADMISSIBLE -> "Non Admissible";
        };
    }
}
