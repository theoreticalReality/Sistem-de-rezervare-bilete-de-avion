package com.ProiectIS.GestionareAeroport.service;

import com.ProiectIS.GestionareAeroport.model.DiscountPolicy;
import com.ProiectIS.GestionareAeroport.repository.DiscountPolicyRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiscountPolicyServiceTest {

    private final DiscountPolicyRepository repository = mock(DiscountPolicyRepository.class);
    private final DiscountPolicyService service = new DiscountPolicyService(repository);

    @Test
    void getPolicyCreatesDefaultsWhenNonePersisted() {
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(DiscountPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscountPolicy policy = service.getPolicy();

        assertThat(policy.getRoundTripDiscount()).isEqualTo(DiscountPolicy.DEFAULT_ROUND_TRIP_DISCOUNT);
        assertThat(policy.getLastMinuteDiscount()).isEqualTo(DiscountPolicy.DEFAULT_LAST_MINUTE_DISCOUNT);
        verify(repository).save(any(DiscountPolicy.class));
    }

    @Test
    void updatePolicyAppliesNewValues() {
        DiscountPolicy existing = new DiscountPolicy();
        when(repository.findAll()).thenReturn(List.of(existing));
        when(repository.save(any(DiscountPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscountPolicy result = service.updatePolicy(7.5, 30.0);

        assertThat(result.getRoundTripDiscount()).isEqualTo(7.5);
        assertThat(result.getLastMinuteDiscount()).isEqualTo(30.0);
    }

    @Test
    void updatePolicyIgnoresNullsAndKeepsExistingValues() {
        DiscountPolicy existing = new DiscountPolicy();
        existing.setRoundTripDiscount(12.0);
        existing.setLastMinuteDiscount(55.0);
        when(repository.findAll()).thenReturn(List.of(existing));
        when(repository.save(any(DiscountPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscountPolicy result = service.updatePolicy(null, null);

        assertThat(result.getRoundTripDiscount()).isEqualTo(12.0);
        assertThat(result.getLastMinuteDiscount()).isEqualTo(55.0);
    }

    @Test
    void isLastMinuteTrueWhenFlightWithinThreshold() {
        DiscountPolicy policy = new DiscountPolicy();

        assertThat(policy.isLastMinute(LocalDateTime.now().plusHours(24))).isTrue();
    }

    @Test
    void isLastMinuteFalseWhenFlightBeyondThreshold() {
        DiscountPolicy policy = new DiscountPolicy();

        assertThat(policy.isLastMinute(LocalDateTime.now().plusHours(72))).isFalse();
    }
}
