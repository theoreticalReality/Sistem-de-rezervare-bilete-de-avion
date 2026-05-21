package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.model.DiscountPolicy;
import com.ProiectIS.GestionareAeroport.repository.DiscountPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscountPolicyService {

    private final DiscountPolicyRepository repository;

    public DiscountPolicyService(DiscountPolicyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DiscountPolicy getPolicy() {
        return repository.findAll().stream()
                .findFirst()
                .orElseGet(() -> repository.save(new DiscountPolicy()));
    }

    @Transactional
    public DiscountPolicy updatePolicy(Double roundTripDiscount, Double lastMinuteDiscount) {
        DiscountPolicy policy = getPolicy();
        if (roundTripDiscount != null) policy.setRoundTripDiscount(roundTripDiscount);
        if (lastMinuteDiscount != null) policy.setLastMinuteDiscount(lastMinuteDiscount);
        return repository.save(policy);
    }
}
