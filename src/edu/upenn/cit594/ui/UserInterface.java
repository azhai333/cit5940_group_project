package edu.upenn.cit594.ui;

import edu.upenn.cit594.datamanagement.DataManager;
import edu.upenn.cit594.logging.Logger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * The UserInterface class provides a console-based interface for interacting with
 * COVID, property, and population data. It handles user input and displays results
 * from the DataManager.
 */
public class UserInterface {
    private final DataManager dataManager;
    private final Logger logger;
    private final Scanner scanner;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructs a new UserInterface with the specified DataManager and Logger.
     *
     * @param dataManager The DataManager instance to handle data operations
     * @param logger The Logger instance for logging user actions
     */
    public UserInterface(DataManager dataManager, Logger logger) {
        this.dataManager = dataManager;
        this.logger = logger;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the user interface and begins processing user commands.
     * Displays the main menu and handles user input in a continuous loop
     * until the user chooses to exit (option 0).
     */
    public void start() {
        displayMainMenu();
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            logger.log("User input: " + input);

            try {
                int action = Integer.parseInt(input);
                switch (action) {
                    case 0: return;
                    case 1: displayAvailableActions(); break;
                    case 2: showTotalPopulation(); break;
                    case 3: showVaccinationsPerCapita(); break;
                    case 4: showAverageMarketValue(); break;
                    case 5: showAverageLivableArea(); break;
                    case 6: showMarketValuePerCapita(); break;
                    case 7: showWellnessClusters(); break;
                    default: System.out.println("Invalid action");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number");
            }
        }
    }

    /**
     * Displays the main menu of available actions.
     * The menu includes all possible operations regardless of data availability.
     */
    private void displayMainMenu() {
        System.out.println("Available Actions:");
        System.out.println("0. Exit");
        System.out.println("1. Show available actions");
        System.out.println("2. Show total population");
        System.out.println("3. Show vaccinations per capita");
        System.out.println("4. Show average market value");
        System.out.println("5. Show average livable area");
        System.out.println("6. Show market value per capita");
        System.out.println("7. Show wellness clusters");
    }

    /**
     * Displays the available actions based on currently loaded data.
     * Currently shows all actions; could be enhanced to show only actions
     * available based on loaded data sets.
     */
    private void displayAvailableActions() {
        System.out.println("Available actions based on loaded data:");
        displayMainMenu();
    }

    /**
     * Displays the total population across all ZIP codes.
     * Retrieves data from the DataManager and formats the output.
     */
    private void showTotalPopulation() {
        int total = dataManager.getTotalPopulation();
        System.out.println("Total population: " + total);
    }

    /**
     * Displays vaccination rates per capita for a specified date and vaccination type.
     * Prompts the user for:
     * - A date in YYYY-MM-DD format
     * - Vaccination type (partial/full)
     * Outputs results as ZIP code to vaccination rate mappings.
     */
    private void showVaccinationsPerCapita() {
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        System.out.print("Enter type (partial/full): ");
        String type = scanner.nextLine();

        Map<String, Double> results = dataManager.getVaccinationsPerCapita(
                type, LocalDate.parse(date, dateFormatter));

        results.forEach((zip, rate) ->
                System.out.printf("%s: %.4f%n", zip, rate));
    }

    /**
     * Displays the average market value for properties in a specified ZIP code.
     * Prompts the user for a 5-digit ZIP code and displays the calculated average.
     */
    private void showAverageMarketValue() {
        System.out.print("Enter ZIP code: ");
        String zip = scanner.nextLine();
        int avg = dataManager.getAverageMarketValue(zip);
        System.out.println("Average market value: " + avg);
    }

    /**
     * Displays the average livable area for properties in a specified ZIP code.
     * Prompts the user for a 5-digit ZIP code and displays the calculated average.
     */
    private void showAverageLivableArea() {
        System.out.print("Enter ZIP code: ");
        String zip = scanner.nextLine();
        int avg = dataManager.getAverageLivableArea(zip);
        System.out.println("Average livable area: " + avg);
    }

    /**
     * Displays the total market value per capita for a specified ZIP code.
     * Prompts the user for a 5-digit ZIP code and displays the calculated value.
     */
    private void showMarketValuePerCapita() {
        System.out.print("Enter ZIP code: ");
        String zip = scanner.nextLine();
        int value = dataManager.getMarketValuePerCapita(zip);
        System.out.println("Market value per capita: " + value);
    }

    private void showWellnessClusters() {
        LocalDate date;
        while (true) {
            System.out.print("Enter date (YYYY-MM-DD) for vaccination data: ");
            String dateStr = scanner.nextLine().trim();
            try {
                date = LocalDate.parse(dateStr, dateFormatter);
                logger.log("Cluster date: " + dateStr);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date, please use YYYY-MM-DD.");
            }
        }

        double minRate;
        while (true) {
            System.out.print("Enter minimum full-vaccination rate (0.0-1.0): ");
            String rateStr = scanner.nextLine().trim();
            try {
                minRate = Double.parseDouble(rateStr);
                if (minRate >= 0.0 && minRate <= 1.0) {
                    logger.log("Min rate: " + minRate);
                    break;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid rate, please enter a number between 0.0 and 1.0.");
        }

        int minArea;
        while (true) {
            System.out.print("Enter minimum average livable area (sq ft): ");
            String areaStr = scanner.nextLine().trim();
            try {
                minArea = Integer.parseInt(areaStr);
                if (minArea >= 0) {
                    logger.log("Min area: " + minArea);
                    break;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid area, please enter a non-negative integer.");
        }

        int minPop;
        while (true) {
            System.out.print("Enter minimum population per ZIP: ");
            String popStr = scanner.nextLine().trim();
            try {
                minPop = Integer.parseInt(popStr);
                if (minPop >= 0) {
                    logger.log("Min population: " + minPop);
                    break;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid population, please enter a non-negative integer.");
        }

        List<Set<String>> clusters = dataManager.getWellnessClusters(date, minRate, minArea, minPop);
        if (clusters.isEmpty()) {
            System.out.println("No clusters found matching the criteria.");
        } else {
            for (int i = 0; i < clusters.size(); i++) {
                System.out.printf("Cluster %d: %s%n", i+1, clusters.get(i));
            }
        }
    }
}