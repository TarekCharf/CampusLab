package ma.campuslab.reservationsservice.services;

import ma.campuslab.reservationsservice.entities.Reservation;
import ma.campuslab.reservationsservice.messaging.MessageEnvelope;


public interface IReservationService {

    Reservation findReservationById(String idReservation);
    Reservation addReservation(Reservation reservation);
    Reservation annulerReservation(String idReservation);
    Reservation appliquerResultat(MessageEnvelope message);
    String deleteAllReservations();
}
