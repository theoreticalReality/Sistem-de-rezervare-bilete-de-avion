package com.ProiectIS.GestionareAeroport.controller;

import com.ProiectIS.GestionareAeroport.dto.DiscountPolicyDto;
import com.ProiectIS.GestionareAeroport.model.DiscountPolicy;
import com.ProiectIS.GestionareAeroport.service.DiscountPolicyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discount-policy")
public class DiscountPolicyController {

    private final DiscountPolicyService discountPolicyService;

    public DiscountPolicyController(DiscountPolicyService discountPolicyService) {
        this.discountPolicyService = discountPolicyService;
    }

    @GetMapping
    public DiscountPolicyDto getPolicy() {
        return DiscountPolicyDto.fromEntity(discountPolicyService.getPolicy());
    }

    @PutMapping
    public DiscountPolicyDto updatePolicy(@Valid @RequestBody DiscountPolicyDto request) {
        DiscountPolicy updated = discountPolicyService.updatePolicy(
                request.getRoundTripDiscountPercent(),
                request.getLastMinuteDiscountPercent()
        );
        return DiscountPolicyDto.fromEntity(updated);
    }
}
