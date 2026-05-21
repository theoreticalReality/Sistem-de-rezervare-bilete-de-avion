package com.ProiectIS.GestionareAeroport.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassengerDto {

    @NotBlank
    private String name;

    @NotBlank
    private String phoneNumber;

    @NotNull
    @Min(0)
    private Integer numberOfAdults = 0;

    @NotNull
    @Min(0)
    private Integer numberOfChildren = 0;

    @NotNull
    @Min(0)
    private Integer numberOfSeniors = 0;
}
