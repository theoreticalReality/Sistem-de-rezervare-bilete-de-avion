package com.ProiectIS.GestionareAeroport.dto;

import com.ProiectIS.GestionareAeroport.model.Booking;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentMethod;
import com.ProiectIS.GestionareAeroport.model.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private String bookingId;
    private String passengerName;
    private String phoneNumber;
    private Integer totalPassengers;
    private String outboundFlightCode;
    private String returnFlightCode;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime outboundDeparture;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime returnDeparture;
    private ClassType selectedClass;
    private Boolean mealIncluded;
    private Boolean extraLuggage;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime bookingDate;
    private Double totalPrice;

    public static BookingResponse fromEntity(Booking b) {
        return BookingResponse.builder()
                .bookingId(b.getBookingId())
                .passengerName(b.getPassenger() != null ? b.getPassenger().getName() : null)
                .phoneNumber(b.getPassenger() != null ? b.getPassenger().getPhoneNumber() : null)
                .totalPassengers(b.getPassenger() != null ? b.getPassenger().getTotalPassengerCount() : null)
                .outboundFlightCode(b.getOutboundFlight() != null ? b.getOutboundFlight().getFlightCode() : null)
                .returnFlightCode(b.getReturnFlight() != null ? b.getReturnFlight().getFlightCode() : null)
                .outboundDeparture(b.getOutboundDeparture())
                .returnDeparture(b.getReturnDeparture())
                .selectedClass(b.getSelectedClass())
                .mealIncluded(b.getMealIncluded())
                .extraLuggage(b.getExtraLuggage())
                .paymentMethod(b.getPaymentMethod())
                .paymentStatus(b.getPaymentStatus())
                .bookingDate(b.getBookingDate())
                .totalPrice(b.getTotalPrice())
                .build();
    }
}
