package ma.campuslab.inventaireservice.repositories;

import ma.campuslab.inventaireservice.entities.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAllocationRepository extends JpaRepository<Allocation, String> {}
