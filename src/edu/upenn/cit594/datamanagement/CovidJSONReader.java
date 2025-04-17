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

public class CovidJSONReader {
    private String filename;
    private List<CovidRecord> records;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CovidJSONReader(String filename) {
        this.filename = filename;
        this.records = new ArrayList<>();
    }

    public List<CovidRecord> readData() {
        JSONParser parser = new JSONParser();
        try (Reader reader = new FileReader(filename)) {
            Object obj = parser.parse(reader);
            JSONArray array = (JSONArray) obj;

            for (Object o : array) {
                JSONObject jsonObj = (JSONObject) o;
                String zip = ((String) jsonObj.get("zip_code")).trim();
                if (zip.length() != 5 || !zip.matches("\\d{5}")) continue;
                String timestampStr = ((String) jsonObj.get("timestamp")).trim();
                LocalDateTime timestamp;
                try {
                    timestamp = LocalDateTime.parse(timestampStr, formatter);
                } catch (DateTimeParseException e) {
                    continue;
                }
                int partial = parseIntFromJson(jsonObj, "partial_vaccinated");
                int full = parseIntFromJson(jsonObj, "full_vaccinated");
                int pos = parseIntFromJson(jsonObj, "POS");
                int neg = parseIntFromJson(jsonObj, "NEG");
                int boosters = parseIntFromJson(jsonObj, "boosters");
                int hospitalized = parseIntFromJson(jsonObj, "hospitalized");
                int deaths = parseIntFromJson(jsonObj, "deaths");

                CovidRecord record = new CovidRecord(zip, timestamp, partial, full, pos, neg, boosters, hospitalized, deaths);
                records.add(record);
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error reading JSON file " + filename + ": " + e.getMessage());
        }
        return records;
    }

    private int parseIntFromJson(JSONObject obj, String key) {
        Object val = obj.get(key);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
