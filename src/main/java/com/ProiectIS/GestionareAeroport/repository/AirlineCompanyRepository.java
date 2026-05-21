package com.ProiectIS.GestionareAeroport.repository;

import com.ProiectIS.GestionareAeroport.model.AirlineCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AirlineCompanyRepository extends JpaRepository<AirlineCompany, Long> {
    Optional<AirlineCompany> findByEmail(String email);
    Optional<AirlineCompany> findByCompanyId(String companyId);
    boolean existsByEmail(String email);
    boolean existsByCompanyId(String companyId);
}
