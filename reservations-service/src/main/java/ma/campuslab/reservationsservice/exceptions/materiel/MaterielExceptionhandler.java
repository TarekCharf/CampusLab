package ma.campuslab.reservationsservice.exceptions.materiel;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class MaterielExceptionhandler {

    @ExceptionHandler(MaterielObligatoireException.class)
    public ResponseEntity<Map<String, Object>> handleMaterielObligatoire(MaterielObligatoireException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 422
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 422,
                        "erreur", "MATERIEL_OBLIGATOIRE",
                        "motif", ex.getCause().getMessage(),
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(MaterielQuantiteException.class)
    public ResponseEntity<Map<String, Object>> handleMaterielQuantite(MaterielQuantiteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 422
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 422,
                        "erreur", "QUANTITE_MATERIEL_INCORRECTE",
                        "motif", ex.getCause().getMessage(),
                        "message", ex.getMessage()
                ));
    }
}
