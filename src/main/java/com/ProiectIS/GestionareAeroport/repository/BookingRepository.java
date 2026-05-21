package com.ProiectIS.GestionareAeroport.repository;

import com.ProiectIS.GestionareAeroport.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingId(String bookingId);
    List<Booking> findByOutboundFlight_FlightCodeOrReturnFlight_FlightCode(String code1, String code2);
    List<Booking> findByOutboundFlight_FlightCode(String code);
}
