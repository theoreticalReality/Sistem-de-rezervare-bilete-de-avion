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

    private final DiscountPolicyService discountPolicyService;
    private final OptionalServicesCalculator optionalServicesCalculator;

    public PriceCalculator(DiscountPolicyService discountPolicyService,
                           OptionalServicesCalculator optionalServicesCalculator) {
        this.discountPolicyService = discountPolicyService;
        this.optionalServicesCalculator = optionalServicesCalculator;
    }

    public Double calculateTicketPrice(Flight flight, ClassType classType, Integer passengerCount) {
        if (flight == null || classType == null || passengerCount == null || passengerCount <= 0) {
            throw new BadRequestException("Datele de calcul pret sunt invalide.");
        }
        Double pricePerSeat = flight.getPriceFor(classType);
        if (pricePerSeat == null) {
            throw new BadRequestException("Clasa selectata nu este disponibila pe acest zbor.");
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

    public Double calculateExtras(Double basePrice, Boolean mealIncluded, Boolean extraLuggage) {
        return optionalServicesCalculator.calculateOptionalServicesPrice(basePrice, mealIncluded, extraLuggage);
    }

    public Double calculateTotal(Booking booking) {
        if (booking.getPassenger() == null || booking.getSelectedClass() == null) {
            throw new BadRequestException("Rezervarea este incompleta.");
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

        if (discountPolicyService.getPolicy().isLastMinute(outboundDeparture)) {
            discounted = applyLastMinuteDiscount(discounted, outboundDeparture);
            lastMinuteApplied = true;
        }

        double extras = calculateExtras(basePrice, mealIncluded, extraLuggage);

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
