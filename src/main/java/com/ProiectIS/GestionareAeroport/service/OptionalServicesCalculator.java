package com.ProiectIS.GestionareAeroport.service;

import org.springframework.stereotype.Service;

@Service
public class OptionalServicesCalculator {

    private static final double OPTIONAL_SERVICE_PERCENTAGE = 0.05;

    public Double calculateOptionalServicesPrice(Double baseTicketPrice,
                                                 Boolean mealIncluded,
                                                 Boolean extraLuggage) {
        if (baseTicketPrice == null || baseTicketPrice <= 0) {
            return 0.0;
        }

        double optionalServicePrice = baseTicketPrice * OPTIONAL_SERVICE_PERCENTAGE;
        double totalOptionalServicesPrice = 0.0;

        if (Boolean.TRUE.equals(mealIncluded)) {
            totalOptionalServicesPrice += optionalServicePrice;
        }

        if (Boolean.TRUE.equals(extraLuggage)) {
            totalOptionalServicesPrice += optionalServicePrice;
        }

        return totalOptionalServicesPrice;
    }
}

