package com.ProiectIS.GestionareAeroport.dto;

import com.ProiectIS.GestionareAeroport.model.PassengerDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDetailDto {

    private String type;
    private String name;
    private String phoneNumber;
    private String email;

    public static PassengerDetailDto fromEntity(PassengerDetail detail) {
        if (detail == null) {
            return null;
        }
        return new PassengerDetailDto(
                detail.getType(),
                detail.getName(),
                detail.getPhoneNumber(),
                detail.getEmail()
        );
    }
}
