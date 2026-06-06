package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.dto.CreateRegularFlightRequest;
import com.ProiectIS.GestionareAeroport.dto.CreateSeasonalFlightRequest;
import com.ProiectIS.GestionareAeroport.dto.FlightDto;
import com.ProiectIS.GestionareAeroport.dto.FlightOccupancyDto;
import com.ProiectIS.GestionareAeroport.exception.BadRequestException;
import com.ProiectIS.GestionareAeroport.exception.NotFoundException;
import com.ProiectIS.GestionareAeroport.model.AirlineCompany;
import com.ProiectIS.GestionareAeroport.model.Booking;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.PassengerDetail;
import com.ProiectIS.GestionareAeroport.model.RegularFlight;
import com.ProiectIS.GestionareAeroport.model.SeasonalFlight;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.repository.BookingRepository;
import com.ProiectIS.GestionareAeroport.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class FlightService {

    private final FlightRepository flightRepository;
    private final AirlineCompanyService airlineCompanyService;
    private final BookingRepository bookingRepository;

    public FlightService(FlightRepository flightRepository,
                         AirlineCompanyService airlineCompanyService,
                         BookingRepository bookingRepository) {
        this.flightRepository = flightRepository;
        this.airlineCompanyService = airlineCompanyService;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public RegularFlight addRegularFlight(Long airlineId, CreateRegularFlightRequest req) {
        if (flightRepository.existsByFlightCode(req.getFlightCode())) {
            throw new BadRequestException("Exista deja un zbor cu codul: " + req.getFlightCode());
        }
        validateFlightDetails(req.getSeats(), req.getPrices());
        validateFlightTimes(req.getDepartureTime(), req.getArrivalTime());
        AirlineCompany airline = airlineCompanyService.findById(airlineId);
        RegularFlight flight = new RegularFlight();
        flight.setFlightCode(req.getFlightCode());
        flight.setAirplaneModel(req.getAirplaneModel());
        flight.setDepartureCity(req.getDepartureCity());
        flight.setDestinationCity(req.getDestinationCity());
        flight.setSeats(req.getSeats());
        flight.setPrices(req.getPrices());
        flight.setDaysOfWeek(req.getDaysOfWeek());
        flight.setDepartureDate(req.getDepartureDate());
        flight.setDepartureTime(req.getDepartureTime());
        flight.setArrivalTime(req.getArrivalTime());
        airline.addFlight(flight);
        return flightRepository.save(flight);
    }

    @Transactional
    public SeasonalFlight addSeasonalFlight(Long airlineId, CreateSeasonalFlightRequest req) {
        if (flightRepository.existsByFlightCode(req.getFlightCode())) {
            throw new BadRequestException("Exista deja un zbor cu codul: " + req.getFlightCode());
        }
        validateFlightDetails(req.getSeats(), req.getPrices());
        validateFlightTimes(req.getDepartureTime(), req.getArrivalTime());
        validateSeasonalDate(req.getDepartureDate(), req.getSeasonStart(), req.getSeasonEnd());

        AirlineCompany airline = airlineCompanyService.findById(airlineId);
        SeasonalFlight flight = new SeasonalFlight();
        flight.setFlightCode(req.getFlightCode());
        flight.setAirplaneModel(req.getAirplaneModel());
        flight.setDepartureCity(req.getDepartureCity());
        flight.setDestinationCity(req.getDestinationCity());
        flight.setSeats(req.getSeats());
        flight.setPrices(req.getPrices());
        flight.setDaysOfWeek(req.getDaysOfWeek());
        flight.setDepartureDate(req.getDepartureDate());
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
        validateFlightTimes(req.getDepartureTime(), req.getArrivalTime());

        flight.setFlightCode(req.getFlightCode());
        flight.setAirplaneModel(req.getAirplaneModel());
        flight.setDepartureCity(req.getDepartureCity());
        flight.setDestinationCity(req.getDestinationCity());
        flight.setSeats(req.getSeats());
        flight.setPrices(req.getPrices());
        flight.setDaysOfWeek(req.getDaysOfWeek());
        flight.setDepartureDate(req.getDepartureDate());
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
        validateFlightTimes(req.getDepartureTime(), req.getArrivalTime());
        validateSeasonalDate(req.getDepartureDate(), req.getSeasonStart(), req.getSeasonEnd());

        flight.setFlightCode(req.getFlightCode());
        flight.setAirplaneModel(req.getAirplaneModel());
        flight.setDepartureCity(req.getDepartureCity());
        flight.setDestinationCity(req.getDestinationCity());
        flight.setSeats(req.getSeats());
        flight.setPrices(req.getPrices());
        flight.setDaysOfWeek(req.getDaysOfWeek());
        flight.setDepartureDate(req.getDepartureDate());
        flight.setDepartureTime(req.getDepartureTime());
        flight.setArrivalTime(req.getArrivalTime());
        flight.setSeasonStart(req.getSeasonStart());
        flight.setSeasonEnd(req.getSeasonEnd());
        return flightRepository.save(flight);
    }

    private void validateSeasonalDate(LocalDate departureDate, MonthDay start, MonthDay end) {
        if (departureDate == null || start == null || end == null) return;
        MonthDay md = MonthDay.from(departureDate);
        boolean isInSeason;
        if (!start.isAfter(end)) {
            isInSeason = !md.isBefore(start) && !md.isAfter(end);
        } else {
            isInSeason = !md.isBefore(start) || !md.isAfter(end);
        }
        if (!isInSeason) {
            throw new BadRequestException("Data de plecare (%s) este in afara sezonului definit (%s - %s)."
                    .formatted(departureDate, start, end));
        }
    }

    @Transactional
    public void removeFlight(Long airlineId, String flightCode) {
        Flight flight = flightRepository.findByFlightCode(flightCode)
                .orElseThrow(() -> new NotFoundException("Zborul nu exista: " + flightCode));
        if (flight.getAirline() == null || !flight.getAirline().getId().equals(airlineId)) {
            throw new BadRequestException("Nu poti sterge un zbor care nu iti apartine.");
        }
        if (bookingRepository.existsByOutboundFlight_IdOrReturnFlight_Id(flight.getId(), flight.getId())) {
            flight.setCancelled(true);
            flightRepository.save(flight);
            return;
        }
        flightRepository.delete(flight);
    }

    @Transactional(readOnly = true)
    public Flight findByCode(String flightCode) {
        return flightRepository.findByFlightCode(flightCode)
                .orElseThrow(() -> new NotFoundException("Zborul nu exista: " + flightCode));
    }

    @Transactional(readOnly = true)
    public Flight findById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Zborul nu exista: " + id));
    }

    @Transactional(readOnly = true)
    public FlightDto findDtoById(Long id, LocalDate date) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Zborul nu exista: " + id));
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
            throw new BadRequestException("Trebuie introdus numarul de locuri pentru BUSINESS, FIRST_CLASS si ECONOMY.");
        }
        if (prices == null || !prices.keySet().containsAll(requiredClasses)) {
            throw new BadRequestException("Trebuie introduse tarifele pentru BUSINESS, FIRST_CLASS si ECONOMY.");
        }
        seats.forEach((classType, seatCount) -> {
            if (seatCount == null || seatCount < 0) {
                throw new BadRequestException("Numarul de locuri pentru " + classType + " trebuie sa fie pozitiv sau zero.");
            }
        });
        prices.forEach((classType, price) -> {
            if (price == null || price <= 0) {
                throw new BadRequestException("Tariful pentru " + classType + " trebuie sa fie mai mare ca zero.");
            }
        });
    }

    private void validateFlightTimes(LocalTime departure, LocalTime arrival) {
        if (departure != null && arrival != null) {
            if (departure.equals(arrival)) {
                throw new BadRequestException("Ora de sosire nu poate fi identica cu ora de plecare.");
            }
            
            long depMinutes = departure.getHour() * 60L + departure.getMinute();
            long arrMinutes = arrival.getHour() * 60L + arrival.getMinute();
            
            long duration = arrMinutes - depMinutes;
            if (duration <= 0) {
                duration += 24 * 60;
            }
            
            if (duration > 1140) {
                throw new BadRequestException("Durata zborului este prea mare (>19h). Verifica orele de plecare si sosire.");
            }
        }
    }

    private Flight findEditableFlight(Long airlineId, String flightCode) {
        Flight flight = findByCode(flightCode);
        if (flight.getAirline() == null || !flight.getAirline().getId().equals(airlineId)) {
            throw new BadRequestException("Nu poti edita un zbor care nu iti apartine.");
        }
        return flight;
    }

    private void validateUpdatedFlightCode(String currentFlightCode, String newFlightCode) {
        if (!currentFlightCode.equals(newFlightCode) && flightRepository.existsByFlightCode(newFlightCode)) {
            throw new BadRequestException("Exista deja un zbor cu codul: " + newFlightCode);
        }
    }

    @Transactional(readOnly = true)
    public FlightOccupancyDto getFlightOccupancy(Long airlineId, String flightCode) {
        Flight flight = flightRepository.findByFlightCode(flightCode)
                .orElseThrow(() -> new NotFoundException("Zborul nu exista: " + flightCode));

        if (!flight.getAirline().getId().equals(airlineId)) {
            throw new BadRequestException("Nu aveti acces la datele acestui zbor.");
        }

        List<Booking> bookings = bookingRepository.findByOutboundFlight_FlightCodeOrReturnFlight_FlightCode(flightCode, flightCode);

        Map<ClassType, Integer> occupiedSeats = new HashMap<>();
        for (ClassType type : ClassType.values()) {
            occupiedSeats.put(type, 0);
        }

        int adults = 0, children = 0, seniors = 0;
        List<FlightOccupancyDto.PassengerManifestEntry> manifest = new ArrayList<>();

        for (Booking b : bookings) {
            boolean isOutbound = b.getOutboundFlight().getFlightCode().equals(flightCode);
            boolean isReturn = b.getReturnFlight() != null && b.getReturnFlight().getFlightCode().equals(flightCode);

            if (isOutbound || isReturn) {
                int passengerCount = b.getPassengerDetails().size();
                occupiedSeats.put(b.getSelectedClass(), occupiedSeats.get(b.getSelectedClass()) + passengerCount);

                for (PassengerDetail pd : b.getPassengerDetails()) {
                    if ("ADULT".equals(pd.getType())) adults++;
                    else if ("CHILD".equals(pd.getType())) children++;
                    else if ("SENIOR".equals(pd.getType())) seniors++;

                    manifest.add(FlightOccupancyDto.PassengerManifestEntry.builder()
                            .name(pd.getName())
                            .type(pd.getType())
                            .selectedClass(b.getSelectedClass())
                            .bookingId(b.getBookingId())
                            .email(pd.getEmail())
                            .build());
                }
            }
        }

        Map<ClassType, Integer> totalSeats = new HashMap<>();
        for (ClassType type : ClassType.values()) {
            totalSeats.put(type, flight.getSeats().getOrDefault(type, 0) + occupiedSeats.get(type));
        }

        return FlightOccupancyDto.builder()
                .flightCode(flight.getFlightCode())
                .airplaneModel(flight.getAirplaneModel())
                .route(flight.getDepartureCity() + " -> " + flight.getDestinationCity())
                .occupiedSeats(occupiedSeats)
                .totalSeats(totalSeats)
                .totalAdults(adults)
                .totalChildren(children)
                .totalSeniors(seniors)
                .manifest(manifest)
                .build();
    }
    }