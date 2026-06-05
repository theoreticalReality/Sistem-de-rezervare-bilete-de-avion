package com.ProiectIS.GestionareAeroport.dto;

import com.ProiectIS.GestionareAeroport.model.enums.ClassType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class FlightOccupancyDto {
    private String flightCode;
    private String airplaneModel;
    private String route;
    
    private Map<ClassType, Integer> occupiedSeats;
    private Map<ClassType, Integer> totalSeats;
    
    private int totalAdults;
    private int totalChildren;
    private int totalSeniors;
    
    private List<PassengerManifestEntry> manifest;

    @Getter
    @Builder
    public static class PassengerManifestEntry {
        private String name;
        private String type;
        private ClassType selectedClass;
        private String bookingId;
        private String email;
    }
}
