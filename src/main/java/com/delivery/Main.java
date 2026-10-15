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
        int typeChoice = promptParcelType();
        String trackingNumber = promptTrackingNumber();
        String receiverName = promptReceiverName();

        try {
            Parcel parcel = createParcelByType(typeChoice, trackingNumber, receiverName);
            manager.addParcel(parcel);
            System.out.printf("Parcel added successfully! Delivery fee: Php %.2f%n", parcel.calculateDeliveryFee());
        } catch (DuplicateTrackingNumberException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (InvalidParcelException | DeliveryServiceNotAllowedException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    private static int promptParcelType() {
        int choice = 0;
        boolean valid = false;
        while (!valid) {
            System.out.println("Select Parcel Type:");
            System.out.println(" 1. Document");
            System.out.println(" 2. Box");
            System.out.println(" 3. Fragile");
            String input = promptString("Enter type choice (1-3): ");
            try {
                choice = Integer.parseInt(input.trim());
                if (choice >= 1 && choice <= 3) {
                    valid = true;
                }
                if (!valid) {
                    System.out.println("Invalid parcel type choice! Please enter 1, 2, or 3.");
                    System.out.println();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number (1-3).");
                System.out.println();
            }
        }
        return choice;
    }

    private static String promptTrackingNumber() {
        String tracking = "";
        boolean valid = false;
        while (!valid) {
            tracking = promptString("Enter Tracking Number (e.g. ABC0001): ").trim().toUpperCase();
            if (!Parcel.TRACKING_NUMBER_PATTERN.matcher(tracking).matches()) {
                System.out.println("Error: Tracking number must start with 'ABC' followed by 4 digits (e.g., ABC0001)!");
                continue;
            }
            if (manager.hasTrackingNumber(tracking)) {
                System.out.println("Error: Tracking number '" + tracking + "' already exists in the system!");
                continue;
            }
            valid = true;
        }
        return tracking;
    }

    private static String promptReceiverName() {
        String name = "";
        boolean valid = false;
        while (!valid) {
            name = promptString("Enter Receiver Name: ").trim();
            if (name.isEmpty()) {
                System.out.println("Error: Receiver name cannot be empty!");
                continue;
            }
            valid = true;
        }
        return name;
    }

    private static double promptWeight(double maxWeight) {
        double weight = 0.0;
        boolean valid = false;
        String maxWeightDisplay = (maxWeight == (long) maxWeight) ? String.format("%d", (long) maxWeight) : String.valueOf(maxWeight);
        String promptMsg = "Enter Weight in kg (> 0 kg - " + maxWeightDisplay + " kg): ";

        while (!valid) {
            weight = promptDouble(promptMsg);
            if (weight <= 0) {
                System.out.println("Error: Weight must be greater than 0 kg!");
                continue;
            }
            if (weight > maxWeight) {
                System.out.println("Error: Weight cannot exceed " + maxWeightDisplay + " kg!");
                continue;
            }
            valid = true;
        }
        return weight;
    }

    private static int promptPages() {
        int pages = 0;
        boolean valid = false;
        while (!valid) {
            pages = promptInt("Enter Number of Pages (1 - 100): ");
            if (pages < DocumentParcel.MIN_PAGES || pages > DocumentParcel.MAX_PAGES) {
                System.out.println("Error: Number of pages must be between " + DocumentParcel.MIN_PAGES + " and " + DocumentParcel.MAX_PAGES + "!");
                continue;
            }
            valid = true;
        }
        return pages;
    }

    private static int promptDimension(String dimensionName) {
        int val = 0;
        boolean valid = false;
        while (!valid) {
            val = promptInt("Enter " + dimensionName + " in cm (> 0 cm): ");
            if (val <= 0) {
                System.out.println("Error: " + dimensionName + " must be greater than 0 cm!");
                continue;
            }
            valid = true;
        }
        return val;
    }

    private static String promptHandlingNote() {
        String note = "";
        boolean valid = false;
        while (!valid) {
            note = promptString("Enter Handling Note (non-empty): ").trim();
            if (note.isEmpty()) {
                System.out.println("Error: Handling note cannot be empty!");
                continue;
            }
            valid = true;
        }
        return note;
    }

    private static Parcel createParcelByType(int typeChoice, String trackingNumber, String receiverName)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        switch (typeChoice) {
            case 1:
                return buildDocumentParcel(trackingNumber, receiverName);
            case 2:
                return buildBoxParcel(trackingNumber, receiverName);
            case 3:
                return buildFragileParcel(trackingNumber, receiverName);
            default:
                throw new InvalidParcelException("Invalid parcel type choice!");
        }
    }

    private static DocumentParcel buildDocumentParcel(String trackingNumber, String receiverName)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        double weight = promptWeight(DocumentParcel.MAX_WEIGHT);
        int pages = promptPages();
        DeliveryStrategy strategy = selectDeliveryServiceStrategy(weight);
        return new DocumentParcel(trackingNumber, receiverName, weight, strategy, pages);
    }

    private static BoxParcel buildBoxParcel(String trackingNumber, String receiverName)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        double weight = promptWeight(BoxParcel.MAX_WEIGHT);
        int length = promptDimension("Length");
        int width = promptDimension("Width");
        int height = promptDimension("Height");

        double volumetricWeight = ((double) length * width * height) / BoxParcel.VOLUMETRIC_DIVISOR;
        double chargeableWeight = Math.ceil(Math.max(weight, volumetricWeight));

        DeliveryStrategy strategy = selectDeliveryServiceStrategy(chargeableWeight);
        return new BoxParcel(trackingNumber, receiverName, weight, strategy, length, width, height);
    }

    private static FragileParcel buildFragileParcel(String trackingNumber, String receiverName)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        double weight = promptWeight(FragileParcel.MAX_FRAGILE_WEIGHT);
        int length = promptDimension("Length");
        int width = promptDimension("Width");
        int height = promptDimension("Height");
        String handlingNote = promptHandlingNote();

        double volumetricWeight = ((double) length * width * height) / BoxParcel.VOLUMETRIC_DIVISOR;
        double chargeableWeight = Math.ceil(Math.max(weight, volumetricWeight));

        DeliveryStrategy strategy = selectDeliveryServiceStrategy(chargeableWeight);
        return new FragileParcel(trackingNumber, receiverName, weight, strategy, length, width, height, handlingNote);
    }

    /**
     * Prompts the user to select a delivery service strategy.
     * Stays on prompt until a valid allowed service is chosen.
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

            String currentServiceName = parcel.getDeliveryStrategy().getServiceName();
            System.out.println("Current delivery service: " + currentServiceName);
            System.out.printf("Current delivery fee: Php %.2f%n", parcel.calculateDeliveryFee());

            DeliveryServiceType newServiceType = promptNewDeliveryService();

            if (newServiceType.getDisplayName().equalsIgnoreCase(currentServiceName)) {
                System.out.println("Delivery Service is Already " + currentServiceName + "!");
                return;
            }

            parcel.changeDeliveryService(newServiceType.getStrategy());
            System.out.printf("Delivery service changed! New fee: Php %.2f%n", parcel.calculateDeliveryFee());

        } catch (ParcelNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (DeliveryServiceNotAllowedException e) {
            System.out.println(e.getMessage());
            System.out.println("(The parcel keeps its old service)");
        }
    }

    private static DeliveryServiceType promptNewDeliveryService() {
        DeliveryServiceType selectedType = null;
        boolean valid = false;

        while (!valid) {
            System.out.println("Select New Delivery Service:");
            System.out.println(" 1. Standard");
            System.out.println(" 2. Express");
            System.out.println(" 3. Same Day");
            String choiceStr = promptString("Enter new service choice (1-3): ");

            try {
                int option = Integer.parseInt(choiceStr.trim());
                selectedType = DeliveryServiceType.fromOption(option);
                valid = true;
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid service option! Please enter 1, 2, or 3.");
            }
        }

        return selectedType;
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
