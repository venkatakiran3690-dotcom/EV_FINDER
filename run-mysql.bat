@echo off
title Smart EV Charging System - MySQL Production Mode
echo ========================================================
echo  Starting Smart EV Station Finder & Billing System
echo  Mode: Local MySQL Database (port 3306)
echo ========================================================
echo.

set "JAVA_CMD=java"
if exist "C:\Users\venka\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe" (
    set "JAVA_CMD=C:\Users\venka\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe"
)

echo Using Java: %JAVA_CMD%
echo Starting server on http://localhost:8080 ...
echo Ensure MySQL is running on port 3306 with database 'smartev_db'.
echo.

"%JAVA_CMD%" -jar target\ev-charging-system-1.0.0.jar

pause
