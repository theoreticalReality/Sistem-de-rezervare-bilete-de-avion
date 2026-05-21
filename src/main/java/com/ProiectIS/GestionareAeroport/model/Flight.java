package com.ProiectIS.GestionareAeroport.model;

import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "flights")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "flight_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_code", nullable = false, unique = true)
    private String flightCode;

    @Column(name = "airplane_model", nullable = false)
    private String airplaneModel;

    @Column(name = "departure_city", nullable = false)
    private String departureCity;

    @Column(name = "destination_city", nullable = false)
    private String destinationCity;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "flight_seats", joinColumns = @JoinColumn(name = "flight_id"))
    @MapKeyColumn(name = "class_type")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "seats")
    private Map<ClassType, Integer> seats = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "flight_prices", joinColumns = @JoinColumn(name = "flight_id"))
    @MapKeyColumn(name = "class_type")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "price")
    private Map<ClassType, Double> prices = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", nullable = false)
    private AirlineCompany airline;

    public Integer getAvailableSeats(ClassType classType) {
        return seats.getOrDefault(classType, 0);
    }

    public Double getPriceFor(ClassType classType) {
        return prices.get(classType);
    }

    public abstract boolean isAvailableOn(LocalDate date);
}
