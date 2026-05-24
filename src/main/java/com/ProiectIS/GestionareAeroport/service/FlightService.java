package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.dto.CreateRegularFlightRequest;
import com.ProiectIS.GestionareAeroport.dto.CreateSeasonalFlightRequest;
import com.ProiectIS.GestionareAeroport.dto.FlightDto;
import com.ProiectIS.GestionareAeroport.exception.BadRequestException;
import com.ProiectIS.GestionareAeroport.exception.NotFoundException;
import com.ProiectIS.GestionareAeroport.model.AirlineCompany;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.RegularFlight;
import com.ProiectIS.GestionareAeroport.model.SeasonalFlight;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

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
            throw new BadRequestException("Exista deja un zbor cu codul: " + req.getFlightCode());
        }
        validateFlightDetails(req.getSeats(), req.getPrices());
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
        flight.setArrivalTime(req.getArrivalTime());
        airline.addFlight(flight);
        return flightRepository.save(flight);
    }

    @Transactional
    public SeasonalFlight addSeasonalFlight(Long airlineId, CreateSeasonalFlightRequest req) {
        if (flightRepository.existsByFlightCode(req.getFlightCode())) {
            throw new BadRequestException("Există deja un zbor cu codul: " + req.getFlightCode());
        }
        validateFlightDetails(req.getSeats(), req.getPrices());
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
        flight.setArrivalTime(req.getArrivalTime());
        flight.setSeasonStart(req.getSeasonStart());
        flight.setSeasonEnd(req.getSeasonEnd());
        airline.addFlight(flight);
        return flightRepository.save(flight);
    }

    @Transactional
    public RegularFlight updateRegularFlight(Long airlineId, String currentFlightCode, CreateRegularFlightRequest req) {
        Flight existing = findEditableFlight(airlineId, currentFlightCode);
        if (!(existing instanceof RegularFlight flight)) {
            throw new BadRequestException("Zborul selectat nu este un zbor regulat.");
        }
        validateUpdatedFlightCode(currentFlightCode, req.getFlightCode());
        validateFlightDetails(req.getSeats(), req.getPrices());

        flight.setFlightCode(req.getFlightCode());
        flight.setAirplaneModel(req.getAirplaneModel());
        flight.setDepartureCity(req.getDepartureCity());
        flight.setDestinationCity(req.getDestinationCity());
        flight.setSeats(req.getSeats());
        flight.setPrices(req.getPrices());
        flight.setDaysOfWeek(req.getDaysOfWeek());
        flight.setDepartureTime(req.getDepartureTime());
        flight.setArrivalTime(req.getArrivalTime());
        return flightRepository.save(flight);
    }

    @Transactional
    public SeasonalFlight updateSeasonalFlight(Long airlineId, String currentFlightCode, CreateSeasonalFlightRequest req) {
        Flight existing = findEditableFlight(airlineId, currentFlightCode);
        if (!(existing instanceof SeasonalFlight flight)) {
            throw new BadRequestException("Zborul selectat nu este un zbor sezonier.");
        }
        validateUpdatedFlightCode(currentFlightCode, req.getFlightCode());
        validateFlightDetails(req.getSeats(), req.getPrices());

        flight.setFlightCode(req.getFlightCode());
        flight.setAirplaneModel(req.getAirplaneModel());
        flight.setDepartureCity(req.getDepartureCity());
        flight.setDestinationCity(req.getDestinationCity());
        flight.setSeats(req.getSeats());
        flight.setPrices(req.getPrices());
        flight.setDaysOfWeek(req.getDaysOfWeek());
        flight.setDepartureTime(req.getDepartureTime());
        flight.setArrivalTime(req.getArrivalTime());
        flight.setSeasonStart(req.getSeasonStart());
        flight.setSeasonEnd(req.getSeasonEnd());
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
    public FlightDto findDtoById(Long id, java.time.LocalDate date) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Zborul nu existÄƒ: " + id));
        return FlightDto.fromEntity(flight, date);
    }

    @Transactional(readOnly = true)
    public List<Flight> findAll() {
        return flightRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FlightDto> findAllDtos() {
        return flightRepository.findAll().stream()
                .map(FlightDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Flight> findByAirline(Long airlineId) {
        return flightRepository.findByAirline_Id(airlineId);
    }

    @Transactional(readOnly = true)
    public List<FlightDto> findDtosByAirline(Long airlineId) {
        return flightRepository.findByAirline_Id(airlineId).stream()
                .map(FlightDto::fromEntity)
                .toList();
    }

    private void validateFlightDetails(Map<ClassType, Integer> seats, Map<ClassType, Double> prices) {
        EnumSet<ClassType> requiredClasses = EnumSet.allOf(ClassType.class);
        if (seats == null || !seats.keySet().containsAll(requiredClasses)) {
            throw new BadRequestException("Trebuie introdus numărul de locuri pentru BUSINESS, FIRST_CLASS și ECONOMY.");
        }
        if (prices == null || !prices.keySet().containsAll(requiredClasses)) {
            throw new BadRequestException("Trebuie introduse tarifele pentru BUSINESS, FIRST_CLASS și ECONOMY.");
        }
        seats.forEach((classType, seatCount) -> {
            if (seatCount == null || seatCount < 0) {
                throw new BadRequestException("Numărul de locuri pentru " + classType + " trebuie să fie pozitiv sau zero.");
            }
        });
        prices.forEach((classType, price) -> {
            if (price == null || price <= 0) {
                throw new BadRequestException("Tariful pentru " + classType + " trebuie să fie mai mare ca zero.");
            }
        });
    }

    private Flight findEditableFlight(Long airlineId, String flightCode) {
        Flight flight = findByCode(flightCode);
        if (flight.getAirline() == null || !flight.getAirline().getId().equals(airlineId)) {
            throw new BadRequestException("Nu poți edita un zbor care nu îți aparține.");
        }
        return flight;
    }

    private void validateUpdatedFlightCode(String currentFlightCode, String newFlightCode) {
        if (!currentFlightCode.equals(newFlightCode) && flightRepository.existsByFlightCode(newFlightCode)) {
            throw new BadRequestException("Există deja un zbor cu codul: " + newFlightCode);
        }
    }
}
