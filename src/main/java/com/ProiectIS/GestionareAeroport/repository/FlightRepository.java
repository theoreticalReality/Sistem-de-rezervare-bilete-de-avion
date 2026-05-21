package com.ProiectIS.GestionareAeroport.repository;

import com.ProiectIS.GestionareAeroport.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    Optional<Flight> findByFlightCode(String flightCode);
    boolean existsByFlightCode(String flightCode);
    List<Flight> findByDepartureCityIgnoreCaseAndDestinationCityIgnoreCase(String from, String to);
    List<Flight> findByAirline_Id(Long airlineId);
}
