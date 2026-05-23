package com.ProiectIS.GestionareAeroport.controller;

import com.ProiectIS.GestionareAeroport.dto.BookingResponse;
import com.ProiectIS.GestionareAeroport.dto.LoginResponse;
import com.ProiectIS.GestionareAeroport.dto.StaffLoginRequest;
import com.ProiectIS.GestionareAeroport.exception.UnauthorizedException;
import com.ProiectIS.GestionareAeroport.model.AirportStaff;
import com.ProiectIS.GestionareAeroport.repository.AirportStaffRepository;
import com.ProiectIS.GestionareAeroport.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final AirportStaffRepository staffRepository;
    private final BookingService bookingService;

    public StaffController(AirportStaffRepository staffRepository, BookingService bookingService) {
        this.staffRepository = staffRepository;
        this.bookingService = bookingService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody StaffLoginRequest request) {
        AirportStaff staff = staffRepository.findByPersonalCode(request.getPersonalCode())
                .orElseThrow(() -> new UnauthorizedException("Cod personal invalid."));
        
        return new LoginResponse(
                true,
                "STAFF",
                staff.getStaffId(),
                staff.getName(),
                "Autentificare reusita."
        );
    }

    @GetMapping("/flights/{flightCode}/bookings")
    public List<BookingResponse> getBookingsForFlight(@PathVariable String flightCode) {
        return bookingService.findResponsesByFlightCode(flightCode);
    }

    @PostMapping("/bookings/{bookingId}/confirm-payment")
    public BookingResponse confirmPayment(@PathVariable String bookingId) {
        return BookingResponse.fromEntity(bookingService.confirmCashPayment(bookingId));
    }
}
