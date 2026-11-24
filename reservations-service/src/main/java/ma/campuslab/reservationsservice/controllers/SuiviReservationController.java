package ma.campuslab.reservationsservice.controllers;

import ma.campuslab.reservationsservice.entities.SuiviReservation;
import ma.campuslab.reservationsservice.services.ISuiviReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/suivi/reservations")
public class SuiviReservationController {

    private final ISuiviReservationService suiviReservationService;

    public SuiviReservationController(ISuiviReservationService suiviReservationService) {
        this.suiviReservationService = suiviReservationService;
    }

    @GetMapping("/find/{idsuiviReservation}")
    public ResponseEntity<SuiviReservation> findSuiviReservationById(@PathVariable String idsuiviReservation) {
        return ResponseEntity.status(HttpStatus.OK).body(suiviReservationService.findReservationById(idsuiviReservation));
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<SuiviReservation>> findAllSuiviReservations() {
        return ResponseEntity.status(HttpStatus.OK).body(suiviReservationService.findAllSuiviReservations());
    }
}
