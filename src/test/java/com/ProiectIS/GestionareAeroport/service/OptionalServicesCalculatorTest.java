package com.ProiectIS.GestionareAeroport.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalServicesCalculatorTest {

    private final OptionalServicesCalculator calculator = new OptionalServicesCalculator();

    @Test
    void shouldAddFivePercentForMealIncluded() {
        Double optionalServicesPrice = calculator.calculateOptionalServicesPrice(1000.0, true, false);

        assertThat(optionalServicesPrice).isEqualTo(50.0);
    }

    @Test
    void shouldAddFivePercentForExtraLuggage() {
        Double optionalServicesPrice = calculator.calculateOptionalServicesPrice(1000.0, false, true);

        assertThat(optionalServicesPrice).isEqualTo(50.0);
    }

    @Test
    void shouldAddTenPercentWhenBothOptionsAreSelected() {
        Double optionalServicesPrice = calculator.calculateOptionalServicesPrice(1000.0, true, true);

        assertThat(optionalServicesPrice).isEqualTo(100.0);
    }
}

