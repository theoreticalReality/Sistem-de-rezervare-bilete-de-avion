package com.ProiectIS.GestionareAeroport.controller;

import com.ProiectIS.GestionareAeroport.dto.AirlineLoginRequest;
import com.ProiectIS.GestionareAeroport.dto.AirlineRegistrationRequest;
import com.ProiectIS.GestionareAeroport.dto.CreateRegularFlightRequest;
import com.ProiectIS.GestionareAeroport.dto.CreateSeasonalFlightRequest;
import com.ProiectIS.GestionareAeroport.dto.FlightDto;
import com.ProiectIS.GestionareAeroport.dto.LoginResponse;
import com.ProiectIS.GestionareAeroport.model.AirlineCompany;
import com.ProiectIS.GestionareAeroport.service.AirlineCompanyService;
import com.ProiectIS.GestionareAeroport.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/airlines")
public class AirlineController {

    private final AirlineCompanyService airlineCompanyService;
    private final FlightService flightService;

    public AirlineController(AirlineCompanyService airlineCompanyService, FlightService flightService) {
        this.airlineCompanyService = airlineCompanyService;
        this.flightService = flightService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse register(@Valid @RequestBody AirlineRegistrationRequest request) {
        AirlineCompany company = airlineCompanyService.register(request);
        return toLoginResponse(company, "Compania aeriana a fost inregistrata.");
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody AirlineLoginRequest request) {
        AirlineCompany company = airlineCompanyService.login(request.getEmail(), request.getPassword());
        return toLoginResponse(company, "Autentificare reusita.");
    }

    @GetMapping("/{airlineId}/flights")
    public List<FlightDto> listFlights(@PathVariable Long airlineId) {
        airlineCompanyService.findById(airlineId);
        return flightService.findDtosByAirline(airlineId);
    }

    @PostMapping("/{airlineId}/flights/regular")
    @ResponseStatus(HttpStatus.CREATED)
    public FlightDto addRegularFlight(@PathVariable Long airlineId,
                                      @Valid @RequestBody CreateRegularFlightRequest request) {
        return FlightDto.fromEntity(flightService.addRegularFlight(airlineId, request));
    }

    @PostMapping("/{airlineId}/flights/seasonal")
    @ResponseStatus(HttpStatus.CREATED)
    public FlightDto addSeasonalFlight(@PathVariable Long airlineId,
                                       @Valid @RequestBody CreateSeasonalFlightRequest request) {
        return FlightDto.fromEntity(flightService.addSeasonalFlight(airlineId, request));
    }

    @DeleteMapping("/{airlineId}/flights/{flightCode}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long airlineId, @PathVariable String flightCode) {
        flightService.removeFlight(airlineId, flightCode);
        return ResponseEntity.noContent().build();
    }

    private LoginResponse toLoginResponse(AirlineCompany company, String message) {
        return new LoginResponse(
                true,
                "AIRLINE",
                String.valueOf(company.getId()),
                company.getName(),
                message
        );
    }
}
