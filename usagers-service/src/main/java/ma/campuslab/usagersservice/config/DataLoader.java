package ma.campuslab.usagersservice.config;

import ma.campuslab.usagersservice.entities.AdmissibiliteUsager;
import ma.campuslab.usagersservice.entities.EtatUsager;
import ma.campuslab.usagersservice.entities.Usager;
import ma.campuslab.usagersservice.repositories.IUsagerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader {

    @Bean
    CommandLineRunner chargerDonnees(IUsagerRepository usagerRepository) {
        return args -> {
            if (usagerRepository.count() == 0) {
                usagerRepository.saveAll(List.of(
                        new Usager("U01", EtatUsager.ACTIF, AdmissibiliteUsager.ADMISSIBLE),
                        new Usager("U02", EtatUsager.SUSPENDU, AdmissibiliteUsager.NON_ADMISSIBLE),
                        new Usager("U03", EtatUsager.ACTIF, AdmissibiliteUsager.ADMISSIBLE)));
            }
        };
    }
}
