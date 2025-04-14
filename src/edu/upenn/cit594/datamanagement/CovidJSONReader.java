package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.util.CovidRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

// Reads COVID-19 data from a JSON file and parses it into a list of CovidRecord objects
public class CovidJSONReader {
    private String filename; // Path to JSON file
    private List<CovidRecord> records; // Stores parsed records
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CovidJSONReader(String filename) {
        this.filename = filename;
        this.records = new ArrayList<>();
    }

    // Parses the JSON file and returns a list of CovidRecord objects
    public List<CovidRecord> readData() {
        JSONParser parser = new JSONParser();
        try (Reader reader = new FileReader(filename)) {
            Object obj = parser.parse(reader);
            JSONArray array = (JSONArray) obj; // Root of the JSON structure

            for (Object o : array) {
                JSONObject jsonObj = (JSONObject) o;

                // Extract and validate ZIP code
                String zip = ((String) jsonObj.get("zip_code")).trim();
                if (zip.length() != 5 || !zip.matches("\\d{5}")) continue;

                // Parse and validate timestamp
                String timestampStr = ((String) jsonObj.get("timestamp")).trim();
                LocalDateTime timestamp;
                try {
                    timestamp = LocalDateTime.parse(timestampStr, formatter);
                } catch (DateTimeParseException e) {
                    continue;
                }

                // Parse individual fields
                int partial = parseIntFromJson(jsonObj, "partial_vaccinated");
                int full = parseIntFromJson(jsonObj, "full_vaccinated");
                int tests = parseIntFromJson(jsonObj, "tests");
                int boosters = parseIntFromJson(jsonObj, "boosters");
                int hospitalized = parseIntFromJson(jsonObj, "hospitalized");
                int deaths = parseIntFromJson(jsonObj, "deaths");

                // Add the new record to the list
                CovidRecord record = new CovidRecord(zip, timestamp, partial, full, tests, boosters, hospitalized, deaths);
                records.add(record);
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error reading JSON file " + filename + ": " + e.getMessage());
        }
        return records;
    }

    // Utility function to parse integer values from a JSON object
    private int parseIntFromJson(JSONObject obj, String key) {
        Object val = obj.get(key);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0; // Return 0 on error
        }
    }
}
