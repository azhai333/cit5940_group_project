package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.util.CovidRecord;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CovidCSVReader {
    private String filename;
    private List<CovidRecord> records;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CovidCSVReader(String filename) {
        this.filename = filename;
        this.records = new ArrayList<>();
    }

    public List<CovidRecord> readData() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename))) {
            String headerLine = br.readLine();
            if (headerLine == null) return records;
            String[] headers = parseCSVLine(headerLine);
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].toLowerCase().trim(), i);
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = parseCSVLine(line);
                String zip = tokens[headerMap.get("zip_code")].trim();
                if (zip.length() != 5 || !zip.matches("\\d{5}")) continue;

                String timestampStr = tokens[headerMap.get("etl_timestamp")].trim();

                LocalDateTime timestamp;
                try {
                    timestamp = LocalDateTime.parse(timestampStr, formatter);
                } catch (DateTimeParseException e) {
                    continue;
                }

                int partial = parseInteger(tokens, headerMap, "partially_vaccinated");
                int full = parseInteger(tokens, headerMap, "fully_vaccinated");
                int pos = parseInteger(tokens, headerMap, "POS");
                int neg = parseInteger(tokens, headerMap, "NEG");
                int boosters = parseInteger(tokens, headerMap, "boosted");
                int hospitalized = parseInteger(tokens, headerMap, "hospitalized");
                int deaths = parseInteger(tokens, headerMap, "deaths");

                CovidRecord record = new CovidRecord(zip, timestamp, partial, full, pos, neg, boosters, hospitalized, deaths);
                records.add(record);
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
        }
        return records;
    }

    private int parseInteger(String[] tokens, Map<String, Integer> headerMap, String fieldName) {
        int index = headerMap.getOrDefault(fieldName, -1);
        if (index == -1 || index >= tokens.length) return 0;
        String value = tokens[index].trim();
        if (value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Basic CSV line parser that handles quoted fields.
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
