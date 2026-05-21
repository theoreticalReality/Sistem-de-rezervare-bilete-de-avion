package com.ProiectIS.GestionareAeroport.repository;

import com.ProiectIS.GestionareAeroport.model.AirportStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AirportStaffRepository extends JpaRepository<AirportStaff, Long> {
    Optional<AirportStaff> findByPersonalCode(String personalCode);
    Optional<AirportStaff> findByStaffId(String staffId);
}
