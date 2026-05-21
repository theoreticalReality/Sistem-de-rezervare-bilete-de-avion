package com.ProiectIS.GestionareAeroport.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    private List<FlightDto> outboundFlights = new ArrayList<>();
    private List<FlightDto> returnFlights = new ArrayList<>();

    public boolean hasOutboundResults() {
        return outboundFlights != null && !outboundFlights.isEmpty();
    }

    public boolean hasReturnResults() {
        return returnFlights != null && !returnFlights.isEmpty();
    }
}
