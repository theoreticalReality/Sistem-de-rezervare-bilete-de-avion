package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.controller.StaffController;
import com.ProiectIS.GestionareAeroport.dto.BookingResponse;
import com.ProiectIS.GestionareAeroport.dto.LoginResponse;
import com.ProiectIS.GestionareAeroport.dto.StaffLoginRequest;
import com.ProiectIS.GestionareAeroport.exception.BadRequestException;
import com.ProiectIS.GestionareAeroport.exception.NotFoundException;
import com.ProiectIS.GestionareAeroport.exception.UnauthorizedException;
import com.ProiectIS.GestionareAeroport.model.AirportStaff;
import com.ProiectIS.GestionareAeroport.model.Booking;
import com.ProiectIS.GestionareAeroport.model.Passenger;
import com.ProiectIS.GestionareAeroport.model.RegularFlight;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentMethod;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentStatus;
import com.ProiectIS.GestionareAeroport.repository.AirportStaffRepository;
import com.ProiectIS.GestionareAeroport.repository.BookingRepository;
import com.ProiectIS.GestionareAeroport.repository.FlightRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StaffRequirementTest {

    private final AirportStaffRepository staffRepository = mock(AirportStaffRepository.class);
    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final FlightRepository flightRepository = mock(FlightRepository.class);
    private final PriceCalculator priceCalculator = mock(PriceCalculator.class);
    private final BookingService bookingService = new BookingService(bookingRepository, flightRepository, priceCalculator);
    private final StaffController staffController = new StaffController(staffRepository, bookingService);

    @Test
    void staffCanLoginWithPersonalCode() {
        AirportStaff staff = staff("ST-01", "CP123", "Ana Pop");
        when(staffRepository.findByPersonalCode("CP123")).thenReturn(Optional.of(staff));

        LoginResponse response = staffController.login(staffLogin("CP123"));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getRole()).isEqualTo("STAFF");
        assertThat(response.getUserId()).isEqualTo("ST-01");
        assertThat(response.getDisplayName()).isEqualTo("Ana Pop");
    }

    @Test
    void staffLoginRejectsInvalidPersonalCode() {
        when(staffRepository.findByPersonalCode("BAD")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffController.login(staffLogin("BAD")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void staffCanSeeBookingsForSpecificOutboundFlight() {
        Booking booking = booking("BK-OUT", "RO101", null, PaymentMethod.CASH, PaymentStatus.PENDING);
        when(bookingRepository.findByOutboundFlight_FlightCodeOrReturnFlight_FlightCode("RO101", "RO101"))
                .thenReturn(List.of(booking));

        List<BookingResponse> responses = staffController.getBookingsForFlight("RO101");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getBookingId()).isEqualTo("BK-OUT");
        assertThat(responses.get(0).getOutboundFlightCode()).isEqualTo("RO101");
        assertThat(responses.get(0).getPassengerName()).isEqualTo("Ion Ionescu");
    }

    @Test
    void staffCanSeeBookingsWhereFlightIsReturnFlight() {
        Booking booking = booking("BK-RET", "RO100", "RO101", PaymentMethod.CASH, PaymentStatus.PENDING);
        when(bookingRepository.findByOutboundFlight_FlightCodeOrReturnFlight_FlightCode("RO101", "RO101"))
                .thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.findByFlightCode("RO101");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getReturnFlight().getFlightCode()).isEqualTo("RO101");
    }

    @Test
    void staffGetsEmptyListWhenFlightHasNoBookings() {
        when(bookingRepository.findByOutboundFlight_FlightCodeOrReturnFlight_FlightCode("EMPTY", "EMPTY"))
                .thenReturn(List.of());

        List<BookingResponse> responses = staffController.getBookingsForFlight("EMPTY");

        assertThat(responses).isEmpty();
    }

    @Test
    void confirmCashPaymentChangesPendingBookingToConfirmed() {
        Booking booking = booking("BK-CASH", "RO101", null, PaymentMethod.CASH, PaymentStatus.PENDING);
        when(bookingRepository.findByBookingId("BK-CASH")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        Booking confirmed = bookingService.confirmCashPayment("BK-CASH");

        assertThat(confirmed.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void staffControllerReturnsConfirmedBookingAfterCashValidation() {
        Booking booking = booking("BK-CASH", "RO101", null, PaymentMethod.CASH, PaymentStatus.PENDING);
        when(bookingRepository.findByBookingId("BK-CASH")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponse response = staffController.confirmPayment("BK-CASH");

        assertThat(response.getBookingId()).isEqualTo("BK-CASH");
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);
    }

    @Test
    void confirmCashPaymentRejectsCardPayment() {
        Booking booking = booking("BK-CARD", "RO101", null, PaymentMethod.CARD, PaymentStatus.CONFIRMED);
        when(bookingRepository.findByBookingId("BK-CARD")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmCashPayment("BK-CARD"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void confirmCashPaymentRejectsAlreadyConfirmedCashPayment() {
        Booking booking = booking("BK-DONE", "RO101", null, PaymentMethod.CASH, PaymentStatus.CONFIRMED);
        when(bookingRepository.findByBookingId("BK-DONE")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmCashPayment("BK-DONE"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void confirmCashPaymentRejectsUnknownBookingId() {
        when(bookingRepository.findByBookingId("BK-MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.confirmCashPayment("BK-MISSING"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void confirmCashPaymentVerifiesRepositorySaveIsCalled() {
        Booking booking = booking("BK-SAVE", "RO101", null, PaymentMethod.CASH, PaymentStatus.PENDING);
        when(bookingRepository.findByBookingId("BK-SAVE")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.confirmCashPayment("BK-SAVE");

        verify(bookingRepository).save(booking);
    }

    @Test
    void confirmCashPaymentRejectsNullBookingId() {
        assertThatThrownBy(() -> bookingService.confirmCashPayment(null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void confirmCashPaymentHandlesCancelledBookingsIfTheyExist() {
        Booking booking = booking("BK-CANCEL", "RO101", null, PaymentMethod.CASH, PaymentStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.CONFIRMED); 
        when(bookingRepository.findByBookingId("BK-CANCEL")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmCashPayment("BK-CANCEL"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void confirmCashPaymentChangesStatusToConfirmedOnly() {
        Booking booking = booking("BK-STATUS", "RO101", null, PaymentMethod.CASH, PaymentStatus.PENDING);
        when(bookingRepository.findByBookingId("BK-STATUS")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Booking result = bookingService.confirmCashPayment("BK-STATUS");

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);
    }

    @Test
    void confirmCashPaymentWorksForBookingsWithReturnFlight() {
        Booking booking = booking("BK-RET-CASH", "RO101", "RO102", PaymentMethod.CASH, PaymentStatus.PENDING);
        when(bookingRepository.findByBookingId("BK-RET-CASH")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.confirmCashPayment("BK-RET-CASH");

        assertThat(result.getBookingId()).isEqualTo("BK-RET-CASH");
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);
    }

    private StaffLoginRequest staffLogin(String personalCode) {
        StaffLoginRequest request = new StaffLoginRequest();
        request.setPersonalCode(personalCode);
        return request;
    }

    private AirportStaff staff(String staffId, String personalCode, String name) {
        AirportStaff staff = new AirportStaff();
        staff.setStaffId(staffId);
        staff.setPersonalCode(personalCode);
        staff.setName(name);
        return staff;
    }

    private Booking booking(String bookingId,
                            String outboundFlightCode,
                            String returnFlightCode,
                            PaymentMethod paymentMethod,
                            PaymentStatus paymentStatus) {
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setPassenger(new Passenger("Ion Ionescu", "0712345678", 1, 1, 0));
        booking.setOutboundFlight(flight(outboundFlightCode));
        booking.setReturnFlight(returnFlightCode == null ? null : flight(returnFlightCode));
        booking.setSelectedClass(ClassType.ECONOMY);
        booking.setMealIncluded(false);
        booking.setExtraLuggage(false);
        booking.setPaymentMethod(paymentMethod);
        booking.setPaymentStatus(paymentStatus);
        booking.setBookingDate(LocalDateTime.of(2026, 6, 1, 10, 0));
        booking.setOutboundDeparture(LocalDateTime.of(2026, 6, 3, 8, 30));
        booking.setBasePrice(500.0);
        booking.setExtrasPrice(0.0);
        booking.setDiscountAmount(0.0);
        booking.setRoundTripDiscountApplied(false);
        booking.setTotalPrice(500.0);
        return booking;
    }

    private RegularFlight flight(String flightCode) {
        RegularFlight flight = new RegularFlight();
        flight.setFlightCode(flightCode);
        flight.setAirplaneModel("Boeing 737");
        flight.setDepartureCity("Bucuresti");
        flight.setDestinationCity("Paris");
        return flight;
    }
}
