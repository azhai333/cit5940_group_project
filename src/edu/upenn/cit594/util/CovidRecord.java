package edu.upenn.cit594.util;

import java.time.LocalDateTime;

public class CovidRecord {
    private String zipCode;
    private LocalDateTime timestamp;
    private int partialVaccinated;
    private int fullVaccinated;
    private int pos;
    private int neg;
    private int boosters;
    private int hospitalized;
    private int deaths;

    public CovidRecord(String zipCode, LocalDateTime timestamp, int partialVaccinated, int fullVaccinated,
                       int pos, int neg, int boosters, int hospitalized, int deaths) {
        this.zipCode = zipCode;
        this.timestamp = timestamp;
        this.partialVaccinated = partialVaccinated;
        this.fullVaccinated = fullVaccinated;
        this.pos = pos;
        this.neg = neg;
        this.boosters = boosters;
        this.hospitalized = hospitalized;
        this.deaths = deaths;
    }

    public String getZipCode() { return zipCode; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getPartialVaccinated() { return partialVaccinated; }
    public int getFullVaccinated() { return fullVaccinated; }
    public int getPos() { return pos; }
    public int getNeg() { return neg; }
    public int getBoosters() { return boosters; }
    public int getHospitalized() { return hospitalized; }
    public int getDeaths() { return deaths; }
}
