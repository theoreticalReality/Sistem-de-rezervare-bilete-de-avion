package com.ProiectIS.GestionareAeroport.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {

    @Column(name = "passenger_name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "number_of_adults", nullable = false)
    private Integer numberOfAdults = 0;

    @Column(name = "number_of_children", nullable = false)
    private Integer numberOfChildren = 0;

    @Column(name = "number_of_seniors", nullable = false)
    private Integer numberOfSeniors = 0;

    public Integer getTotalPassengerCount() {
        int adults = numberOfAdults == null ? 0 : numberOfAdults;
        int children = numberOfChildren == null ? 0 : numberOfChildren;
        int seniors = numberOfSeniors == null ? 0 : numberOfSeniors;
        return adults + children + seniors;
    }
}
