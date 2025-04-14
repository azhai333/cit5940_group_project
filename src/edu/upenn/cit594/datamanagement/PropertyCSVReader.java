package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.util.PropertyRecord;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// Reads property data from a CSV file and parses it into a list of PropertyRecord objects
public class PropertyCSVReader {
    private String filename; // Path to the CSV file
    private List<PropertyRecord> records; // Stores parsed property records

    public PropertyCSVReader(String filename) {
        this.filename = filename;
        this.records = new ArrayList<>();
    }

    // Main method to read and parse the file
    public List<PropertyRecord> readData() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename))) {
            String headerLine = br.readLine(); // Read header line
            if (headerLine == null) return records;

            String[] headers = parseCSVLine(headerLine);
            Map<String, Integer> headerMap = new HashMap<>();

            // Create a map from column names to their indices
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].toLowerCase().trim(), i);
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = parseCSVLine(line);

                // Extract and validate ZIP code
                String rawZip = tokens[headerMap.get("zip_code")].trim();
                if (rawZip.length() < 5) continue;
                String zip = rawZip.substring(0, 5);
                if (!zip.matches("\\d{5}")) continue;

                // Parse market value field
                String marketValStr = tokens[headerMap.get("market_value")].trim();
                double marketValue;
                try {
                    marketValue = Double.parseDouble(marketValStr);
                } catch (NumberFormatException e) {
                    continue; // Skip if market value is invalid
                }

                // Parse livable area field
                String areaStr = tokens[headerMap.get("total_livable_area")].trim();
                double livableArea = 0;
                try {
                    livableArea = Double.parseDouble(areaStr);
                } catch (NumberFormatException e) {
                    // Default to 0 if parsing fails
                }

                // Create and add the property record
                records.add(new PropertyRecord(zip, marketValue, livableArea));
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
        }
        return records;
    }

    // Custom CSV parser that handles quoted fields correctly
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
