package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.dto.AirlineRegistrationRequest;
import com.ProiectIS.GestionareAeroport.exception.BadRequestException;
import com.ProiectIS.GestionareAeroport.exception.NotFoundException;
import com.ProiectIS.GestionareAeroport.exception.UnauthorizedException;
import com.ProiectIS.GestionareAeroport.model.AirlineCompany;
import com.ProiectIS.GestionareAeroport.repository.AirlineCompanyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AirlineCompanyService {

    private final AirlineCompanyRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AirlineCompanyService(AirlineCompanyRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AirlineCompany register(AirlineRegistrationRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Există deja o companie cu acest email.");
        }
        if (repository.existsByCompanyId(request.getCompanyId())) {
            throw new BadRequestException("Există deja o companie cu acest ID.");
        }
        AirlineCompany company = new AirlineCompany();
        company.setCompanyId(request.getCompanyId());
        company.setName(request.getName());
        company.setEmail(request.getEmail());
        company.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return repository.save(company);
    }

    @Transactional(readOnly = true)
    public AirlineCompany login(String email, String password) {
        AirlineCompany company = repository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Email sau parolă incorecte."));
        if (!passwordEncoder.matches(password, company.getPasswordHash())) {
            throw new UnauthorizedException("Email sau parolă incorecte.");
        }
        return company;
    }

    @Transactional(readOnly = true)
    public AirlineCompany findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compania nu a fost găsită: " + id));
    }

    @Transactional(readOnly = true)
    public AirlineCompany findByCompanyId(String companyId) {
        return repository.findByCompanyId(companyId)
                .orElseThrow(() -> new NotFoundException("Compania nu a fost găsită: " + companyId));
    }

    @Transactional(readOnly = true)
    public List<AirlineCompany> findAll() {
        return repository.findAll();
    }
}
