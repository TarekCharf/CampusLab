package ma.campuslab.usagersservice.repositories;

import ma.campuslab.usagersservice.entities.Usager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUsagerRepository extends JpaRepository<Usager, String> {
}
