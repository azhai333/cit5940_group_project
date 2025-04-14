package edu.upenn.cit594.datamanagement;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// Reads population data from a CSV file and returns a mapping from ZIP code to population
public class PopulationCSVReader {
    private String filename; // Path to the CSV file

    public PopulationCSVReader(String filename) {
        this.filename = filename;
    }

    // Parses the CSV file and returns a ZIP-to-population map
    public Map<String, Integer> readData() {
        Map<String, Integer> populationMap = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename))) {
            String headerLine = br.readLine(); // Read header
            if (headerLine == null) return populationMap; // Empty file case

            String[] headers = parseCSVLine(headerLine);
            Map<String, Integer> headerMap = new HashMap<>();

            // Build a map of header name to column index
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].toLowerCase().trim(), i);
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = parseCSVLine(line);

                // Extract and validate ZIP code
                String zip = tokens[headerMap.get("zip_code")].trim();
                if (!zip.matches("\\d{5}")) continue;

                // Attempt to parse the population value
                String popStr = tokens[headerMap.get("population")].trim();
                int pop;
                try {
                    pop = Integer.parseInt(popStr);
                } catch (NumberFormatException e) {
                    continue; // Skip invalid entries
                }
                populationMap.put(zip, pop);
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
        }

        return populationMap;
    }

    // Handles CSV parsing including quoted fields and escaped characters
    private String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }
}
