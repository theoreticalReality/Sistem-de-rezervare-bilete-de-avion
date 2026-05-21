package com.ProiectIS.GestionareAeroport.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("SEASONAL")
@Getter
@Setter
@NoArgsConstructor
public class SeasonalFlight extends Flight {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "seasonal_flight_days", joinColumns = @JoinColumn(name = "flight_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> daysOfWeek = new ArrayList<>();

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "season_start_month")
    private Integer seasonStartMonth;

    @Column(name = "season_start_day")
    private Integer seasonStartDay;

    @Column(name = "season_end_month")
    private Integer seasonEndMonth;

    @Column(name = "season_end_day")
    private Integer seasonEndDay;

    public MonthDay getSeasonStart() {
        if (seasonStartMonth == null || seasonStartDay == null) return null;
        return MonthDay.of(seasonStartMonth, seasonStartDay);
    }

    public void setSeasonStart(MonthDay md) {
        if (md == null) {
            this.seasonStartMonth = null;
            this.seasonStartDay = null;
        } else {
            this.seasonStartMonth = md.getMonthValue();
            this.seasonStartDay = md.getDayOfMonth();
        }
    }

    public MonthDay getSeasonEnd() {
        if (seasonEndMonth == null || seasonEndDay == null) return null;
        return MonthDay.of(seasonEndMonth, seasonEndDay);
    }

    public void setSeasonEnd(MonthDay md) {
        if (md == null) {
            this.seasonEndMonth = null;
            this.seasonEndDay = null;
        } else {
            this.seasonEndMonth = md.getMonthValue();
            this.seasonEndDay = md.getDayOfMonth();
        }
    }

    public boolean isInSeason(LocalDate date) {
        if (date == null) return false;
        MonthDay start = getSeasonStart();
        MonthDay end = getSeasonEnd();
        if (start == null || end == null) return false;
        MonthDay md = MonthDay.from(date);
        if (!start.isAfter(end)) {
            return !md.isBefore(start) && !md.isAfter(end);
        }
        // sezon care trece de schimbarea de an (ex. 1 Noi → 31 Mar)
        return !md.isBefore(start) || !md.isAfter(end);
    }

    @Override
    public boolean isAvailableOn(LocalDate date) {
        if (date == null || daysOfWeek == null) return false;
        return isInSeason(date) && daysOfWeek.contains(date.getDayOfWeek());
    }

    public LocalDateTime getDepartureDateTime(LocalDate date) {
        if (!isAvailableOn(date) || departureTime == null) return null;
        return LocalDateTime.of(date, departureTime);
    }
}
