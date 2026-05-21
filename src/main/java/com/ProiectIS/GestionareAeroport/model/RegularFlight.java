package com.ProiectIS.GestionareAeroport.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("REGULAR")
@Getter
@Setter
@NoArgsConstructor
public class RegularFlight extends Flight {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "regular_flight_days", joinColumns = @JoinColumn(name = "flight_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> daysOfWeek = new ArrayList<>();

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Override
    public boolean isAvailableOn(LocalDate date) {
        if (date == null || daysOfWeek == null) return false;
        return daysOfWeek.contains(date.getDayOfWeek());
    }

    public LocalDateTime getDepartureDateTime(LocalDate date) {
        if (!isAvailableOn(date) || departureTime == null) return null;
        return LocalDateTime.of(date, departureTime);
    }
}
