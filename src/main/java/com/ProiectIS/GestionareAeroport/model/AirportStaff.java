package com.ProiectIS.GestionareAeroport.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "airport_staff")
@Getter
@Setter
@NoArgsConstructor
public class AirportStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_id", nullable = false, unique = true)
    private String staffId;

    @Column(name = "personal_code", nullable = false, unique = true)
    private String personalCode;

    @Column(nullable = false)
    private String name;
}
