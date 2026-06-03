package com.ProiectIS.GestionareAeroport.dto;

import com.ProiectIS.GestionareAeroport.model.DiscountPolicy;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class DiscountPolicyDto {

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double roundTripDiscountPercent;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double lastMinuteDiscountPercent;

    public static DiscountPolicyDto fromEntity(DiscountPolicy policy) {
        return new DiscountPolicyDto(
                policy.getRoundTripDiscount(),
                policy.getLastMinuteDiscount()
        );
    }
}
