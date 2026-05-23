package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.dto.BookingRequest;
import com.ProiectIS.GestionareAeroport.dto.BookingResponse;
import com.ProiectIS.GestionareAeroport.dto.PassengerDto;
import com.ProiectIS.GestionareAeroport.exception.BadRequestException;
import com.ProiectIS.GestionareAeroport.exception.NotFoundException;
import com.ProiectIS.GestionareAeroport.model.Booking;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.Passenger;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentMethod;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentStatus;
import com.ProiectIS.GestionareAeroport.repository.BookingRepository;
import com.ProiectIS.GestionareAeroport.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final PriceCalculator priceCalculator;

    public BookingService(BookingRepository bookingRepository,
                          FlightRepository flightRepository,
                          PriceCalculator priceCalculator) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.priceCalculator = priceCalculator;
    }

    @Transactional
    public Booking createBooking(BookingRequest request) {
        Flight outbound = loadFlight(request.getOutboundFlightId());
        Flight returnFlight = request.getReturnFlightId() == null ? null
                : loadFlight(request.getReturnFlightId());

        validateFlightAvailability(outbound, request.getOutboundDate(), request.getSelectedClass(), passengers(request.getPassenger()));
        LocalDateTime outboundDeparture = priceCalculator.resolveDeparture(outbound, request.getOutboundDate());
        if (outboundDeparture == null) {
            throw new BadRequestException("Zborul dus nu este disponibil în data selectată.");
        }

        LocalDateTime returnDeparture = null;
        if (returnFlight != null) {
            if (request.getReturnDate() == null) {
                throw new BadRequestException("Lipsește data zborului de întoarcere.");
            }
            validateFlightAvailability(returnFlight, request.getReturnDate(), request.getSelectedClass(), passengers(request.getPassenger()));
            returnDeparture = priceCalculator.resolveDeparture(returnFlight, request.getReturnDate());
            if (returnDeparture == null) {
                throw new BadRequestException("Zborul de întoarcere nu este disponibil în data selectată.");
            }
        }

        Booking booking = new Booking();
        booking.setBookingId(generateBookingId());
        booking.setPassenger(toPassenger(request.getPassenger()));
        booking.setOutboundFlight(outbound);
        booking.setReturnFlight(returnFlight);
        booking.setSelectedClass(request.getSelectedClass());
        booking.setMealIncluded(Boolean.TRUE.equals(request.getMealIncluded()));
        booking.setExtraLuggage(Boolean.TRUE.equals(request.getExtraLuggage()));
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setPaymentStatus(request.getPaymentMethod() == PaymentMethod.CASH
                ? PaymentStatus.PENDING : PaymentStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
        booking.setOutboundDeparture(outboundDeparture);
        booking.setReturnDeparture(returnDeparture);

        Double total = priceCalculator.calculateTotal(booking);
        booking.setTotalPrice(total);

        decrementSeats(outbound, request.getSelectedClass(), booking.getPassenger().getTotalPassengerCount());
        if (returnFlight != null) {
            decrementSeats(returnFlight, request.getSelectedClass(), booking.getPassenger().getTotalPassengerCount());
        }

        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking findByBookingId(String bookingId) {
        return bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new NotFoundException("Rezervarea nu există: " + bookingId));
    }

    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Booking> findByFlightCode(String flightCode) {
        return bookingRepository.findByOutboundFlight_FlightCodeOrReturnFlight_FlightCode(flightCode, flightCode);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findResponsesByFlightCode(String flightCode) {
        return bookingRepository.findByOutboundFlight_FlightCodeOrReturnFlight_FlightCode(flightCode, flightCode).stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    @Transactional
    public Booking confirmCashPayment(String bookingId) {
        Booking booking = findByBookingId(bookingId);
        if (booking.getPaymentMethod() != PaymentMethod.CASH) {
            throw new BadRequestException("Doar plățile cash necesită validare manuală.");
        }
        if (booking.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Această rezervare nu este în așteptarea plății.");
        }
        booking.confirmPayment();
        return bookingRepository.save(booking);
    }

    private Flight loadFlight(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Zborul nu există: " + id));
    }

    private void validateFlightAvailability(Flight flight, LocalDate date, ClassType classType, int passengerCount) {
        if (!flight.isAvailableOn(date)) {
            throw new BadRequestException("Zborul " + flight.getFlightCode() + " nu operează în data " + date + ".");
        }
        Integer available = flight.getAvailableSeats(classType);
        if (available == null || available < passengerCount) {
            throw new BadRequestException("Locuri insuficiente pe zborul " + flight.getFlightCode() + " la clasa " + classType + ".");
        }
    }

    private void decrementSeats(Flight flight, ClassType classType, int passengerCount) {
        int remaining = flight.getAvailableSeats(classType) - passengerCount;
        flight.getSeats().put(classType, remaining);
        flightRepository.save(flight);
    }

    private Passenger toPassenger(PassengerDto dto) {
        if (dto == null) return null;
        return new Passenger(
                dto.getName(),
                dto.getPhoneNumber(),
                dto.getNumberOfAdults() == null ? 0 : dto.getNumberOfAdults(),
                dto.getNumberOfChildren() == null ? 0 : dto.getNumberOfChildren(),
                dto.getNumberOfSeniors() == null ? 0 : dto.getNumberOfSeniors()
        );
    }

    private int passengers(PassengerDto dto) {
        if (dto == null) return 0;
        return (dto.getNumberOfAdults() == null ? 0 : dto.getNumberOfAdults())
                + (dto.getNumberOfChildren() == null ? 0 : dto.getNumberOfChildren())
                + (dto.getNumberOfSeniors() == null ? 0 : dto.getNumberOfSeniors());
    }

    private String generateBookingId() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
