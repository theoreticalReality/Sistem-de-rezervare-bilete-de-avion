package com.ProiectIS.GestionareAeroport.dto;

import com.ProiectIS.GestionareAeroport.model.Flight;
import com.ProiectIS.GestionareAeroport.model.RegularFlight;
import com.ProiectIS.GestionareAeroport.model.SeasonalFlight;
import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightDto {

    private Long id;
    private String flightCode;
    private String airplaneModel;
    private String departureCity;
    private String destinationCity;
    private String airlineName;
    private String type;
    private Map<ClassType, Integer> seats;
    private Map<ClassType, Double> prices;

    private List<DayOfWeek> daysOfWeek;
    private LocalTime departureTime;

    @JsonFormat(pattern = "MM-dd")
    private MonthDay seasonStart;
    @JsonFormat(pattern = "MM-dd")
    private MonthDay seasonEnd;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime departureDateTime;

    public static FlightDto fromEntity(Flight flight) {
        return fromEntity(flight, null);
    }

    public static FlightDto fromEntity(Flight flight, LocalDate forDate) {
        FlightDto.FlightDtoBuilder b = FlightDto.builder()
                .id(flight.getId())
                .flightCode(flight.getFlightCode())
                .airplaneModel(flight.getAirplaneModel())
                .departureCity(flight.getDepartureCity())
                .destinationCity(flight.getDestinationCity())
                .airlineName(flight.getAirline() != null ? flight.getAirline().getName() : null)
                .seats(flight.getSeats())
                .prices(flight.getPrices());

        if (flight instanceof RegularFlight rf) {
            b.type("REGULAR")
                    .daysOfWeek(rf.getDaysOfWeek())
                    .departureTime(rf.getDepartureTime());
            if (forDate != null) b.departureDateTime(rf.getDepartureDateTime(forDate));
        } else if (flight instanceof SeasonalFlight sf) {
            b.type("SEASONAL")
                    .daysOfWeek(sf.getDaysOfWeek())
                    .departureTime(sf.getDepartureTime())
                    .seasonStart(sf.getSeasonStart())
                    .seasonEnd(sf.getSeasonEnd());
            if (forDate != null) b.departureDateTime(sf.getDepartureDateTime(forDate));
        }
        return b.build();
    }
}
