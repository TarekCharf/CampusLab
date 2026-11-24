package ma.campuslab.reservationsservice.services;

import ma.campuslab.reservationsservice.entities.SuiviReservation;

import java.util.List;

public interface ISuiviReservationService {

    public SuiviReservation findReservationById(String idReservation);
    public SuiviReservation addSuiviReservation(SuiviReservation suiviReservation);
    public String deleteAllSuiviReservations();
    public List<SuiviReservation> findAllSuiviReservations();
}
