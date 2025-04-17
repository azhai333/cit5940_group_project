package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.util.PropertyRecord;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PropertyCSVReader {
    private String filename;
    private List<PropertyRecord> records;

    public PropertyCSVReader(String filename) {
        this.filename = filename;
        this.records = new ArrayList<>();
    }

    public List<PropertyRecord> readData() {
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
                String rawZip = tokens[headerMap.get("zip_code")].trim();
                if (rawZip.length() < 5) continue;
                String zip = rawZip.substring(0, 5);
                if (!zip.matches("\\d{5}")) continue;

                String marketValStr = tokens[headerMap.get("market_value")].trim();
                double marketValue;
                try {
                    marketValue = Double.parseDouble(marketValStr);
                } catch (NumberFormatException e) {
                    continue;
                }

                String areaStr = tokens[headerMap.get("total_livable_area")].trim();
                double livableArea = 0;
                try {
                    livableArea = Double.parseDouble(areaStr);
                } catch (NumberFormatException e) {
                    // If parsing livable area fails, it remains 0.
                }
                records.add(new PropertyRecord(zip, marketValue, livableArea));
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
        }
        return records;
    }

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
