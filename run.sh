#!/bin/bash
# Parcel Delivery System - Build & Run Script for Linux / macOS

set -e

echo "===================================================="
echo "  Building Parcel Delivery System..."
echo "===================================================="

mkdir -p bin

javac --release 8 -Xlint:-options -d bin \
    src/main/java/com/delivery/*.java \
    src/main/java/com/delivery/model/*.java \
    src/main/java/com/delivery/strategy/*.java \
    src/main/java/com/delivery/service/*.java \
    src/main/java/com/delivery/exception/*.java \
    src/test/java/com/delivery/*.java

echo "Compilation successful!"
echo ""

if [ "$1" = "test" ]; then
    echo "Running automated test suite..."
    java -cp bin com.delivery.ParcelDeliverySystemTest
else
    echo "Starting Parcel Delivery System CLI..."
    java -cp bin com.delivery.Main
fi
