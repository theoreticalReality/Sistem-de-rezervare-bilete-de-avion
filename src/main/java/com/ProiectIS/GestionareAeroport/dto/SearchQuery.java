package com.ProiectIS.GestionareAeroport.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchQuery {

    @NotBlank
    private String departureCity;

    @NotBlank
    private String destinationCity;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;

    @NotNull
    @Min(1)
    private Integer numberOfPassengers;

    private Boolean wantsReturn = false;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    public SearchQuery getReturnQuery() {
        if (returnDate == null) return null;
        return new SearchQuery(
                destinationCity,
                departureCity,
                returnDate,
                numberOfPassengers,
                false,
                null
        );
    }
}
