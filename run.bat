@echo off
echo Starting Savora Finance Tracker...
echo.

java -jar target/savora-finance-1.0.0.jar

if %errorlevel% neq 0 (
    echo.
    echo Application failed to start. Please check:
    echo 1. Java is installed and in PATH
    echo 2. MySQL server is running
    echo 3. Database connection is configured correctly
    echo 4. Application was built successfully
    echo.
    pause
)
