package com.ProiectIS.GestionareAeroport.dto;

import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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
    private LocalDate outboundDate;

    private Long returnFlightId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate returnDate;

    @NotNull
    private ClassType selectedClass;

    private Boolean mealIncluded = false;

    private Boolean extraLuggage = false;

    @NotNull
    private PaymentMethod paymentMethod;
}
