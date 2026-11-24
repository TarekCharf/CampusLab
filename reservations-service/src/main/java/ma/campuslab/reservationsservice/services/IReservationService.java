package ma.campuslab.reservationsservice.services;

import ma.campuslab.reservationsservice.entities.Reservation;
import ma.campuslab.reservationsservice.messaging.MessageEnvelope;


public interface IReservationService {

    public Reservation findReservationById(String idReservation);
    public Reservation addReservation(Reservation reservation);
    public Reservation annulerReservation(String idReservation);
    public Reservation appliquerResultat(MessageEnvelope message);
    public String deleteAllReservations();
}
