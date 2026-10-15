package com.delivery;

import com.delivery.exception.DeliveryServiceNotAllowedException;
import com.delivery.exception.DuplicateTrackingNumberException;
import com.delivery.exception.InvalidParcelException;
import com.delivery.exception.ParcelNotFoundException;
import com.delivery.model.BoxParcel;
import com.delivery.model.DocumentParcel;
import com.delivery.model.FragileParcel;
import com.delivery.model.Parcel;
import com.delivery.model.ParcelStatus;
import com.delivery.service.ParcelManager;
import com.delivery.strategy.ExpressDeliveryStrategy;
import com.delivery.strategy.SameDayDeliveryStrategy;
import com.delivery.strategy.StandardDeliveryStrategy;

/**
 * Automated test suite verifying:
 * 1. Polymorphism and Inheritance (Parcel -> BoxParcel -> FragileParcel, Parcel -> DocumentParcel)
 * 2. Strategy Pattern (Standard, Express, Same Day fee calculations)
 * 3. Singleton Pattern (ParcelManager single instance and registry)
 * 4. Exception Handling (validation, duplicates, missing parcels, service weight limits)
 */
public class ParcelDeliverySystemTest {
    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   RUNNING PARCEL DELIVERY SYSTEM TEST SUITE     ");
        System.out.println("==================================================");

        ParcelManager manager = ParcelManager.getInstance();
        manager.clear();

        testSingletonPattern();
        testExampleFromPrompt();
        testDocumentParcelValidAndFees();
        testDocumentWeightLimitException();
        testDocumentPagesLimitException();
        testBoxParcelCalculations();
        testBoxWeightLimitException();
        testFragileInheritanceAndBaseFee();
        testFragileWeightLimitException();
        testSameDayWeightConstraint();
        testDuplicateTrackingNumberException();
        testInvalidTrackingNumberPattern();
        testParcelNotFoundException();
        testStatusAndDeliveredLogic();
        testChangeDeliveryService();
        testReceiverNameLengthRestriction();

        System.out.println("==================================================");
        System.out.printf("Results: %d/%d tests PASSED.%n", testsPassed, testsRun);
        if (testsPassed != testsRun) {
            System.err.println("SOME TESTS FAILED!");
            System.exit(1);
        }
        System.out.println("ALL TESTS PASSED SUCCESSFULLY! (100%)");
    }

    private static void assertTrue(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
            return;
        }
        System.err.println("[FAIL] " + testName);
    }

    private static void testSingletonPattern() {
        ParcelManager instance1 = ParcelManager.getInstance();
        ParcelManager instance2 = ParcelManager.getInstance();
        assertTrue("Singleton Pattern: Both references point to same instance", instance1 == instance2);
    }

    private static void testExampleFromPrompt() {
        // Example: FRAGILE, 40 x 20 x 10 cm, 1.8 kg, EXPRESS
        // Volumetric weight = 8000 / 3500 = 2.29 kg -> chargeable weight 3 kg
        // Base fee = (Php 40 × 3) + Php 100 = Php 220
        // Delivery fee = Php 220 × 1.5 = Php 330.00
        try {
            FragileParcel fragile = new FragileParcel(
                    "ABC0001", "John Doe", 1.8,
                    new ExpressDeliveryStrategy(), 40, 20, 10, "Handle with extreme care"
            );

            double volWeight = fragile.getVolumetricWeight();
            double chargeableWeight = fragile.getChargeableWeight();
            double baseFee = fragile.getBaseFee();
            double totalFee = fragile.calculateDeliveryFee();

            assertTrue("Example: Volumetric weight is approx 2.2857 kg", Math.abs(volWeight - (8000.0 / 3500.0)) < 0.001);
            assertTrue("Example: Chargeable weight is rounded up to 3 kg", chargeableWeight == 3.0);
            assertTrue("Example: Base fee is Php 220.00", baseFee == 220.0);
            assertTrue("Example: Delivery fee with Express is Php 330.00", totalFee == 330.0);
            assertTrue("Example: Fragile extends BoxParcel", fragile instanceof BoxParcel);
            assertTrue("Example: Fragile extends Parcel", fragile instanceof Parcel);
        } catch (Exception e) {
            assertTrue("Example from prompt threw unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void testDocumentParcelValidAndFees() {
        try {
            DocumentParcel doc = new DocumentParcel(
                    "ABC0002", "Jane Smith", 0.35, new StandardDeliveryStrategy(), 50
            );
            assertTrue("Document: Chargeable weight equals actual weight", doc.getChargeableWeight() == 0.35);
            assertTrue("Document: Base fee is flat Php 50.00", doc.getBaseFee() == 50.0);
            assertTrue("Document: Standard delivery fee is Php 50.00", doc.calculateDeliveryFee() == 50.0);

            // Change to Express: 50 * 1.5 = 75
            doc.changeDeliveryService(new ExpressDeliveryStrategy());
            assertTrue("Document: Express delivery fee is Php 75.00", doc.calculateDeliveryFee() == 75.0);

            // Change to Same Day: (50 * 2) + 100 = 200
            doc.changeDeliveryService(new SameDayDeliveryStrategy());
            assertTrue("Document: Same Day delivery fee is Php 200.00", doc.calculateDeliveryFee() == 200.0);
        } catch (Exception e) {
            assertTrue("Document test unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void testDocumentWeightLimitException() {
        try {
            new DocumentParcel("ABC0003", "Alice", 0.51, new StandardDeliveryStrategy(), 20);
            assertTrue("Document weight > 0.5 kg should throw InvalidParcelException", false);
        } catch (InvalidParcelException e) {
            assertTrue("Document weight > 0.5 kg threw InvalidParcelException as expected", true);
        } catch (Exception e) {
            assertTrue("Document weight test threw wrong exception", false);
        }
    }

    private static void testDocumentPagesLimitException() {
        try {
            new DocumentParcel("ABC0004", "Bob", 0.2, new StandardDeliveryStrategy(), 0);
            assertTrue("Document pages < 1 should throw InvalidParcelException", false);
        } catch (InvalidParcelException e) {
            assertTrue("Document pages < 1 threw InvalidParcelException as expected", true);
        } catch (Exception e) {
            assertTrue("Document pages test threw wrong exception", false);
        }

        try {
            new DocumentParcel("ABC0005", "Bob", 0.2, new StandardDeliveryStrategy(), 101);
            assertTrue("Document pages > 100 should throw InvalidParcelException", false);
        } catch (InvalidParcelException e) {
            assertTrue("Document pages > 100 threw InvalidParcelException as expected", true);
        } catch (Exception e) {
            assertTrue("Document pages test threw wrong exception", false);
        }
    }

    private static void testBoxParcelCalculations() {
        try {
            // Actual weight = 5.0 kg, dimensions 20x20x20 = 8000 / 3500 = 2.28 kg -> max is 5.0 -> ceil = 5 kg
            BoxParcel box = new BoxParcel("ABC0006", "Charlie", 5.0, new StandardDeliveryStrategy(), 20, 20, 20);
            assertTrue("Box: Chargeable weight is 5.0 kg when actual > volumetric", box.getChargeableWeight() == 5.0);
            assertTrue("Box: Base fee is 5 * 40 = 200", box.getBaseFee() == 200.0);
            assertTrue("Box: Standard fee is 200", box.calculateDeliveryFee() == 200.0);
        } catch (Exception e) {
            assertTrue("Box calculation threw unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void testBoxWeightLimitException() {
        try {
            new BoxParcel("ABC0007", "Charlie", 50.1, new StandardDeliveryStrategy(), 10, 10, 10);
            assertTrue("Box weight > 50 kg should throw InvalidParcelException", false);
        } catch (InvalidParcelException e) {
            assertTrue("Box weight > 50 kg threw InvalidParcelException as expected", true);
        } catch (Exception e) {
            assertTrue("Box weight test threw wrong exception", false);
        }
    }

    private static void testFragileInheritanceAndBaseFee() {
        try {
            FragileParcel fragile = new FragileParcel(
                    "ABC0008", "David", 2.0, new StandardDeliveryStrategy(), 10, 10, 10, "Glass items inside"
            );
            // Volumetric: 1000 / 3500 = 0.285 kg. Actual = 2.0. Max = 2.0 -> ceil = 2.0 kg.
            // Box base fee = 2 * 40 = 80. Fragile base fee = 80 + 100 = 180.
            assertTrue("Fragile base fee includes +100 handling fee (Php 180.00)", fragile.getBaseFee() == 180.0);
            assertTrue("Fragile handling note preserved", "Glass items inside".equals(fragile.getHandlingNote()));
        } catch (Exception e) {
            assertTrue("Fragile fee calculation threw unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void testFragileWeightLimitException() {
        try {
            new FragileParcel("ABC0009", "David", 30.5, new StandardDeliveryStrategy(), 10, 10, 10, "Fragile");
            assertTrue("Fragile weight > 30 kg should throw InvalidParcelException", false);
        } catch (InvalidParcelException e) {
            assertTrue("Fragile weight > 30 kg threw InvalidParcelException as expected", true);
        } catch (Exception e) {
            assertTrue("Fragile weight test threw wrong exception", false);
        }
    }

    private static void testSameDayWeightConstraint() {
        try {
            // Actual weight 11 kg -> chargeable weight 11 kg (> 10 kg)
            new BoxParcel("ABC0010", "Eve", 11.0, new SameDayDeliveryStrategy(), 10, 10, 10);
            assertTrue("Same Day delivery for parcel > 10 kg should throw DeliveryServiceNotAllowedException", false);
        } catch (DeliveryServiceNotAllowedException e) {
            assertTrue("Same Day delivery > 10 kg threw DeliveryServiceNotAllowedException: " + e.getMessage(),
                    e.getMessage().contains("Same Day delivery is only for parcels up to 10 kg!"));
        } catch (Exception e) {
            assertTrue("Same Day test threw wrong exception", false);
        }
    }

    private static void testDuplicateTrackingNumberException() {
        ParcelManager manager = ParcelManager.getInstance();
        try {
            BoxParcel p1 = new BoxParcel("ABC0011", "Frank", 2.0, new StandardDeliveryStrategy(), 10, 10, 10);
            BoxParcel p2 = new BoxParcel("ABC0011", "Frank Dup", 3.0, new StandardDeliveryStrategy(), 10, 10, 10);
            manager.addParcel(p1);
            manager.addParcel(p2);
            assertTrue("Duplicate tracking number should throw DuplicateTrackingNumberException", false);
        } catch (DuplicateTrackingNumberException e) {
            assertTrue("Duplicate tracking number threw DuplicateTrackingNumberException as expected", true);
        } catch (Exception e) {
            assertTrue("Duplicate tracking number test threw wrong exception", false);
        }
    }

    private static void testInvalidTrackingNumberPattern() {
        try {
            new BoxParcel("XYZ1234", "Grace", 2.0, new StandardDeliveryStrategy(), 10, 10, 10);
            assertTrue("Invalid tracking pattern (XYZ1234) should throw InvalidParcelException", false);
        } catch (InvalidParcelException e) {
            assertTrue("Invalid tracking pattern threw InvalidParcelException as expected", true);
        } catch (Exception e) {
            assertTrue("Invalid tracking pattern test threw wrong exception", false);
        }

        try {
            new BoxParcel("ABC123", "Grace", 2.0, new StandardDeliveryStrategy(), 10, 10, 10); // only 3 digits
            assertTrue("Tracking pattern with 3 digits should throw InvalidParcelException", false);
        } catch (InvalidParcelException e) {
            assertTrue("Tracking pattern with 3 digits threw InvalidParcelException as expected", true);
        } catch (Exception e) {
            assertTrue("Invalid tracking pattern test threw wrong exception", false);
        }
    }

    private static void testParcelNotFoundException() {
        ParcelManager manager = ParcelManager.getInstance();
        try {
            manager.findParcel("ABC9999");
            assertTrue("Finding non-existent parcel should throw ParcelNotFoundException", false);
        } catch (ParcelNotFoundException e) {
            assertTrue("Finding non-existent parcel threw ParcelNotFoundException as expected", true);
        }
    }

    private static void testStatusAndDeliveredLogic() {
        try {
            BoxParcel p = new BoxParcel("ABC0012", "Hank", 2.0, new StandardDeliveryStrategy(), 10, 10, 10);
            assertTrue("New parcel has PENDING status", p.getStatus() == ParcelStatus.PENDING && !p.isDelivered());
            p.markAsDelivered();
            assertTrue("Parcel marked as delivered has DELIVERED status", p.getStatus() == ParcelStatus.DELIVERED && p.isDelivered());
        } catch (Exception e) {
            assertTrue("Status test threw unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void testChangeDeliveryService() {
        try {
            // Box of 15 kg
            BoxParcel heavyBox = new BoxParcel("ABC0013", "Ivy", 15.0, new StandardDeliveryStrategy(), 10, 10, 10);
            // Change to Express should work
            heavyBox.changeDeliveryService(new ExpressDeliveryStrategy());
            assertTrue("Changed to Express successfully", "Express".equals(heavyBox.getDeliveryStrategy().getServiceName()));

            // Attempt change to Same Day on 15 kg parcel should fail
            try {
                heavyBox.changeDeliveryService(new SameDayDeliveryStrategy());
                assertTrue("Change to Same Day on 15 kg should throw DeliveryServiceNotAllowedException", false);
            } catch (DeliveryServiceNotAllowedException e) {
                assertTrue("Change to Same Day threw DeliveryServiceNotAllowedException as expected", true);
                assertTrue("Original strategy retained after failed change", "Express".equals(heavyBox.getDeliveryStrategy().getServiceName()));
            }
        } catch (Exception e) {
            assertTrue("Change delivery service threw unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void testReceiverNameLengthRestriction() {
        try {
            // Exactly 30 alphabetic characters - should succeed
            String name30 = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCD";
            BoxParcel p1 = new BoxParcel("ABC0014", name30, 2.0, new StandardDeliveryStrategy(), 10, 10, 10);
            assertTrue("Receiver name of exactly 30 characters is accepted", name30.equals(p1.getReceiverName()));

            // 31 characters - should fail with InvalidParcelException
            String name31 = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDE";
            try {
                new BoxParcel("ABC0015", name31, 2.0, new StandardDeliveryStrategy(), 10, 10, 10);
                assertTrue("Receiver name exceeding 30 characters should throw InvalidParcelException", false);
            } catch (InvalidParcelException e) {
                assertTrue("Receiver name exceeding 30 characters threw InvalidParcelException as expected", true);
            }

            // Name with digits/numbers - should fail with InvalidParcelException
            try {
                new BoxParcel("ABC0016", "John Doe 123", 2.0, new StandardDeliveryStrategy(), 10, 10, 10);
                assertTrue("Receiver name containing numbers should throw InvalidParcelException", false);
            } catch (InvalidParcelException e) {
                assertTrue("Receiver name containing numbers threw InvalidParcelException as expected", true);
            }

            // Name with invalid symbols - should fail with InvalidParcelException
            try {
                new BoxParcel("ABC0017", "John@Doe#", 2.0, new StandardDeliveryStrategy(), 10, 10, 10);
                assertTrue("Receiver name containing symbols should throw InvalidParcelException", false);
            } catch (InvalidParcelException e) {
                assertTrue("Receiver name containing symbols threw InvalidParcelException as expected", true);
            }

            // Dimensions > 300 cm - should fail with InvalidParcelException
            try {
                new BoxParcel("ABC0018", "Valid Name", 2.0, new StandardDeliveryStrategy(), 301, 10, 10);
                assertTrue("Dimension exceeding 300 cm should throw InvalidParcelException", false);
            } catch (InvalidParcelException e) {
                assertTrue("Dimension exceeding 300 cm threw InvalidParcelException as expected", true);
            }

            // Handling note > 100 chars - should fail with InvalidParcelException
            try {
                StringBuilder longNote = new StringBuilder();
                for (int i = 0; i < 101; i++) {
                    longNote.append("A");
                }
                new FragileParcel("ABC0019", "Valid Name", 2.0, new StandardDeliveryStrategy(), 10, 10, 10, longNote.toString());
                assertTrue("Handling note exceeding 100 chars should throw InvalidParcelException", false);
            } catch (InvalidParcelException e) {
                assertTrue("Handling note exceeding 100 chars threw InvalidParcelException as expected", true);
            }
        } catch (Exception e) {
            assertTrue("Receiver name test unexpected exception: " + e.getMessage(), false);
        }
    }
}
