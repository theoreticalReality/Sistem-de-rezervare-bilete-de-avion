package com.ProiectIS.GestionareAeroport.dto;

import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CreateRegularFlightRequest {

    @NotBlank
    private String flightCode;

    @NotBlank
    private String airplaneModel;

    @NotBlank
    private String departureCity;

    @NotBlank
    private String destinationCity;

    @NotEmpty
    private Map<ClassType, Integer> seats;

    @NotEmpty
    private Map<ClassType, Double> prices;

    @NotEmpty
    private List<DayOfWeek> daysOfWeek;

    @NotNull
    private LocalTime departureTime;

    @NotNull
    private LocalTime arrivalTime;
}
