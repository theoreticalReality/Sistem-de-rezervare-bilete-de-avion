package com.ProiectIS.GestionareAeroport.controller;

import com.ProiectIS.GestionareAeroport.dto.BookingRequest;
import com.ProiectIS.GestionareAeroport.dto.BookingResponse;
import com.ProiectIS.GestionareAeroport.dto.SearchQuery;
import com.ProiectIS.GestionareAeroport.dto.SearchResult;
import com.ProiectIS.GestionareAeroport.service.BookingService;
import com.ProiectIS.GestionareAeroport.service.FlightSearchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final FlightSearchService flightSearchService;
    private final BookingService bookingService;

    public PassengerController(FlightSearchService flightSearchService,
                               BookingService bookingService) {
        this.flightSearchService = flightSearchService;
        this.bookingService = bookingService;
    }

    @PostMapping("/flights/search")
    public SearchResult searchFlights(@Valid @RequestBody SearchQuery query) {
        return flightSearchService.search(query);
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        return BookingResponse.fromEntity(bookingService.createBooking(request));
    }
}
