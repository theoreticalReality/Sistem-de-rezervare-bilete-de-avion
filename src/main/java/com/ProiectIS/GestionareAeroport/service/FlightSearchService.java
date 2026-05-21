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
        if (query.getDepartureDate() == null) {
            throw new BadRequestException("Data plecării este obligatorie.");
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
                throw new BadRequestException("Data întoarcerii este obligatorie pentru zbor dus-întors.");
            }
            if (query.getReturnDate().isBefore(query.getDepartureDate())) {
                throw new BadRequestException("Data întoarcerii nu poate fi înainte de plecare.");
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
}
