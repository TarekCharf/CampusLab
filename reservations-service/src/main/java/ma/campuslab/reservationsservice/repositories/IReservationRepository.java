package ma.campuslab.reservationsservice.repositories;

import ma.campuslab.reservationsservice.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IReservationRepository extends JpaRepository<Reservation, String> {
}
