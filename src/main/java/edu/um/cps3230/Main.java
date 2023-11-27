package edu.um.cps3230;

//IMPORTS
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;


public class Main {

    private static final int CHOICE_RECOMMEND_CURRENT = 1;
    private static final int CHOICE_RECOMMEND_FUTURE = 2;
    private static final int CHOICE_EXIT = 3;
    private static final int MAX_DAYS_LIMIT = 10;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            RetrieveWeather weatherService = new RetrieveWeather();
            RetrieveIP ipService = new RetrieveIP(HttpClients.createDefault());

            Main.runWeatherApp(scanner, weatherService, ipService);
        } catch (Exception e) {
            System.err.println("ERROR --> " + e.getMessage());
        }
    }
    public static void runWeatherApp(Scanner scanner, RetrieveWeather weatherService, RetrieveIP ipService) {
        boolean exit = false;

        while (!exit) {
            displayMenu();
            int choice = getUserChoice(scanner);

            switch (choice) {
                case CHOICE_RECOMMEND_CURRENT:
                    recommendClothingForCurrentLocation(weatherService, ipService);
                    break;
                case CHOICE_RECOMMEND_FUTURE:
                    handleFutureWeatherRecommendation(scanner, weatherService);
                    break;
                case CHOICE_EXIT:
                    exitApplication();
                    exit = true;
                    break;
                default:
                    System.out.println("\nInvalid choice. Please enter a valid option.");
            }
        }
    }

    public static void displayMenu() {
        System.out.println("\nWeatherWear.com");
        System.out.println("---------------");
        System.out.println("1. Recommend clothing for current location");
        System.out.println("2. Recommend clothing for future location");
        System.out.println("3. Exit");
        System.out.print("\nEnter choice: ");
    }

    public static int getUserChoice(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("\nInvalid input. Please enter a number.");
            displayMenu();
            scanner.next();
        }
        return scanner.nextInt();
    }

    public static void recommendClothingForCurrentLocation(RetrieveWeather weatherService, RetrieveIP ipService) {
        String userIPAddress = ipService.fetchIp();
        String currentRecommendation = weatherService.getCurrentWeather(userIPAddress);
        System.out.println("\nCurrent Weather Recommendation:\n" + currentRecommendation);
    }

    public static void handleFutureWeatherRecommendation(Scanner scanner, RetrieveWeather weatherService) {
        String airportIATACode = getValidIATACode(scanner);
        String dateOfArrival = getValidDate(scanner);

        try {
            String futureRecommendation = weatherService.getForecastRecommendation(airportIATACode, dateOfArrival);
            System.out.println("\nFuture Weather Recommendation:\n" + futureRecommendation);
        } catch (IOException e) {
            System.err.println("Error getting future weather recommendation: " + e.getMessage());
        }
    }

    private static void exitApplication() {
        System.out.println("Exiting...");
        System.exit(1);
    }

    private static String getValidDate(Scanner scanner) {
        while (true) {
            System.out.print("Enter date of arrival (YYYY-MM-DD [max: 10 days from current day] --> ");
            String date = scanner.next();

            try {
                LocalDate inputDate = LocalDate.parse(date);

                LocalDate currentDate = LocalDate.now();
                if (inputDate.isBefore(currentDate) || inputDate.isAfter(currentDate.plusDays(MAX_DAYS_LIMIT))) {
                    System.out.println("\nInvalid date. Please enter a date within 10 days from the current day.");
                    continue;
                }

                return date;
            } catch (DateTimeParseException e) {
                System.out.println("\nInvalid date format. Please enter a date in YYYY-MM-DD format.");
            }
        }
    }

    private static String getValidIATACode(Scanner scanner) {
        while (true) {
            System.out.print("Enter airport IATA code: ");
            String IATA = scanner.next();

            if (isValidIATA(IATA)) {
                return IATA;
            } else {
                System.out.println("\nInvalid IATA code. Please enter a valid code.");
            }
        }
    }

    private static boolean isValidIATA(String iataCode) {
        return iataCode.length() == 3 && iataCode.matches("[A-Z]{3}");
    }
}