package ma.campuslab.inventaireservice.repositories;

import ma.campuslab.inventaireservice.entities.Decision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IDecisionRepository extends JpaRepository<Decision, Long> {
    Optional<Decision> findByIdReservationAndAction(String idReservation, Decision.Action action); }