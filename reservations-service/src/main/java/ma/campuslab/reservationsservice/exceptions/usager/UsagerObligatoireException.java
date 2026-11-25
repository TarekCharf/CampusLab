package ma.campuslab.reservationsservice.exceptions.usager;


public class UsagerObligatoireException extends RuntimeException {
    public UsagerObligatoireException(String message) {
        super(message);
    }
}
