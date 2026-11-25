package ma.campuslab.reservationsservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UsagerNonAdmissibleException.class)
    public ResponseEntity<Map<String, Object>> handleNonAdmissible(UsagerNonAdmissibleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT) // 422
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 422,
                        "erreur", "USAGER_NON_ADMISSIBLE",
                        "motif", ex.getMotif(),
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(UsagerServiceIndisponibleException.class)
    public ResponseEntity<Map<String, Object>> handleIndisponible(UsagerServiceIndisponibleException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE) // 503
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 503,
                        "erreur", "SERVICE_USAGERS_INDISPONIBLE",
                        "motif", ex.getMessage()
                ));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 400,
                        "erreur", "REQUETE_INVALIDE",
                        "motif", ex.getMessage() != null ? ex.getMessage() : "Requête invalide"
                ));
    }
}
