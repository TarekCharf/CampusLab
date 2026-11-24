package ma.campuslab.inventaireservice.repositories;

import ma.campuslab.inventaireservice.entities.Materiel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IMaterielRepository extends JpaRepository<Materiel, String> {
}
