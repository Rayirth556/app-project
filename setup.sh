#!/bin/bash

echo "Savora Finance Tracker - Setup Script"
echo "===================================="
echo

echo "Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 11 or higher from https://openjdk.org/"
    exit 1
fi

echo "Java found!"
java -version
echo

echo "Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven from https://maven.apache.org/"
    exit 1
fi

echo "Maven found!"
mvn -version
echo

echo "Building the application..."
mvn clean package
if [ $? -ne 0 ]; then
    echo "ERROR: Build failed. Please check the error messages above."
    exit 1
fi

echo
echo "Build successful!"
echo

echo "IMPORTANT: Before running the application, please ensure:"
echo "1. MySQL server is running"
echo "2. Database 'savora_finance' exists"
echo "3. Tables are created (run schema.sql)"
echo "4. Database credentials are correct in DatabaseConnection.java"
echo

echo "To run the application:"
echo "java -jar target/savora-finance-1.0.0.jar"
echo
