package ma.campuslab.reservationsservice.exceptions;


public class UsagerObligatoireException extends RuntimeException {
    public UsagerObligatoireException(String message, Throwable cause) {
        super(message, cause);
    }
}
