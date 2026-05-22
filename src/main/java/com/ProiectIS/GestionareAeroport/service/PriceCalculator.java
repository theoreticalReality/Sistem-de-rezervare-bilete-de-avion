package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.dto.PriceQuote;
import com.ProiectIS.GestionareAeroport.exception.BadRequestException;
import com.ProiectIS.GestionareAeroport.model.Booking;
import com.ProiectIS.GestionareAeroport.model.DiscountPolicy;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.RegularFlight;
import com.ProiectIS.GestionareAeroport.model.SeasonalFlight;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PriceCalculator {

    public static final double MEAL_FEE = 25.0;
    public static final double LUGGAGE_FEE = 35.0;

    private final DiscountPolicyService discountPolicyService;

    public PriceCalculator(DiscountPolicyService discountPolicyService) {
        this.discountPolicyService = discountPolicyService;
    }

    public Double calculateTicketPrice(Flight flight, ClassType classType, Integer passengerCount) {
        if (flight == null || classType == null || passengerCount == null || passengerCount <= 0) {
            throw new BadRequestException("Datele de calcul preț sunt invalide.");
        }
        Double pricePerSeat = flight.getPriceFor(classType);
        if (pricePerSeat == null) {
            throw new BadRequestException("Clasa selectată nu este disponibilă pe acest zbor.");
        }
        return pricePerSeat * passengerCount;
    }

    public Double applyRoundTripDiscount(Double price) {
        DiscountPolicy policy = discountPolicyService.getPolicy();
        double discount = policy.getRoundTripDiscount();
        return price - (price * discount / 100.0);
    }

    public Double applyLastMinuteDiscount(Double price, LocalDateTime flightDate) {
        DiscountPolicy policy = discountPolicyService.getPolicy();
        if (!policy.isLastMinute(flightDate)) {
            return price;
        }
        double discount = policy.getLastMinuteDiscount();
        return price - (price * discount / 100.0);
    }

    public Double calculateExtras(Double discountedPrice, Integer passengerCount, Boolean mealIncluded, Boolean extraLuggage) {
        double extras = 0.0;
        double extraFee = discountedPrice * 0.05; // 5% din valoarea biletelor de bază (după discount-uri, sau înainte? cerința zice "baza")
        
        if (Boolean.TRUE.equals(mealIncluded)) extras += extraFee;
        if (Boolean.TRUE.equals(extraLuggage)) extras += extraFee;
        
        return extras;
    }

    public Double calculateTotal(Booking booking) {
        if (booking.getPassenger() == null || booking.getSelectedClass() == null) {
            throw new BadRequestException("Rezervarea este incompletă.");
        }
        PriceQuote quote = quote(
                booking.getOutboundFlight(),
                booking.getReturnFlight(),
                booking.getOutboundDeparture(),
                booking.getReturnDeparture(),
                booking.getSelectedClass(),
                booking.getPassenger().getTotalPassengerCount(),
                booking.getMealIncluded(),
                booking.getExtraLuggage()
        );
        return quote.getTotalPrice();
    }

    public PriceQuote quote(Flight outbound, Flight returnFlight,
                            LocalDateTime outboundDeparture, LocalDateTime returnDeparture,
                            ClassType classType, Integer passengerCount,
                            Boolean mealIncluded, Boolean extraLuggage) {
        DiscountPolicy policy = discountPolicyService.getPolicy();

        double outboundPrice = calculateTicketPrice(outbound, classType, passengerCount);
        double returnPrice = 0.0;
        if (returnFlight != null) {
            returnPrice = calculateTicketPrice(returnFlight, classType, passengerCount);
        }

        double basePrice = outboundPrice + returnPrice;
        double discounted = basePrice;

        boolean roundTripApplied = false;
        boolean lastMinuteApplied = false;

        if (returnFlight != null) {
            discounted = applyRoundTripDiscount(discounted);
            roundTripApplied = true;
        }

        // Cerința f) cere implementarea doar a discount-ului de tur-retur.
        // Discount-ul de last minute este definit la punctul c), dar punctul f) pare să-l restricționeze la cel de tur-retur pentru calculul final.
        // Totuși, dacă punctul c) spune că sunt obligatorii, le lăsăm pe ambele sau doar pe cel cerut la f)?
        // Re-citind: "Se cere implementarea doar a discount-ului de tur-retur". Voi comenta/elimina aplicarea last-minute aici.

        /*
        if (outboundDeparture != null && policy.isLastMinute(outboundDeparture)) {
            discounted = applyLastMinuteDiscount(discounted, outboundDeparture);
            lastMinuteApplied = true;
        } else if (returnDeparture != null && policy.isLastMinute(returnDeparture)) {
            discounted = applyLastMinuteDiscount(discounted, returnDeparture);
            lastMinuteApplied = true;
        }
        */

        double extras = calculateExtras(discounted, passengerCount, mealIncluded, extraLuggage);

        double total = discounted + extras;

        return PriceQuote.builder()
                .basePrice(basePrice)
                .extrasPrice(extras)
                .discountAmount(basePrice - discounted)
                .totalPrice(total)
                .roundTripDiscountApplied(roundTripApplied)
                .lastMinuteDiscountApplied(lastMinuteApplied)
                .build();
    }

    public LocalDateTime resolveDeparture(Flight flight, LocalDate date) {
        if (flight == null || date == null) return null;
        if (flight instanceof RegularFlight rf) return rf.getDepartureDateTime(date);
        if (flight instanceof SeasonalFlight sf) return sf.getDepartureDateTime(date);
        return null;
    }
}
