package ma.campuslab.reservationsservice.repositories;

import ma.campuslab.reservationsservice.entities.SuiviReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ISuiviReservationRepository extends JpaRepository<SuiviReservation, String> {
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO suivi_reservation
          (id_reservation, id_usager, id_materiel, quantite, statut, version_etat)
        VALUES (:id, :usager, :materiel, :quantite, :statut, :version) AS nouveau
        ON DUPLICATE KEY UPDATE
          statut       = IF(nouveau.version_etat > version_etat, nouveau.statut, statut),
          version_etat = GREATEST(version_etat, nouveau.version_etat)
        """, nativeQuery = true)
    SuiviReservation upsertSiPlusRecent(@Param("id") String id,
                           @Param("usager") String usager,
                           @Param("materiel") String materiel,
                           @Param("quantite") int quantite,
                           @Param("statut") String statut,
                           @Param("version") int version);
    }
