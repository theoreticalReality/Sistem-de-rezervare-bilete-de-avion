package com.ProiectIS.GestionareAeroport;

import com.ProiectIS.GestionareAeroport.dto.PriceQuote;
import com.ProiectIS.GestionareAeroport.exception.BadRequestException;
import com.ProiectIS.GestionareAeroport.exception.NotFoundException;
import com.ProiectIS.GestionareAeroport.model.DiscountPolicy;
import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.Passenger;
import com.ProiectIS.GestionareAeroport.model.SeasonalFlight;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.ProiectIS.GestionareAeroport.repository.BookingRepository;
import com.ProiectIS.GestionareAeroport.repository.FlightRepository;
import com.ProiectIS.GestionareAeroport.service.BookingService;
import com.ProiectIS.GestionareAeroport.service.DiscountPolicyService;
import com.ProiectIS.GestionareAeroport.service.OptionalServicesCalculator;
import com.ProiectIS.GestionareAeroport.service.PriceCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class GestionareAeroportApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void whiteBox_optionalServicesReturnsZeroWhenNoExtrasSelected() {
		OptionalServicesCalculator calculator = new OptionalServicesCalculator();

		Double result = calculator.calculateOptionalServicesPrice(1000.0, false, false);

		assertThat(result).isEqualTo(0.0);
	}

	@Test
	void whiteBox_optionalServicesReturnsZeroForNonPositiveBasePrice() {
		OptionalServicesCalculator calculator = new OptionalServicesCalculator();

		assertThat(calculator.calculateOptionalServicesPrice(null, true, true)).isEqualTo(0.0);
		assertThat(calculator.calculateOptionalServicesPrice(0.0, true, true)).isEqualTo(0.0);
		assertThat(calculator.calculateOptionalServicesPrice(-50.0, true, true)).isEqualTo(0.0);
	}

	@Test
	void whiteBox_calculateTicketPriceRejectsZeroPassengerCount() {
		PriceCalculator calculator = new PriceCalculator(
				mock(DiscountPolicyService.class),
				new OptionalServicesCalculator()
		);
		Flight flight = new TestFlight();
		flight.getPrices().put(ClassType.ECONOMY, 200.0);

		assertThatThrownBy(() -> calculator.calculateTicketPrice(flight, ClassType.ECONOMY, 0))
				.isInstanceOf(BadRequestException.class);
	}

	@Test
	void whiteBox_seasonalFlightRecognizesCrossYearSeason() {
		SeasonalFlight flight = new SeasonalFlight();
		flight.setSeasonStart(MonthDay.of(12, 1));
		flight.setSeasonEnd(MonthDay.of(2, 28));

		assertThat(flight.isInSeason(LocalDate.of(2026, 12, 15))).isTrue();
		assertThat(flight.isInSeason(LocalDate.of(2027, 1, 20))).isTrue();
		assertThat(flight.isInSeason(LocalDate.of(2026, 6, 15))).isFalse();
	}

	@Test
	void whiteBox_isLastMinuteReturnsFalseForPastFlight() {
		DiscountPolicy policy = new DiscountPolicy();

		assertThat(policy.isLastMinute(LocalDateTime.now().minusHours(1))).isFalse();
	}

	@Test
	void blackBox_oneWayQuoteHasNoRoundTripDiscount() {
		DiscountPolicyService discountPolicyService = mock(DiscountPolicyService.class);
		when(discountPolicyService.getPolicy()).thenReturn(new DiscountPolicy());
		PriceCalculator calculator = new PriceCalculator(discountPolicyService, new OptionalServicesCalculator());

		Flight outbound = new TestFlight();
		outbound.getPrices().put(ClassType.ECONOMY, 150.0);

		PriceQuote quote = calculator.quote(outbound, null, null, null, ClassType.ECONOMY, 2, false, false);

		assertThat(quote.getBasePrice()).isCloseTo(300.0, within(0.001));
		assertThat(quote.getDiscountAmount()).isCloseTo(0.0, within(0.001));
		assertThat(quote.getExtrasPrice()).isCloseTo(0.0, within(0.001));
		assertThat(quote.getTotalPrice()).isCloseTo(300.0, within(0.001));
		assertThat(quote.isRoundTripDiscountApplied()).isFalse();
	}

	@Test
	void blackBox_ticketPriceMultipliesUnitPriceByPassengerCount() {
		PriceCalculator calculator = new PriceCalculator(
				mock(DiscountPolicyService.class),
				new OptionalServicesCalculator()
		);
		Flight flight = new TestFlight();
		flight.getPrices().put(ClassType.BUSINESS, 450.0);

		Double price = calculator.calculateTicketPrice(flight, ClassType.BUSINESS, 3);

		assertThat(price).isCloseTo(1350.0, within(0.001));
	}

	@Test
	void blackBox_passengerCountSumsAllCategories() {
		Passenger passenger = new Passenger("Maria", "0712345678", 2, 1, 1);

		assertThat(passenger.getTotalPassengerCount()).isEqualTo(4);
	}

	@Test
	void blackBox_flightReturnsZeroAvailableSeatsForUnknownClass() {
		Flight flight = new TestFlight();
		flight.getSeats().put(ClassType.ECONOMY, 10);

		assertThat(flight.getAvailableSeats(ClassType.BUSINESS)).isEqualTo(0);
	}

	@Test
	void blackBox_findByBookingIdThrowsNotFoundWhenMissing() {
		BookingRepository bookingRepository = mock(BookingRepository.class);
		FlightRepository flightRepository = mock(FlightRepository.class);
		PriceCalculator priceCalculator = mock(PriceCalculator.class);
		BookingService bookingService = new BookingService(bookingRepository, flightRepository, priceCalculator);
		when(bookingRepository.findByBookingId("BK-UNKNOWN")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingService.findByBookingId("BK-UNKNOWN"))
				.isInstanceOf(NotFoundException.class);
	}

	private static class TestFlight extends Flight {
		@Override
		public boolean isAvailableOn(LocalDate date) {
			return true;
		}
	}
}
