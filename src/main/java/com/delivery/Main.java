package com.delivery;

import com.delivery.exception.DeliveryServiceNotAllowedException;
import com.delivery.exception.DuplicateTrackingNumberException;
import com.delivery.exception.InvalidParcelException;
import com.delivery.exception.ParcelNotFoundException;
import com.delivery.model.BoxParcel;
import com.delivery.model.DocumentParcel;
import com.delivery.model.FragileParcel;
import com.delivery.model.Parcel;
import com.delivery.service.ParcelManager;
import com.delivery.strategy.DeliveryServiceType;
import com.delivery.strategy.DeliveryStrategy;

import java.util.List;
import java.util.Scanner;

/**
 * Main console application demonstrating:
 * - Polymorphism (Parcel, DocumentParcel, BoxParcel, FragileParcel)
 * - Exception Handling (Checked custom exceptions and validation)
 * - Singleton Pattern (ParcelManager)
 * - Strategy Pattern (DeliveryStrategy implementations)
 *
 * Implemented cleanly without nested if-else structures and without while(true) loops.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ParcelManager manager = ParcelManager.getInstance();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = promptString("Enter your choice (1-5): ");
            System.out.println();

            switch (choice.trim()) {
                case "1":
                    handleAddParcel();
                    break;
                case "2":
                    handleViewAllParcels();
                    break;
                case "3":
                    handleChangeDeliveryService();
                    break;
                case "4":
                    handleMarkAsDelivered();
                    break;
                case "5":
                    System.out.println("Exiting Parcel Delivery System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option! Please enter a number from 1 to 5.");
                    break;
            }
            System.out.println();
        }
    }

    private static void printMainMenu() {
        System.out.println("==================================================");
        System.out.println("             PARCEL DELIVERY SYSTEM               ");
        System.out.println("==================================================");
        System.out.println(" 1. Add Parcel");
        System.out.println(" 2. View All Parcels");
        System.out.println(" 3. Change Delivery Service");
        System.out.println(" 4. Mark as Delivered");
        System.out.println(" 5. Exit");
        System.out.println("==================================================");
    }

    // =========================================================================
    // 1. ADD PARCEL
    // =========================================================================
    private static void handleAddParcel() {
        System.out.println("--- Add New Parcel ---");
        System.out.println("Select Parcel Type:");
        System.out.println(" 1. Document");
        System.out.println(" 2. Box");
        System.out.println(" 3. Fragile");

        int typeChoice = parseParcelTypeChoice(promptString("Enter type choice (1-3): "));
        if (typeChoice == -1) {
            return;
        }

        try {
            String trackingNumber = promptString("Enter Tracking Number (e.g. ABC0001): ").trim().toUpperCase();

            // Validate format
            if (!Parcel.TRACKING_NUMBER_PATTERN.matcher(trackingNumber).matches()) {
                throw new InvalidParcelException("Tracking number must start with 'ABC' followed by 4 digits (e.g., ABC0001)!");
            }

            // Check for duplicate tracking number
            if (manager.hasTrackingNumber(trackingNumber)) {
                throw new DuplicateTrackingNumberException(trackingNumber);
            }

            String receiverName = promptString("Enter Receiver Name: ").trim();
            if (receiverName.isEmpty()) {
                throw new InvalidParcelException("Receiver name cannot be empty!");
            }

            double weight = promptDouble("Enter Weight in kg (> 0): ");
            if (weight <= 0) {
                throw new InvalidParcelException("Weight must be greater than 0 kg!");
            }

            Parcel parcel = createParcelByType(typeChoice, trackingNumber, receiverName, weight);
            if (parcel == null) {
                return;
            }

            manager.addParcel(parcel);
            System.out.printf("Parcel added successfully! Delivery fee: Php %.2f%n", parcel.calculateDeliveryFee());

        } catch (InvalidParcelException | DuplicateTrackingNumberException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid numeric input entered!");
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    private static int parseParcelTypeChoice(String input) {
        try {
            int choice = Integer.parseInt(input.trim());
            if (choice < 1 || choice > 3) {
                System.out.println("Invalid parcel type choice! Returning to menu.");
                return -1;
            }
            return choice;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a valid number.");
            return -1;
        }
    }

    private static Parcel createParcelByType(int typeChoice, String trackingNumber, String receiverName, double weight)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        switch (typeChoice) {
            case 1:
                return buildDocumentParcel(trackingNumber, receiverName, weight);
            case 2:
                return buildBoxParcel(trackingNumber, receiverName, weight);
            case 3:
                return buildFragileParcel(trackingNumber, receiverName, weight);
            default:
                System.out.println("Invalid parcel type choice!");
                return null;
        }
    }

    private static DocumentParcel buildDocumentParcel(String trackingNumber, String receiverName, double weight)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        if (weight > DocumentParcel.MAX_WEIGHT) {
            throw new InvalidParcelException("Document weight cannot exceed 0.5 kg! Current: " + weight + " kg");
        }
        int pages = promptInt("Enter Number of Pages (1 to 100): ");
        if (pages < DocumentParcel.MIN_PAGES || pages > DocumentParcel.MAX_PAGES) {
            throw new InvalidParcelException("Number of pages must be between 1 and 100! Provided: " + pages);
        }

        DeliveryStrategy strategy = selectDeliveryServiceStrategy(weight);
        return new DocumentParcel(trackingNumber, receiverName, weight, strategy, pages);
    }

    private static BoxParcel buildBoxParcel(String trackingNumber, String receiverName, double weight)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        if (weight > BoxParcel.MAX_WEIGHT) {
            throw new InvalidParcelException("Box weight cannot exceed 50 kg! Current: " + weight + " kg");
        }
        int length = promptInt("Enter Length in cm (> 0): ");
        int width = promptInt("Enter Width in cm (> 0): ");
        int height = promptInt("Enter Height in cm (> 0): ");

        if (length <= 0 || width <= 0 || height <= 0) {
            throw new InvalidParcelException("Dimensions must be positive integers!");
        }

        double volumetricWeight = ((double) length * width * height) / BoxParcel.VOLUMETRIC_DIVISOR;
        double chargeableWeight = Math.ceil(Math.max(weight, volumetricWeight));

        DeliveryStrategy strategy = selectDeliveryServiceStrategy(chargeableWeight);
        return new BoxParcel(trackingNumber, receiverName, weight, strategy, length, width, height);
    }

    private static FragileParcel buildFragileParcel(String trackingNumber, String receiverName, double weight)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        if (weight > FragileParcel.MAX_FRAGILE_WEIGHT) {
            throw new InvalidParcelException("Fragile parcel weight cannot exceed 30 kg! Current: " + weight + " kg");
        }
        int length = promptInt("Enter Length in cm (> 0): ");
        int width = promptInt("Enter Width in cm (> 0): ");
        int height = promptInt("Enter Height in cm (> 0): ");

        if (length <= 0 || width <= 0 || height <= 0) {
            throw new InvalidParcelException("Dimensions must be positive integers!");
        }

        String handlingNote = promptString("Enter Handling Note: ").trim();
        if (handlingNote.isEmpty()) {
            throw new InvalidParcelException("Handling note cannot be empty!");
        }

        double volumetricWeight = ((double) length * width * height) / BoxParcel.VOLUMETRIC_DIVISOR;
        double chargeableWeight = Math.ceil(Math.max(weight, volumetricWeight));

        DeliveryStrategy strategy = selectDeliveryServiceStrategy(chargeableWeight);
        return new FragileParcel(trackingNumber, receiverName, weight, strategy, length, width, height, handlingNote);
    }

    /**
     * Prompts the user to select a delivery service strategy.
     * Uses a bounded condition flag without while(true) or nested if-else.
     */
    private static DeliveryStrategy selectDeliveryServiceStrategy(double chargeableWeight) {
        DeliveryStrategy selectedStrategy = null;
        boolean selecting = true;

        while (selecting) {
            System.out.println("Select Delivery Service:");
            System.out.println(" 1. Standard");
            System.out.println(" 2. Express");
            System.out.println(" 3. Same Day");
            String serviceInput = promptString("Enter delivery service choice (1-3): ");

            try {
                int option = Integer.parseInt(serviceInput.trim());
                DeliveryServiceType serviceType = DeliveryServiceType.fromOption(option);
                DeliveryStrategy strategy = serviceType.getStrategy();

                strategy.validateEligibility(chargeableWeight);
                selectedStrategy = strategy;
                selecting = false;
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid selection! Please enter 1, 2, or 3.");
            } catch (DeliveryServiceNotAllowedException e) {
                System.out.println(e.getMessage());
                System.out.println("Please select again.");
            }
        }

        return selectedStrategy;
    }

    // =========================================================================
    // 2. VIEW ALL PARCELS
    // =========================================================================
    private static void handleViewAllParcels() {
        System.out.println("--- All Registered Parcels ---");
        List<Parcel> parcels = manager.getAllParcels();

        if (parcels.isEmpty()) {
            System.out.println("No parcels registered in the system.");
            return;
        }

        String border = "+------------+----------+----------------------+-------------+-----------------+-------------+----------------+-----------+";
        System.out.println(border);
        System.out.printf("| %-10s | %-8s | %-20s | %-11s | %-15s | %-11s | %-14s | %-9s |%n",
                "Tracking", "Type", "Receiver", "Weight (kg)", "Chargeable (kg)", "Service", "Fee (Php)", "Status");
        System.out.println(border);

        for (Parcel p : parcels) {
            System.out.printf("| %-10s | %-8s | %-20s | %11.2f | %15.2f | %-11s | %14.2f | %-9s |%n",
                    p.getTrackingNumber(),
                    p.getParcelType(),
                    truncate(p.getReceiverName(), 20),
                    p.getWeight(),
                    p.getChargeableWeight(),
                    p.getDeliveryStrategy().getServiceName(),
                    p.calculateDeliveryFee(),
                    p.getStatus().getDisplayName());
        }
        System.out.println(border);
        System.out.println("Total parcels: " + parcels.size());
    }

    // =========================================================================
    // 3. CHANGE DELIVERY SERVICE
    // =========================================================================
    private static void handleChangeDeliveryService() {
        System.out.println("--- Change Delivery Service ---");
        if (manager.getParcelCount() == 0) {
            System.out.println("No parcels registered in the system.");
            return;
        }

        String trackingNumber = promptString("Enter Tracking Number: ").trim().toUpperCase();

        try {
            Parcel parcel = manager.findParcel(trackingNumber);

            if (parcel.isDelivered()) {
                System.out.println("Parcel already delivered!");
                return;
            }

            System.out.println("Current delivery service: " + parcel.getDeliveryStrategy().getServiceName());
            System.out.printf("Current delivery fee: Php %.2f%n", parcel.calculateDeliveryFee());
            System.out.println("Select New Delivery Service:");
            System.out.println(" 1. Standard");
            System.out.println(" 2. Express");
            System.out.println(" 3. Same Day");

            String choiceStr = promptString("Enter new service choice (1-3): ");
            int option = Integer.parseInt(choiceStr.trim());
            DeliveryServiceType newServiceType = DeliveryServiceType.fromOption(option);

            parcel.changeDeliveryService(newServiceType.getStrategy());
            System.out.printf("Delivery service changed! New fee: Php %.2f%n", parcel.calculateDeliveryFee());

        } catch (ParcelNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (DeliveryServiceNotAllowedException e) {
            System.out.println(e.getMessage());
            System.out.println("(The parcel keeps its old service)");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid service option! The parcel keeps its old service.");
        }
    }

    // =========================================================================
    // 4. MARK AS DELIVERED
    // =========================================================================
    private static void handleMarkAsDelivered() {
        System.out.println("--- Mark Parcel as Delivered ---");
        if (manager.getParcelCount() == 0) {
            System.out.println("No parcels registered in the system.");
            return;
        }

        String trackingNumber = promptString("Enter Tracking Number: ").trim().toUpperCase();

        try {
            Parcel parcel = manager.findParcel(trackingNumber);

            if (parcel.isDelivered()) {
                System.out.println("Parcel was already delivered.");
                return;
            }

            parcel.markAsDelivered();
            System.out.println("Parcel delivered successfully!");

        } catch (ParcelNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    // =========================================================================
    // UTILITY INPUT HELPERS (NO while(true) LOOPS, NO nested if-else)
    // =========================================================================
    private static String promptString(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    private static int promptInt(String message) {
        int result = 0;
        boolean valid = false;

        while (!valid) {
            System.out.print(message);
            String line = scanner.nextLine().trim();
            try {
                result = Integer.parseInt(line);
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer! Please enter a valid whole number.");
            }
        }

        return result;
    }

    private static double promptDouble(String message) {
        double result = 0.0;
        boolean valid = false;

        while (!valid) {
            System.out.print(message);
            String line = scanner.nextLine().trim();
            try {
                result = Double.parseDouble(line);
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid decimal number! Please enter a valid number (e.g., 2.5).");
            }
        }

        return result;
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
