package ma.campuslab.usagersservice.services;

import ma.campuslab.usagersservice.entities.AdmissibiliteUsager;
import ma.campuslab.usagersservice.entities.EtatUsager;
import ma.campuslab.usagersservice.entities.Usager;
import ma.campuslab.usagersservice.repositories.IUsagerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsagerService implements IUsagerService {

    private final IUsagerRepository usagerRepository;

    public UsagerService(IUsagerRepository usagerRepository) {
        this.usagerRepository = usagerRepository;
    }

    public Usager findUsagerById(String idUsager) {
        return usagerRepository.findById(idUsager).orElse(null);
    }

    public String resetAllUsagers() {
        usagerRepository.deleteAll();
        usagerRepository.saveAll(List.of(
                new Usager("U01", EtatUsager.ACTIF, AdmissibiliteUsager.ADMISSIBLE),
                new Usager("U02", EtatUsager.SUSPENDU, AdmissibiliteUsager.NON_ADMISSIBLE),
                new Usager("U03", EtatUsager.ACTIF, AdmissibiliteUsager.ADMISSIBLE)));
        return "Tous les usagers ont été réinitialisés. Count : " + usagerRepository.count();
    }
}
