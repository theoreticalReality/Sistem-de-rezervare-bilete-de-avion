package com.ProiectIS.GestionareAeroport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceQuote {

    private Double basePrice;
    private Double extrasPrice;
    private Double discountAmount;
    private Double totalPrice;
    private boolean roundTripDiscountApplied;
    private boolean lastMinuteDiscountApplied;
}
