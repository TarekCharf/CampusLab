package ma.campuslab.reservationsservice.exceptions.usager;

public class UsagerServiceIndisponibleException extends RuntimeException {
    public UsagerServiceIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}