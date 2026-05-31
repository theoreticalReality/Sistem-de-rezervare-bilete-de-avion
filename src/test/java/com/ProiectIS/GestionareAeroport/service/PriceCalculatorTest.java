package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.dto.PriceQuote;
import com.ProiectIS.GestionareAeroport.model.DiscountPolicy;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PriceCalculatorTest {

    private final DiscountPolicyService discountPolicyService = mock(DiscountPolicyService.class);
    private final PriceCalculator calculator = new PriceCalculator(
            discountPolicyService,
            new OptionalServicesCalculator()
    );

    @Test
    void shouldCalculateTotalForRoundTripWithExtrasAndDiscount() {
        DiscountPolicy policy = new DiscountPolicy();
        policy.setRoundTripDiscount(5.0);
        when(discountPolicyService.getPolicy()).thenReturn(policy);

        Flight outbound = flightWithPrice(100.0);
        Flight returnFlight = flightWithPrice(120.0);

        PriceQuote quote = calculator.quote(
                outbound,
                returnFlight,
                null,
                null,
                ClassType.ECONOMY,
                2,
                true,
                true
        );

        assertThat(quote.getBasePrice()).isCloseTo(440.0, within(0.001));
        assertThat(quote.getDiscountAmount()).isCloseTo(22.0, within(0.001));
        assertThat(quote.getExtrasPrice()).isCloseTo(44.0, within(0.001));
        assertThat(quote.getTotalPrice()).isCloseTo(462.0, within(0.001));
        assertThat(quote.isRoundTripDiscountApplied()).isTrue();
    }

    private Flight flightWithPrice(double economyPrice) {
        Flight flight = new TestFlight();
        flight.getPrices().put(ClassType.ECONOMY, economyPrice);
        return flight;
    }

    private static class TestFlight extends Flight {
        @Override
        public boolean isAvailableOn(LocalDate date) {
            return true;
        }
    }
}
