package com.ProiectIS.GestionareAeroport.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private boolean success;
    private String role;
    private String userId;
    private String displayName;
    private String message;
}
