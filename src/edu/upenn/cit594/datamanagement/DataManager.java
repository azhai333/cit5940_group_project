package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.processor.PropertyAverageCalculator;
import edu.upenn.cit594.util.CovidRecord;
import edu.upenn.cit594.util.PropertyRecord;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

// Central class responsible for integrating and providing access to all datasets
public class DataManager {
    private final List<CovidRecord> covidRecords;
    private final List<PropertyRecord> propertyRecords;
    private final Map<String, Integer> populationData;

    // Calculator instances configured for specific property metrics
    private final PropertyAverageCalculator marketValueCalculator;
    private final PropertyAverageCalculator livableAreaCalculator;

    // Creates maps for memoization
    private final Map<String, Map<String, Double>> cachedVaccinationsPerCapita = new HashMap<>();
    private final Map<String, Integer> cachedMarketValuePerCapita = new HashMap<>();
    private Integer cachedTotalPopulation = null;

    public DataManager(List<CovidRecord> covidRecords, List<PropertyRecord> propertyRecords, Map<String, Integer> populationData) {
        this.covidRecords = covidRecords;
        this.propertyRecords = propertyRecords;
        this.populationData = populationData;

        // Initialize calculators for average market value and livable area
        this.marketValueCalculator = PropertyAverageCalculator.createWithMarketValue(propertyRecords);
        this.livableAreaCalculator = PropertyAverageCalculator.createWithLivableArea(propertyRecords);
    }

    // Returns the total population across all ZIP codes
    public int getTotalPopulation() {
        if (cachedTotalPopulation != null) {
            return cachedTotalPopulation;
        }
        cachedTotalPopulation = populationData.values().stream().mapToInt(Integer::intValue).sum();
        return cachedTotalPopulation;
    }

    // Returns a map of ZIP codes to vaccination per capita for a given date and vaccine type
    public Map<String, Double> getVaccinationsPerCapita(String type, LocalDate date) {
        String key = type.toLowerCase() + "_" + date;
        if (cachedVaccinationsPerCapita.containsKey(key)) {
            return cachedVaccinationsPerCapita.get(key);
        }

        Map<String, CovidRecord> latestPerZip = new HashMap<>();

        // Get the latest record per ZIP for the given date
        for (CovidRecord record : covidRecords) {
            if (record.getTimestamp().toLocalDate().equals(date)) {
                String zip = record.getZipCode();
                latestPerZip.merge(zip, record, (r1, r2) -> {
                    if (r2.getTimestamp().isAfter(r1.getTimestamp())) {
                        return r2;
                    } else {
                        return r1;
                    }
                });
            }
        }

        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, CovidRecord> entry : latestPerZip.entrySet()) {
            String zip = entry.getKey();
            CovidRecord rec = entry.getValue();
            int population = populationData.getOrDefault(zip, 0);
            if (population == 0) {
                continue;
            }

            int vaccinated;
            if (type.equalsIgnoreCase("partial")) {
                vaccinated = rec.getPartialVaccinated();
            } else {
                vaccinated = rec.getFullVaccinated();
            }

            if (vaccinated != 0) {
                double perCapita = (double) vaccinated / population;
                // Round to 4 decimal places
                result.put(zip, Math.round(perCapita * 10000.0) / 10000.0);
            }
        }

        cachedVaccinationsPerCapita.put(key, result);
        return result;
    }

    // Average market value of properties in a given ZIP
    public int getAverageMarketValue(String zip) {
        return marketValueCalculator.calculate(zip);
    }

    // Average livable area of properties in a given ZIP
    public int getAverageLivableArea(String zip) {
        return livableAreaCalculator.calculate(zip);
    }

    // Computes market value per capita for a ZIP by dividing total market value by population
    public int getMarketValuePerCapita(String zip) {
        if (cachedMarketValuePerCapita.containsKey(zip)) {
            return cachedMarketValuePerCapita.get(zip);
        }

        // Get all property records for this ZIP
        List<PropertyRecord> records = propertyRecords.stream()
                .filter(r -> r.getZipCode().equals(zip))
                .collect(Collectors.toList());

        if (records.isEmpty()) {
            cachedMarketValuePerCapita.put(zip, 0);
            return 0;
        }

        // Sum the market values
        double totalMarketValue = records.stream().mapToDouble(PropertyRecord::getMarketValue).sum();
        int population = populationData.getOrDefault(zip, 0);

        if (population == 0) {
            cachedMarketValuePerCapita.put(zip, 0);
            return 0;
        }

        int perCapita = (int) (totalMarketValue / population);
        cachedMarketValuePerCapita.put(zip, perCapita);
        return perCapita;
    }
}
