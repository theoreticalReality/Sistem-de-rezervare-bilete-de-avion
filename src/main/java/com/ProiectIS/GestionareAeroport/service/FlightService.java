package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.dto.CreateRegularFlightRequest;
import com.ProiectIS.GestionareAeroport.dto.CreateSeasonalFlightRequest;
import com.ProiectIS.GestionareAeroport.exception.BadRequestException;
import com.ProiectIS.GestionareAeroport.exception.NotFoundException;
import com.ProiectIS.GestionareAeroport.model.AirlineCompany;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.RegularFlight;
import com.ProiectIS.GestionareAeroport.model.SeasonalFlight;
import com.ProiectIS.GestionareAeroport.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlightService {

    private final FlightRepository flightRepository;
    private final AirlineCompanyService airlineCompanyService;

    public FlightService(FlightRepository flightRepository, AirlineCompanyService airlineCompanyService) {
        this.flightRepository = flightRepository;
        this.airlineCompanyService = airlineCompanyService;
    }

    @Transactional
    public RegularFlight addRegularFlight(Long airlineId, CreateRegularFlightRequest req) {
        if (flightRepository.existsByFlightCode(req.getFlightCode())) {
            throw new BadRequestException("Există deja un zbor cu codul: " + req.getFlightCode());
        }
        AirlineCompany airline = airlineCompanyService.findById(airlineId);
        RegularFlight flight = new RegularFlight();
        flight.setFlightCode(req.getFlightCode());
        flight.setAirplaneModel(req.getAirplaneModel());
        flight.setDepartureCity(req.getDepartureCity());
        flight.setDestinationCity(req.getDestinationCity());
        flight.setSeats(req.getSeats());
        flight.setPrices(req.getPrices());
        flight.setDaysOfWeek(req.getDaysOfWeek());
        flight.setDepartureTime(req.getDepartureTime());
        airline.addFlight(flight);
        return flightRepository.save(flight);
    }

    @Transactional
    public SeasonalFlight addSeasonalFlight(Long airlineId, CreateSeasonalFlightRequest req) {
        if (flightRepository.existsByFlightCode(req.getFlightCode())) {
            throw new BadRequestException("Există deja un zbor cu codul: " + req.getFlightCode());
        }
        AirlineCompany airline = airlineCompanyService.findById(airlineId);
        SeasonalFlight flight = new SeasonalFlight();
        flight.setFlightCode(req.getFlightCode());
        flight.setAirplaneModel(req.getAirplaneModel());
        flight.setDepartureCity(req.getDepartureCity());
        flight.setDestinationCity(req.getDestinationCity());
        flight.setSeats(req.getSeats());
        flight.setPrices(req.getPrices());
        flight.setDaysOfWeek(req.getDaysOfWeek());
        flight.setDepartureTime(req.getDepartureTime());
        flight.setSeasonStart(req.getSeasonStart());
        flight.setSeasonEnd(req.getSeasonEnd());
        airline.addFlight(flight);
        return flightRepository.save(flight);
    }

    @Transactional
    public void removeFlight(Long airlineId, String flightCode) {
        Flight flight = flightRepository.findByFlightCode(flightCode)
                .orElseThrow(() -> new NotFoundException("Zborul nu există: " + flightCode));
        if (flight.getAirline() == null || !flight.getAirline().getId().equals(airlineId)) {
            throw new BadRequestException("Nu poți șterge un zbor care nu îți aparține.");
        }
        flightRepository.delete(flight);
    }

    @Transactional(readOnly = true)
    public Flight findByCode(String flightCode) {
        return flightRepository.findByFlightCode(flightCode)
                .orElseThrow(() -> new NotFoundException("Zborul nu există: " + flightCode));
    }

    @Transactional(readOnly = true)
    public Flight findById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Zborul nu există: " + id));
    }

    @Transactional(readOnly = true)
    public List<Flight> findAll() {
        return flightRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Flight> findByAirline(Long airlineId) {
        return flightRepository.findByAirline_Id(airlineId);
    }
}
