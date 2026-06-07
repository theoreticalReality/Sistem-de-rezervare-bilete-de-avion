package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.dto.CreateRegularFlightRequest;
import com.ProiectIS.GestionareAeroport.dto.CreateSeasonalFlightRequest;
import com.ProiectIS.GestionareAeroport.exception.BadRequestException;
import com.ProiectIS.GestionareAeroport.exception.NotFoundException;
import com.ProiectIS.GestionareAeroport.exception.UnauthorizedException;
import com.ProiectIS.GestionareAeroport.model.AirlineCompany;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.RegularFlight;
import com.ProiectIS.GestionareAeroport.model.SeasonalFlight;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.repository.AirlineCompanyRepository;
import com.ProiectIS.GestionareAeroport.repository.BookingRepository;
import com.ProiectIS.GestionareAeroport.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirlineRequirementTest {

    private final AirlineCompanyRepository airlineRepository = mock(AirlineCompanyRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AirlineCompanyService airlineCompanyService = new AirlineCompanyService(airlineRepository, passwordEncoder);

    private final FlightRepository flightRepository = mock(FlightRepository.class);
    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final FlightService flightService = new FlightService(flightRepository, airlineCompanyService, bookingRepository);

    @Test
    void airlineCanLoginWithEmailAndCorrectPassword() {
        AirlineCompany company = airline(1L, "AIR-01", "air@test.ro", "hash");
        when(airlineRepository.findByEmail("air@test.ro")).thenReturn(Optional.of(company));
        when(passwordEncoder.matches("secret123", "hash")).thenReturn(true);

        AirlineCompany result = airlineCompanyService.login("air@test.ro", "secret123");

        assertThat(result).isSameAs(company);
    }

    @Test
    void airlineCanLoginWithCompanyIdWhenEmailIsNotFound() {
        AirlineCompany company = airline(1L, "AIR-01", "air@test.ro", "hash");
        when(airlineRepository.findByEmail("AIR-01")).thenReturn(Optional.empty());
        when(airlineRepository.findByCompanyId("AIR-01")).thenReturn(Optional.of(company));
        when(passwordEncoder.matches("secret123", "hash")).thenReturn(true);

        AirlineCompany result = airlineCompanyService.login("  AIR-01  ", "secret123");

        assertThat(result.getCompanyId()).isEqualTo("AIR-01");
    }

    @Test
    void airlineLoginRejectsWrongPassword() {
        AirlineCompany company = airline(1L, "AIR-01", "air@test.ro", "hash");
        when(airlineRepository.findByEmail("air@test.ro")).thenReturn(Optional.of(company));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> airlineCompanyService.login("air@test.ro", "wrong"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void addRegularFlightStoresAllRequiredFlightDetailsForAirline() {
        AirlineCompany company = airline(10L, "AIR-10", "air10@test.ro", "hash");
        CreateRegularFlightRequest request = regularRequest("RO101");
        when(flightRepository.existsByFlightCode("RO101")).thenReturn(false);
        when(airlineRepository.findById(10L)).thenReturn(Optional.of(company));
        when(flightRepository.save(any(RegularFlight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegularFlight result = flightService.addRegularFlight(10L, request);

        assertThat(result.getFlightCode()).isEqualTo("RO101");
        assertThat(result.getAirplaneModel()).isEqualTo("Boeing 737");
        assertThat(result.getSeats()).containsEntry(ClassType.BUSINESS, 8)
                .containsEntry(ClassType.FIRST_CLASS, 12)
                .containsEntry(ClassType.ECONOMY, 120);
        assertThat(result.getPrices()).containsEntry(ClassType.BUSINESS, 900.0)
                .containsEntry(ClassType.FIRST_CLASS, 600.0)
                .containsEntry(ClassType.ECONOMY, 250.0);
        assertThat(result.getDaysOfWeek()).containsExactly(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        assertThat(result.getDepartureTime()).isEqualTo(LocalTime.of(8, 30));
        assertThat(result.getAirline()).isSameAs(company);
    }

    @Test
    void addSeasonalFlightStoresSeasonPeriodInAdditionToRegularFlightDetails() {
        AirlineCompany company = airline(10L, "AIR-10", "air10@test.ro", "hash");
        CreateSeasonalFlightRequest request = seasonalRequest("SUN77");
        when(flightRepository.existsByFlightCode("SUN77")).thenReturn(false);
        when(airlineRepository.findById(10L)).thenReturn(Optional.of(company));
        when(flightRepository.save(any(SeasonalFlight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SeasonalFlight result = flightService.addSeasonalFlight(10L, request);

        assertThat(result.getFlightCode()).isEqualTo("SUN77");
        assertThat(result.getSeasonStart()).isEqualTo(MonthDay.of(6, 1));
        assertThat(result.getSeasonEnd()).isEqualTo(MonthDay.of(8, 31));
        assertThat(result.isAvailableOn(java.time.LocalDate.of(2026, 6, 1))).isTrue();
        assertThat(result.isAvailableOn(java.time.LocalDate.of(2026, 9, 7))).isFalse();
    }

    @Test
    void addFlightRejectsDuplicateFlightCode() {
        CreateRegularFlightRequest request = regularRequest("RO101");
        when(flightRepository.existsByFlightCode("RO101")).thenReturn(true);

        assertThatThrownBy(() -> flightService.addRegularFlight(10L, request))
                .isInstanceOf(BadRequestException.class);

        verify(flightRepository, never()).save(any());
    }

    @Test
    void addFlightRejectsMissingSeatClass() {
        CreateRegularFlightRequest request = regularRequest("RO101");
        request.getSeats().remove(ClassType.FIRST_CLASS);
        when(flightRepository.existsByFlightCode("RO101")).thenReturn(false);

        assertThatThrownBy(() -> flightService.addRegularFlight(10L, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void addFlightRejectsInvalidTariff() {
        CreateRegularFlightRequest request = regularRequest("RO101");
        request.getPrices().put(ClassType.ECONOMY, 0.0);
        when(flightRepository.existsByFlightCode("RO101")).thenReturn(false);

        assertThatThrownBy(() -> flightService.addRegularFlight(10L, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void removeFlightDeletesOnlyFlightOwnedByAirline() {
        RegularFlight flight = new RegularFlight();
        flight.setFlightCode("RO101");
        flight.setAirline(airline(10L, "AIR-10", "air10@test.ro", "hash"));
        when(flightRepository.findByFlightCode("RO101")).thenReturn(Optional.of(flight));

        flightService.removeFlight(10L, "RO101");

        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository).delete(captor.capture());
        assertThat(captor.getValue().getFlightCode()).isEqualTo("RO101");
    }

    @Test
    void removeFlightRejectsFlightOwnedByAnotherAirline() {
        RegularFlight flight = new RegularFlight();
        flight.setFlightCode("RO101");
        flight.setAirline(airline(99L, "AIR-99", "air99@test.ro", "hash"));
        when(flightRepository.findByFlightCode("RO101")).thenReturn(Optional.of(flight));

        assertThatThrownBy(() -> flightService.removeFlight(10L, "RO101"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void removeFlightRejectsUnknownFlightCode() {
        when(flightRepository.findByFlightCode("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.removeFlight(10L, "MISSING"))
                .isInstanceOf(NotFoundException.class);
    }

    private AirlineCompany airline(Long id, String companyId, String email, String passwordHash) {
        AirlineCompany company = new AirlineCompany();
        company.setId(id);
        company.setCompanyId(companyId);
        company.setName("Test Air");
        company.setEmail(email);
        company.setPasswordHash(passwordHash);
        return company;
    }

    private CreateRegularFlightRequest regularRequest(String flightCode) {
        CreateRegularFlightRequest request = new CreateRegularFlightRequest();
        request.setFlightCode(flightCode);
        request.setAirplaneModel("Boeing 737");
        request.setDepartureCity("Bucuresti");
        request.setDestinationCity("Paris");
        request.setSeats(seats());
        request.setPrices(prices());
        request.setDaysOfWeek(List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
        request.setDepartureTime(LocalTime.of(8, 30));
        request.setArrivalTime(LocalTime.of(11, 10));
        return request;
    }

    private CreateSeasonalFlightRequest seasonalRequest(String flightCode) {
        CreateSeasonalFlightRequest request = new CreateSeasonalFlightRequest();
        request.setFlightCode(flightCode);
        request.setAirplaneModel("Airbus A320");
        request.setDepartureCity("Bucuresti");
        request.setDestinationCity("Malaga");
        request.setSeats(seats());
        request.setPrices(prices());
        request.setDaysOfWeek(List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
        request.setDepartureTime(LocalTime.of(6, 15));
        request.setArrivalTime(LocalTime.of(9, 45));
        request.setSeasonStart(MonthDay.of(6, 1));
        request.setSeasonEnd(MonthDay.of(8, 31));
        return request;
    }

    private Map<ClassType, Integer> seats() {
        Map<ClassType, Integer> seats = new EnumMap<>(ClassType.class);
        seats.put(ClassType.BUSINESS, 8);
        seats.put(ClassType.FIRST_CLASS, 12);
        seats.put(ClassType.ECONOMY, 120);
        return seats;
    }

    private Map<ClassType, Double> prices() {
        Map<ClassType, Double> prices = new EnumMap<>(ClassType.class);
        prices.put(ClassType.BUSINESS, 900.0);
        prices.put(ClassType.FIRST_CLASS, 600.0);
        prices.put(ClassType.ECONOMY, 250.0);
        return prices;
    }
}