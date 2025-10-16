@echo off
echo Savora Finance Tracker - Setup Script
echo ====================================
echo.

echo Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or higher from https://openjdk.org/
    pause
    exit /b 1
)

echo Java found!
echo.

echo Checking Maven installation...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/
    pause
    exit /b 1
)

echo Maven found!
echo.

echo Building the application...
mvn clean package
if %errorlevel% neq 0 (
    echo ERROR: Build failed. Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo Build successful!
echo.

echo IMPORTANT: Before running the application, please ensure:
echo 1. MySQL server is running
echo 2. Database 'savora_finance' exists
echo 3. Tables are created (run schema.sql)
echo 4. Database credentials are correct in DatabaseConnection.java
echo.

echo To run the application:
echo java -jar target/savora-finance-1.0.0.jar
echo.

pause
