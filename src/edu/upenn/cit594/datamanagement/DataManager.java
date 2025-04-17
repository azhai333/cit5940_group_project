package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.processor.PropertyAverageCalculator;
import edu.upenn.cit594.util.CovidRecord;
import edu.upenn.cit594.util.PropertyRecord;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {
    private final List<CovidRecord> covidRecords;
    private final List<PropertyRecord> propertyRecords;
    private final Map<String, Integer> populationData;

    private final PropertyAverageCalculator marketValueCalculator;
    private final PropertyAverageCalculator livableAreaCalculator;

    private final Map<String, Map<String, Double>> cachedVaccinationsPerCapita = new HashMap<>();
    private final Map<String, Integer> cachedMarketValuePerCapita = new HashMap<>();
    private Integer cachedTotalPopulation = null;

    public DataManager(List<CovidRecord> covidRecords, List<PropertyRecord> propertyRecords, Map<String, Integer> populationData) {
        this.covidRecords = covidRecords;
        this.propertyRecords = propertyRecords;
        this.populationData = populationData;
        this.marketValueCalculator = PropertyAverageCalculator.createWithMarketValue(propertyRecords);
        this.livableAreaCalculator = PropertyAverageCalculator.createWithLivableArea(propertyRecords);
    }

    public int getTotalPopulation() {
        if (cachedTotalPopulation != null) return cachedTotalPopulation;
        cachedTotalPopulation = populationData.values().stream().mapToInt(Integer::intValue).sum();
        return cachedTotalPopulation;
    }

    public Map<String, Double> getVaccinationsPerCapita(String type, LocalDate date) {
        String key = type.toLowerCase() + "_" + date;
        if (cachedVaccinationsPerCapita.containsKey(key)) return cachedVaccinationsPerCapita.get(key);

        Map<String, CovidRecord> latestPerZip = new HashMap<>();
        for (CovidRecord record : covidRecords) {
            if (record.getTimestamp().toLocalDate().equals(date)) {
                String zip = record.getZipCode();
                latestPerZip.merge(zip, record, (r1, r2) -> r2.getTimestamp().isAfter(r1.getTimestamp()) ? r2 : r1);
            }
        }

        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, CovidRecord> entry : latestPerZip.entrySet()) {
            String zip = entry.getKey();
            CovidRecord rec = entry.getValue();
            int population = populationData.getOrDefault(zip, 0);
            if (population == 0) continue;
            int vaccinated = type.equalsIgnoreCase("partial") ? rec.getPartialVaccinated() : rec.getFullVaccinated();

            if (vaccinated != 0) {
                double perCapita = (double) vaccinated / population;
                result.put(zip, Math.round(perCapita * 10000.0) / 10000.0);
            }
        }

        cachedVaccinationsPerCapita.put(key, result);
        return result;
    }

    public int getAverageMarketValue(String zip) {
        return marketValueCalculator.calculate(zip);
    }

    public int getAverageLivableArea(String zip) {
        return livableAreaCalculator.calculate(zip);
    }

    public int getMarketValuePerCapita(String zip) {
        if (cachedMarketValuePerCapita.containsKey(zip)) return cachedMarketValuePerCapita.get(zip);

        List<PropertyRecord> records = propertyRecords.stream()
                .filter(r -> r.getZipCode().equals(zip))
                .collect(Collectors.toList());

        if (records.isEmpty()) {
            cachedMarketValuePerCapita.put(zip, 0);
            return 0;
        }

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

    /**
     * Custom Feature: Finds clusters of ZIPs connected by adjacency (ZIP difference == 1)
     * where each ZIP's full-vaccination rate, average livable area, and population meet thresholds.
     * Uses a graph (adjacency list) + BFS (Queue) to identify connected components.
     *
     * @param date        LocalDate for full vaccination data
     * @param minRate     minimum full-vaccination rate (0.0–1.0)
     * @param minArea     minimum average livable area (sq ft)
     * @param minPopulation minimum population per ZIP
     * @return list of clusters (sets of ZIP codes) matching criteria
     */
    public List<Set<String>> getWellnessClusters(LocalDate date,
                                                 double minRate,
                                                 int minArea,
                                                 int minPopulation) {
        // Step 1: collect full vaccination rates on date
        Map<String, Double> rates = getVaccinationsPerCapita("full", date);

        // Build node set: only ZIPs with data
        Set<String> nodes = new HashSet<>();
        for (String zip : rates.keySet()) {
            int pop = populationData.getOrDefault(zip, 0);
            if (pop >= minPopulation) {
                nodes.add(zip);
            }
        }

        // Step 2: build adjacency list: ZIPs adjacent if numeric difference == 1
        Map<String, List<String>> adj = new HashMap<>();
        for (String z : nodes) {
            adj.put(z, new ArrayList<>());
        }
        for (String z1 : nodes) {
            int code1 = Integer.parseInt(z1);
            for (String z2 : nodes) {
                int code2 = Integer.parseInt(z2);
                if (Math.abs(code1 - code2) == 1) {
                    adj.get(z1).add(z2);
                }
            }
        }

        // Step 3: BFS to find clusters where all meeting thresholds
        Set<String> visited = new HashSet<>();
        List<Set<String>> clusters = new ArrayList<>();

        for (String start : nodes) {
            if (visited.contains(start)) continue;
            double rate = rates.getOrDefault(start, 0.0);
            int area = getAverageLivableArea(start);
            // Population already filtered
            if (rate < minRate || area < minArea) {
                visited.add(start);
                continue;
            }
            // BFS for this component
            Set<String> comp = new HashSet<>();
            Queue<String> queue = new LinkedList<>();
            visited.add(start);
            queue.add(start);
            while (!queue.isEmpty()) {
                String cur = queue.poll();
                comp.add(cur);
                for (String nei : adj.get(cur)) {
                    if (!visited.contains(nei)) {
                        double r2 = rates.getOrDefault(nei, 0.0);
                        int a2 = getAverageLivableArea(nei);
                        int p2 = populationData.getOrDefault(nei, 0);
                        if (r2 >= minRate && a2 >= minArea && p2 >= minPopulation) {
                            visited.add(nei);
                            queue.add(nei);
                        }
                    }
                }
            }
            clusters.add(comp);
        }
        return clusters;
    }
}