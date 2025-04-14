package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.util.CovidRecord;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

// Responsible for reading COVID-19 data from a CSV file and parsing it into a list of CovidRecord objects
public class CovidCSVReader {
    private String filename; // Path to the CSV file
    private List<CovidRecord> records; // List to store the parsed COVID-19 records
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Format used for parsing timestamps

    public CovidCSVReader(String filename) {
        this.filename = filename;
        this.records = new ArrayList<>();
    }

    // Main method to read data from the CSV file
    public List<CovidRecord> readData() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename))) {
            String headerLine = br.readLine(); // Read header to identify column indices
            if (headerLine == null) return records; // Return empty list if file is empty

            String[] headers = parseCSVLine(headerLine);
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].toLowerCase().trim(), i); // Normalize header names to allow flexible matching
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = parseCSVLine(line);

                // Validate and extract ZIP code
                String zip = tokens[headerMap.get("zip_code")].trim();
                if (zip.length() != 5 || !zip.matches("\\d{5}")) continue; // Skip invalid ZIPs

                // Parse and validate timestamp
                String timestampStr = tokens[headerMap.get("timestamp")].trim();
                LocalDateTime timestamp;
                try {
                    timestamp = LocalDateTime.parse(timestampStr, formatter);
                } catch (DateTimeParseException e) {
                    continue; // Skip malformed timestamps
                }

                // Parse all relevant integer fields
                int partial = parseInteger(tokens, headerMap, "partial_vaccinated");
                int full = parseInteger(tokens, headerMap, "full_vaccinated");
                int tests = parseInteger(tokens, headerMap, "tests");
                int boosters = parseInteger(tokens, headerMap, "boosters");
                int hospitalized = parseInteger(tokens, headerMap, "hospitalized");
                int deaths = parseInteger(tokens, headerMap, "deaths");

                // Construct a CovidRecord and add it to the result list
                CovidRecord record = new CovidRecord(zip, timestamp, partial, full, tests, boosters, hospitalized, deaths);
                records.add(record);
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
        }
        return records;
    }

    // Parse an integer value from the specified column in the token array
    private int parseInteger(String[] tokens, Map<String, Integer> headerMap, String fieldName) {
        int index = headerMap.getOrDefault(fieldName, -1);
        if (index == -1 || index >= tokens.length) return 0;
        String value = tokens[index].trim();
        if (value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0; // Default to 0 if parsing fails
        }
    }

    // Custom CSV parser to correctly handle fields that may be quoted or contain embedded commas
    private String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"'); // Handle escaped quote inside quoted field
                    i++;
                } else {
                    inQuotes = !inQuotes; // Toggle quote state
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString()); // Comma outside quotes ends field
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString()); // Add final token
        return tokens.toArray(new String[0]);
    }
}
