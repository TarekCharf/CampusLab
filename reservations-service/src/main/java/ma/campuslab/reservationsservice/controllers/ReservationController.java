package ma.campuslab.reservationsservice.controllers;

import ma.campuslab.reservationsservice.entities.Reservation;
import ma.campuslab.reservationsservice.services.IReservationService;
import ma.campuslab.reservationsservice.services.ISuiviReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final IReservationService reservationService;
    private final ISuiviReservationService suiviReservationService;

    public ReservationController(IReservationService reservationService, ISuiviReservationService suiviReservationService) {
        this.reservationService = reservationService;
        this.suiviReservationService = suiviReservationService;
    }

    @GetMapping("/find/{idReservation}")
    public ResponseEntity<Reservation> findReservationById(@PathVariable String idReservation) {
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.findReservationById(idReservation));
    }

    @PostMapping("/add")
    public ResponseEntity<Reservation> addReservation(@RequestBody Reservation reservation) {
        return ResponseEntity.status((HttpStatus.CREATED)).body(reservationService.addReservation(reservation));
    }

    @PutMapping("/annuler/{idReservation}")
    public ResponseEntity<Reservation> annulerReservation(@PathVariable String idReservation) {
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.annulerReservation(idReservation));
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<String> deleteAllReservations() {
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.deleteAllReservations() +
                " | " + suiviReservationService.deleteAllSuiviReservations());
    }
}
