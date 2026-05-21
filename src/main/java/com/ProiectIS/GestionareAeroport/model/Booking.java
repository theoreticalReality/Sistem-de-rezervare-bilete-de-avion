package com.ProiectIS.GestionareAeroport.model;

import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentMethod;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private String bookingId;

    @Embedded
    private Passenger passenger;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "outbound_flight_id", nullable = false)
    private Flight outboundFlight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_flight_id")
    private Flight returnFlight;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_class", nullable = false)
    private ClassType selectedClass;

    @Column(name = "meal_included", nullable = false)
    private Boolean mealIncluded = false;

    @Column(name = "extra_luggage", nullable = false)
    private Boolean extraLuggage = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "outbound_departure")
    private LocalDateTime outboundDeparture;

    @Column(name = "return_departure")
    private LocalDateTime returnDeparture;

    public boolean isRoundTrip() {
        return returnFlight != null;
    }

    public void confirmPayment() {
        this.paymentStatus = PaymentStatus.CONFIRMED;
    }
}
