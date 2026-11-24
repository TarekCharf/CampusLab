package ma.campuslab.reservationsservice.services;

import ma.campuslab.reservationsservice.entities.SuiviReservation;

import java.util.List;

public interface ISuiviReservationService {

    SuiviReservation findReservationById(String idReservation);
    int addSuiviReservation(SuiviReservation suiviReservation);
    String deleteAllSuiviReservations();
    List<SuiviReservation> findAllSuiviReservations();
}
