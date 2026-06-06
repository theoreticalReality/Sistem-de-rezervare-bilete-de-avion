package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.dto.BookingRequest;
import com.ProiectIS.GestionareAeroport.dto.BookingResponse;
import com.ProiectIS.GestionareAeroport.dto.PassengerDetailDto;
import com.ProiectIS.GestionareAeroport.dto.PassengerDto;
import com.ProiectIS.GestionareAeroport.dto.PriceQuote;
import com.ProiectIS.GestionareAeroport.exception.BadRequestException;
import com.ProiectIS.GestionareAeroport.exception.NotFoundException;
import com.ProiectIS.GestionareAeroport.model.Booking;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.Passenger;
import com.ProiectIS.GestionareAeroport.model.PassengerDetail;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentMethod;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentStatus;
import com.ProiectIS.GestionareAeroport.repository.BookingRepository;
import com.ProiectIS.GestionareAeroport.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        int passengerCount = passengers(request.getPassenger());
        if (passengerCount <= 0) {
            throw new BadRequestException("Trebuie selectat cel putin un pasager.");
        }
        if (request.getSelectedClass() == null) {
            throw new BadRequestException("Trebuie selectata clasa zborului.");
        }
        if (request.getPaymentMethod() == null) {
            throw new BadRequestException("Trebuie selectata metoda de plata.");
        }
        List<PassengerDetail> passengerDetails = validateAndBuildPassengerDetails(request, passengerCount);
        Passenger passengerSummary = buildPassengerSummary(request.getPassenger(), passengerDetails);

        Flight outbound = loadFlight(request.getOutboundFlightId());
        Flight returnFlight = request.getReturnFlightId() == null ? null
                : loadFlight(request.getReturnFlightId());

        validateFlightAvailability(outbound, request.getOutboundDate(), request.getSelectedClass(), passengerCount);
        LocalDateTime outboundDeparture = priceCalculator.resolveDeparture(outbound, request.getOutboundDate());
        if (outboundDeparture == null) {
            throw new BadRequestException("Zborul dus nu este disponibil in data selectata.");
        }

        LocalDateTime returnDeparture = null;
        if (returnFlight != null) {
            if (request.getReturnDate() == null) {
                throw new BadRequestException("Lipseste data zborului de intoarcere.");
            }
            validateFlightAvailability(returnFlight, request.getReturnDate(), request.getSelectedClass(), passengerCount);
            returnDeparture = priceCalculator.resolveDeparture(returnFlight, request.getReturnDate());
            if (returnDeparture == null) {
                throw new BadRequestException("Zborul de intoarcere nu este disponibil in data selectata.");
            }
        }

        Booking booking = new Booking();
        booking.setBookingId(generateBookingId());
        booking.setPassenger(passengerSummary);
        booking.setPassengerDetails(passengerDetails);
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

        PriceQuote quote = priceCalculator.quote(
                outbound,
                returnFlight,
                outboundDeparture,
                returnDeparture,
                request.getSelectedClass(),
                booking.getPassenger().getTotalPassengerCount(),
                booking.getMealIncluded(),
                booking.getExtraLuggage()
        );
        booking.setBasePrice(quote.getBasePrice());
        booking.setExtrasPrice(quote.getExtrasPrice());
        booking.setDiscountAmount(quote.getDiscountAmount());
        booking.setRoundTripDiscountApplied(quote.isRoundTripDiscountApplied());
        booking.setLastMinuteDiscountApplied(quote.isLastMinuteDiscountApplied());
        booking.setTotalPrice(quote.getTotalPrice());

        decrementSeats(outbound, request.getSelectedClass(), booking.getPassenger().getTotalPassengerCount());
        if (returnFlight != null) {
            decrementSeats(returnFlight, request.getSelectedClass(), booking.getPassenger().getTotalPassengerCount());
        }

        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking findByBookingId(String bookingId) {
        return bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new NotFoundException("Rezervarea nu exista: " + bookingId));
    }

    @Transactional(readOnly = true)
    public BookingResponse findResponseByBookingId(String bookingId) {
        Booking booking = findByBookingId(bookingId);
        return BookingResponse.fromEntity(booking);
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

    @Transactional(readOnly = true)
    public List<BookingResponse> findResponsesByAirline(Long airlineId) {
        return bookingRepository.findByOutboundFlight_Airline_IdOrReturnFlight_Airline_Id(airlineId, airlineId).stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    @Transactional
    public Booking confirmCashPayment(String bookingId) {
        Booking booking = findByBookingId(bookingId);
        if (booking.getPaymentMethod() != PaymentMethod.CASH) {
            throw new BadRequestException("Doar platile cash necesita validare manuala.");
        }
        if (booking.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Aceasta rezervare nu este in asteptarea platii.");
        }
        booking.confirmPayment();
        return bookingRepository.save(booking);
    }

    private Flight loadFlight(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Zborul nu exista: " + id));
        // Force initialization of airline to avoid LazyInitializationException outside transaction
        if (flight.getAirline() != null) {
            flight.getAirline().getName();
        }
        return flight;
    }

    private void validateFlightAvailability(Flight flight, LocalDate date, ClassType classType, int passengerCount) {
        if (flight.isCancelled()) {
            throw new BadRequestException("Zborul " + flight.getFlightCode() + " este anulat si nu mai poate fi rezervat.");
        }
        if (!flight.isAvailableOn(date)) {
            throw new BadRequestException("Zborul " + flight.getFlightCode() + " nu opereaza in data " + date + ".");
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

    private Passenger buildPassengerSummary(PassengerDto dto, List<PassengerDetail> details) {
        Passenger passenger = toPassenger(dto);
        PassengerDetail lead = details.stream()
                .filter(detail -> !isBlank(detail.getPhoneNumber()))
                .findFirst()
                .orElse(details.isEmpty() ? null : details.get(0));
        if (lead != null) {
            passenger.setName(lead.getName());
            passenger.setPhoneNumber(lead.getPhoneNumber() == null ? "" : lead.getPhoneNumber());
        }
        return passenger;
    }

    private List<PassengerDetail> validateAndBuildPassengerDetails(BookingRequest request, int expectedCount) {
        List<PassengerDetailDto> details = request.getPassengerDetails();
        if (details == null || details.size() != expectedCount) {
            throw new BadRequestException("Completeaza datele pentru fiecare pasager.");
        }

        int expectedAdults = zeroIfNull(request.getPassenger().getNumberOfAdults());
        int expectedChildren = zeroIfNull(request.getPassenger().getNumberOfChildren());
        int expectedSeniors = zeroIfNull(request.getPassenger().getNumberOfSeniors());
        int adults = 0;
        int children = 0;
        int seniors = 0;
        List<PassengerDetail> passengerDetails = new ArrayList<>();

        for (PassengerDetailDto detail : details) {
            String type = normalizeType(detail.getType());
            if ("ADULT".equals(type)) adults++;
            if ("CHILD".equals(type)) children++;
            if ("SENIOR".equals(type)) seniors++;

            String name = detail.getName() == null ? "" : detail.getName().trim();
            if (isBlank(name)) {
                throw new BadRequestException("Numele este obligatoriu pentru fiecare pasager.");
            }
            if (!name.matches("^[A-Za-zÀ-ÿ\\s\\-]+$")) {
                throw new BadRequestException("Numele pasagerului '" + name + "' poate contine doar litere.");
            }

            String phone = isBlank(detail.getPhoneNumber()) ? null : detail.getPhoneNumber().trim();
            String email = isBlank(detail.getEmail()) ? null : detail.getEmail().trim();

            if (requiresContact(type)) {
                if (isBlank(phone) || isBlank(email)) {
                    throw new BadRequestException("Adultii si seniorii trebuie sa aiba telefon si email.");
                }
                if (!phone.matches("^[0-9\\+\\s]{10,15}$")) {
                    throw new BadRequestException("Numarul de telefon '" + phone + "' este invalid (trebuie sa aiba 10-15 cifre).");
                }
                if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                    throw new BadRequestException("Adresa de email '" + email + "' este invalida.");
                }
            }

            passengerDetails.add(new PassengerDetail(
                    type,
                    name,
                    phone,
                    email
            ));
        }

        if (adults != expectedAdults || children != expectedChildren || seniors != expectedSeniors) {
            throw new BadRequestException("Numarul de pasageri completati nu corespunde cu adultii, copiii si seniorii selectati.");
        }

        return passengerDetails;
    }

    private boolean requiresContact(String type) {
        return "ADULT".equals(type) || "SENIOR".equals(type);
    }

    private String normalizeType(String type) {
        if (type == null) {
            throw new BadRequestException("Tipul pasagerului este obligatoriu.");
        }
        String normalized = type.trim().toUpperCase();
        if (!List.of("ADULT", "CHILD", "SENIOR").contains(normalized)) {
            throw new BadRequestException("Tip pasager invalid.");
        }
        return normalized;
    }

    private int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private int passengers(PassengerDto dto) {
        if (dto == null) return 0;
        return (dto.getNumberOfAdults() == null ? 0 : dto.getNumberOfAdults())
                + (dto.getNumberOfChildren() == null ? 0 : dto.getNumberOfChildren())
                + (dto.getNumberOfSeniors() == null ? 0 : dto.getNumberOfSeniors());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String generateBookingId() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
