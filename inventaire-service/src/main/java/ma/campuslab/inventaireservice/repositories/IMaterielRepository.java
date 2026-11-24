package ma.campuslab.inventaireservice.repositories;

import ma.campuslab.inventaireservice.entities.Materiel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IMaterielRepository extends JpaRepository<Materiel, String> {
    @Modifying
    @Query("update Materiel m set m.disponible = m.disponible - :q " +
            "where m.idMateriel = :id and m.disponible >= :q")
    int reserverSiDisponible(@Param("id") String idMateriel, @Param("q") int quantite);   // 1 = réussi, 0 = stock insuffisant

    @Modifying
    @Query("update Materiel m set m.disponible = m.disponible + :q where m.idMateriel = :id")
    int restituer(@Param("id") String idMateriel, @Param("q") int quantite);
}