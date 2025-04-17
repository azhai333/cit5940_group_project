package edu.upenn.cit594;

import edu.upenn.cit594.datamanagement.*;
import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.ui.UserInterface;
import edu.upenn.cit594.util.CovidRecord;
import edu.upenn.cit594.util.PropertyRecord;
import java.io.File;
import java.util.*;

/**
 * The Main class serves as the entry point for the application.
 * It handles command-line argument parsing, data loading, and initialization
 * of the application components.
 */
public class Main {

    /**
     * The main entry point for the application.
     *
     * @param args Command-line arguments in the format --key=value.
     *             Supported keys: covid, properties, population, log
     * @throws IllegalArgumentException If invalid arguments are provided
     */
    public static void main(String[] args) {
        // 1. Parse and validate arguments
        Map<String, String> argsMap = parseArguments(args);
        if (argsMap == null || !validateArguments(argsMap)) {
            System.err.println("Invalid arguments");
            return;
        }

        // 2. Initialize logger
        Logger logger = Logger.getInstance();
        if (argsMap.containsKey("log")) {
            logger.setDestination(argsMap.get("log"));
        }
        logger.log("Program started with arguments: " + String.join(" ", args));

        // 3. Read input files
        List<CovidRecord> covidRecords = readCovidData(argsMap.get("covid"), logger);
        List<PropertyRecord> propertyRecords = readPropertyData(argsMap.get("properties"), logger);
        Map<String, Integer> populationData = readPopulationData(argsMap.get("population"), logger);

        // 4. Initialize DataManager
        DataManager dataManager = new DataManager(covidRecords, propertyRecords, populationData);

        // 5. Start UI
        UserInterface ui = new UserInterface(dataManager, logger);
        ui.start();
    }

    /**
     * Parses command-line arguments into a key-value map.
     *
     * @param args The command-line arguments array
     * @return Map of argument keys to values, or null if parsing fails
     */
    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> argsMap = new HashMap<>();
        for (String arg : args) {
            if (!arg.startsWith("--") || !arg.contains("=")) {
                System.err.println("Invalid argument format: " + arg);
                return null;
            }
            String[] parts = arg.substring(2).split("=", 2);
            argsMap.put(parts[0], parts[1]);
        }
        return argsMap;
    }

    /**
     * Validates the parsed command-line arguments.
     *
     * @param argsMap The parsed arguments map
     * @return true if all arguments are valid, false otherwise
     */
    private static boolean validateArguments(Map<String, String> argsMap) {
        Set<String> validArgs = Set.of("covid", "properties", "population", "log");
        if (!argsMap.keySet().stream().allMatch(validArgs::contains)) {
            return false;
        }

        return argsMap.entrySet().stream()
                .filter(e -> !e.getKey().equals("log"))
                .allMatch(e -> {
                    File f = new File(e.getValue());
                    return f.exists() && f.canRead();
                });
    }

    /**
     * Reads COVID data from either JSON or CSV file based on file extension.
     *
     * @param filename Path to the input file
     * @param logger Logger instance for error reporting
     * @return List of CovidRecord objects, or empty list if reading fails
     */
    private static List<CovidRecord> readCovidData(String filename, Logger logger) {
        if (filename == null) return Collections.emptyList();

        try {
            List<CovidRecord> records = filename.endsWith(".json")
                    ? new CovidJSONReader(filename).readData()
                    : new CovidCSVReader(filename).readData();
            logger.log("Loaded " + records.size() + " COVID records");
            return records;
        } catch (Exception e) {
            logger.log("Error reading COVID data: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Reads property data from CSV file.
     *
     * @param filename Path to the properties CSV file
     * @param logger Logger instance for error reporting
     * @return List of PropertyRecord objects, or empty list if reading fails
     */
    private static List<PropertyRecord> readPropertyData(String filename, Logger logger) {
        if (filename == null) return Collections.emptyList();

        try {
            List<PropertyRecord> records = new PropertyCSVReader(filename).readData();
            logger.log("Loaded " + records.size() + " property records");
            return records;
        } catch (Exception e) {
            logger.log("Error reading property data: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Reads population data from CSV file.
     *
     * @param filename Path to the population CSV file
     * @param logger Logger instance for error reporting
     * @return Map of ZIP codes to population counts, or empty map if reading fails
     */
    private static Map<String, Integer> readPopulationData(String filename, Logger logger) {
        if (filename == null) return Collections.emptyMap();

        try {
            Map<String, Integer> data = new PopulationCSVReader(filename).readData();
            logger.log("Loaded population data for " + data.size() + " ZIP codes");
            return data;
        } catch (Exception e) {
            logger.log("Error reading population data: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}