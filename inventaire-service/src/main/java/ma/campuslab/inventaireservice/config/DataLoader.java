package ma.campuslab.inventaireservice.config;

import ma.campuslab.inventaireservice.entities.Materiel;
import ma.campuslab.inventaireservice.repositories.IMaterielRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader {

    @Bean
    CommandLineRunner chargerDonnees(IMaterielRepository materielRepository) {
        return args -> {
            if (materielRepository.count() == 0) {
                materielRepository.saveAll(List.of(
                        new Materiel("M01", "Vidéoprojecteur", 3),
                        new Materiel("M02", "Microphone", 1),
                        new Materiel("M03", "Caméra", 0)));
            }
        };
    }
}
