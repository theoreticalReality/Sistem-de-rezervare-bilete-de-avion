package com.ProiectIS.GestionareAeroport.dto;

import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BookingRequest {

    @NotNull
    @Valid
    private PassengerDto passenger;

    @NotNull
    private Long outboundFlightId;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate outboundDate;

    private Long returnFlightId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    @NotNull
    private ClassType selectedClass;

    private Boolean mealIncluded = false;

    private Boolean extraLuggage = false;

    @NotNull
    private PaymentMethod paymentMethod;

    @Valid
    private List<PassengerDetailDto> passengerDetails = new ArrayList<>();
}
