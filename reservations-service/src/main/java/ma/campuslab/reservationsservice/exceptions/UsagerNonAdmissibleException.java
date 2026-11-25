package ma.campuslab.reservationsservice.exceptions;

import lombok.Getter;

@Getter
public class UsagerNonAdmissibleException extends RuntimeException {
    private final String motif;

    public UsagerNonAdmissibleException(String motif, String message) {
        super(message);
        this.motif = motif;
    }
}
