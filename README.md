# Parcel Delivery System

A robust, object-oriented Parcel Delivery System implemented in Java. This project demonstrates core Object-Oriented Programming (OOP) concepts and software design patterns: **Polymorphism**, **Custom Exception Handling**, the **Singleton Pattern**, and the **Strategy Pattern**.

The implementation strictly avoids nested `if-else` blocks and `while (true)` loops, leveraging guard clauses, polymorphism, and condition-bounded iterations for maximum readability and maintainability.

---

## 📋 Architectural Highlights & Design Patterns

### 1. Polymorphism & Inheritance
- **Base Class**: [`Parcel`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/model/Parcel.java)
  - Declares abstract methods `getChargeableWeight()`, `getBaseFee()`, and `getParcelType()`.
  - Encapsulates common attributes: `trackingNumber`, `receiverName`, `weight`, `deliveryStrategy`, and `status`.
- **Subclasses**:
  - [`DocumentParcel`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/model/DocumentParcel.java): Flat base fee of Php 50, maximum weight 0.5 kg, page count (1–100).
  - [`BoxParcel`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/model/BoxParcel.java): Base fee = Php 40 × chargeable weight, volumetric weight calculation `(L × W × H) / 3500`, maximum weight 50 kg.
  - [`FragileParcel`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/model/FragileParcel.java): **Extends `BoxParcel`**, inherits volumetric calculations, adds handling note and a Php 100 handling fee (`super.getBaseFee() + 100`), maximum weight 30 kg.

### 2. Strategy Design Pattern
- **Interface**: [`DeliveryStrategy`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/strategy/DeliveryStrategy.java)
  - `double calculateFee(double baseFee)`
  - `void validateEligibility(double chargeableWeight) throws DeliveryServiceNotAllowedException`
- **Concrete Strategies**:
  - [`StandardDeliveryStrategy`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/strategy/StandardDeliveryStrategy.java): `fee = baseFee`
  - [`ExpressDeliveryStrategy`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/strategy/ExpressDeliveryStrategy.java): `fee = baseFee * 1.5`
  - [`SameDayDeliveryStrategy`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/strategy/SameDayDeliveryStrategy.java): `fee = (baseFee * 2) + 100`, restricted to parcels with chargeable weight $\le 10$ kg.

### 3. Singleton Design Pattern
- **Class**: [`ParcelManager`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/service/ParcelManager.java)
  - Manages the in-memory parcel repository.
  - Implemented using the thread-safe **Bill Pugh Singleton Pattern** (Initialization-on-demand holder idiom), eliminating nested conditionals and synchronization overhead while guaranteeing lazy loading.

### 4. Custom Exception Handling
- [`ParcelException`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/exception/ParcelException.java): Base checked exception.
- [`DuplicateTrackingNumberException`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/exception/DuplicateTrackingNumberException.java): Raised when adding a parcel whose tracking number already exists.
- [`ParcelNotFoundException`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/exception/ParcelNotFoundException.java): Raised when a tracking number is not found.
- [`InvalidParcelException`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/exception/InvalidParcelException.java): Raised for format, weight, dimension, or field validation errors.
- [`DeliveryServiceNotAllowedException`](file:///c:/Users/ace%20justine%20cayabyab/Documents/OOPROG%20Finals/src/main/java/com/delivery/exception/DeliveryServiceNotAllowedException.java): Raised when attempting Same Day delivery for parcels exceeding 10 kg.

---

## 📦 Parcel Types & Pricing Rules

| Type | Max Weight | Volumetric Divisor | Chargeable Weight Rule | Base Fee Formula |
|---|---|---|---|---|
| **Document** | 0.5 kg | N/A | Actual weight | Flat Php 50.00 |
| **Box** | 50.0 kg | 3500 | $\lceil\max(\text{actual}, \frac{L \times W \times H}{3500})\rceil$ | Php 40.00 × Chargeable Weight |
| **Fragile** | 30.0 kg | 3500 | Same as Box | Box Base Fee + Php 100.00 |

### Delivery Service Fee Multipliers
1. **Standard**: `Delivery Fee = Base Fee`
2. **Express**: `Delivery Fee = Base Fee × 1.5`
3. **Same Day**: `Delivery Fee = (Base Fee × 2) + Php 100.00` *(Chargeable weight must be $\le 10$ kg)*

---

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK 8 or higher)
- Git

### Project Structure
```
polymorphism-exception-patterns-exercise/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── delivery/
│   │               ├── Main.java
│   │               ├── exception/
│   │               │   ├── DeliveryServiceNotAllowedException.java
│   │               │   ├── DuplicateTrackingNumberException.java
│   │               │   ├── InvalidParcelException.java
│   │               │   ├── ParcelException.java
│   │               │   └── ParcelNotFoundException.java
│   │               ├── model/
│   │               │   ├── BoxParcel.java
│   │               │   ├── DocumentParcel.java
│   │               │   ├── FragileParcel.java
│   │               │   ├── Parcel.java
│   │               │   └── ParcelStatus.java
│   │               ├── service/
│   │               │   └── ParcelManager.java
│   │               └── strategy/
│   │                   ├── DeliveryServiceType.java
│   │                   ├── DeliveryStrategy.java
│   │                   ├── ExpressDeliveryStrategy.java
│   │                   ├── SameDayDeliveryStrategy.java
│   │                   └── StandardDeliveryStrategy.java
│   └── test/
│       └── java/
│           └── com/
│               └── delivery/
│                   └── ParcelDeliverySystemTest.java
├── run.bat
├── run.sh
├── .gitignore
└── README.md
```

### Running the Application

#### Windows:
```cmd
.\run.bat
```

#### Linux / macOS:
```bash
chmod +x run.sh
./run.sh
```

#### Manual Compilation & Run:
```bash
javac -d bin $(find src -name "*.java")
java -cp bin com.delivery.Main
```

### Running Automated Tests
The project includes a standalone test suite with 32 assertion tests covering all validation rules, polymorphism, exceptions, and patterns.

#### Windows:
```cmd
.\run.bat test
```

#### Linux / macOS:
```bash
./run.sh test
```

---

## 💻 GitHub Submission
To link and push this project to your GitHub repository named `polymorphism-exception-patterns-exercise`:

```bash
git init
git add .
git commit -m "Initial commit: Parcel Delivery System implementation"
git branch -M main
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/polymorphism-exception-patterns-exercise.git
git push -u origin main
```
