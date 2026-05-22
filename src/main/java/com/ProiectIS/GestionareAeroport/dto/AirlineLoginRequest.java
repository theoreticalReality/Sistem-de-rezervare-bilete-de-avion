package com.ProiectIS.GestionareAeroport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AirlineLoginRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
