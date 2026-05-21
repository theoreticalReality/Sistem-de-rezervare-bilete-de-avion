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

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "discount_policy")
@Getter
@Setter
@NoArgsConstructor
public class DiscountPolicy {

    public static final double DEFAULT_ROUND_TRIP_DISCOUNT = 5.0;
    public static final double DEFAULT_LAST_MINUTE_DISCOUNT = 40.0;
    public static final long LAST_MINUTE_THRESHOLD_HOURS = 48;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "round_trip_discount_percent", nullable = false)
    private Double roundTripDiscountPercent = DEFAULT_ROUND_TRIP_DISCOUNT;

    @Column(name = "last_minute_discount_percent", nullable = false)
    private Double lastMinuteDiscountPercent = DEFAULT_LAST_MINUTE_DISCOUNT;

    public Double getRoundTripDiscount() {
        return roundTripDiscountPercent;
    }

    public Double getLastMinuteDiscount() {
        return lastMinuteDiscountPercent;
    }

    public void setRoundTripDiscount(Double value) {
        this.roundTripDiscountPercent = value;
    }

    public void setLastMinuteDiscount(Double value) {
        this.lastMinuteDiscountPercent = value;
    }

    public boolean isLastMinute(LocalDateTime flightDate) {
        if (flightDate == null) return false;
        Duration timeUntilDeparture = Duration.between(LocalDateTime.now(), flightDate);
        return !timeUntilDeparture.isNegative()
                && timeUntilDeparture.toHours() <= LAST_MINUTE_THRESHOLD_HOURS;
    }
}
