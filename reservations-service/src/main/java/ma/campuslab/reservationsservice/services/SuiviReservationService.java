package ma.campuslab.reservationsservice.services;

import ma.campuslab.reservationsservice.entities.SuiviReservation;
import ma.campuslab.reservationsservice.repositories.ISuiviReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuiviReservationService implements ISuiviReservationService {

    private final ISuiviReservationRepository suivireservationRepository;

    public SuiviReservationService(ISuiviReservationRepository suivireservationRepository) {
        this.suivireservationRepository = suivireservationRepository;
    }

    public SuiviReservation findReservationById(String idReservation) {
        return suivireservationRepository.findById(idReservation).orElseThrow();
    }

    public SuiviReservation addSuiviReservation(SuiviReservation suiviReservation) {
        return suivireservationRepository.save(suiviReservation);
    }

    public String deleteAllSuiviReservations() {
        suivireservationRepository.deleteAll();
        return "Tous les suivis de réservations ont été supprimés. Count : " + suivireservationRepository.count();
    }

    @Override
    public List<SuiviReservation> findAllSuiviReservations() {
        return suivireservationRepository.findAll();
    }
}
