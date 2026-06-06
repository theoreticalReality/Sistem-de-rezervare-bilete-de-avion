package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.dto.FlightDto;
import com.ProiectIS.GestionareAeroport.dto.SearchQuery;
import com.ProiectIS.GestionareAeroport.dto.SearchResult;
import com.ProiectIS.GestionareAeroport.exception.BadRequestException;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class FlightSearchService {

    private final FlightRepository flightRepository;

    public FlightSearchService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional(readOnly = true)
    public SearchResult search(SearchQuery query) {
        if (query == null) {
            throw new BadRequestException("Criteriile de cautare sunt obligatorii.");
        }
        if (isBlank(query.getDepartureCity()) || isBlank(query.getDestinationCity())) {
            throw new BadRequestException("Locul de plecare si destinatia sunt obligatorii.");
        }
        if (query.getDepartureDate() == null) {
            throw new BadRequestException("Data plecarii este obligatorie.");
        }

        if (query.getNumberOfPassengers() == null || query.getNumberOfPassengers() <= 0) {
            throw new BadRequestException("Numarul de persoane trebuie sa fie cel putin 1.");
        }

        List<FlightDto> outbound = findMatchingFlights(
                query.getDepartureCity(),
                query.getDestinationCity(),
                query.getDepartureDate(),
                query.getNumberOfPassengers()
        );

        List<FlightDto> returnFlights = Collections.emptyList();
        if (Boolean.TRUE.equals(query.getWantsReturn())) {
            if (query.getReturnDate() == null) {
                throw new BadRequestException("Data intoarcerii este obligatorie pentru zbor dus-intors.");
            }
            if (query.getReturnDate().isBefore(query.getDepartureDate())) {
                throw new BadRequestException("Data intoarcerii nu poate fi inainte de plecare.");
            }
            returnFlights = findMatchingFlights(
                    query.getDestinationCity(),
                    query.getDepartureCity(),
                    query.getReturnDate(),
                    query.getNumberOfPassengers()
            );
        }

        return new SearchResult(outbound, returnFlights);
    }

    private List<FlightDto> findMatchingFlights(String from, String to, LocalDate date, Integer passengerCount) {
        List<Flight> candidates = flightRepository
                .findByDepartureCityIgnoreCaseAndDestinationCityIgnoreCase(from, to);
        return candidates.stream()
                .filter(f -> !f.isCancelled())
                .filter(f -> f.isAvailableOn(date))
                .filter(f -> hasEnoughSeats(f, passengerCount))
                .map(f -> FlightDto.fromEntity(f, date))
                .toList();
    }

    private boolean hasEnoughSeats(Flight flight, Integer passengerCount) {
        if (passengerCount == null || passengerCount <= 0) return true;
        return flight.getSeats().values().stream()
                .anyMatch(s -> s != null && s >= passengerCount);
    }

    public List<String> getAvailableDepartureCities() {
        return flightRepository.findAll().stream()
                .map(f -> f.getDepartureCity().toLowerCase())
                .distinct()
                .toList();
    }

    public List<String> getAvailableDestinations(String from) {
        if (isBlank(from)) return List.of();
        return flightRepository.findAll().stream()
                .filter(f -> f.getDepartureCity().equalsIgnoreCase(from.trim()))
                .map(f -> f.getDestinationCity().toLowerCase())
                .distinct()
                .toList();
    }

    public List<java.time.LocalDate> getAvailableDates(String from, String to) {
        if (isBlank(from) || isBlank(to)) return List.of();
        
        List<Flight> flights = flightRepository.findByDepartureCityIgnoreCaseAndDestinationCityIgnoreCase(from.trim(), to.trim());
        List<java.time.LocalDate> dates = new java.util.ArrayList<>();
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate limit = today.plusMonths(6);

        for (java.time.LocalDate date = today; !date.isAfter(limit); date = date.plusDays(1)) {
            final java.time.LocalDate currentDate = date;
            boolean hasFlight = flights.stream().anyMatch(f -> !f.isCancelled() && f.isAvailableOn(currentDate));
            if (hasFlight) {
                dates.add(currentDate);
            }
        }
        return dates;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
