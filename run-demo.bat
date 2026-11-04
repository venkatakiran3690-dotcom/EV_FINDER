@echo off
title Smart EV Charging System - Demo Mode
echo ========================================================
echo  Starting Smart EV Station Finder & Billing System
echo  Mode: In-Memory Demo (dev-h2 profile, Zero-Setup)
echo ========================================================
echo.

set "JAVA_CMD=java"
if exist "C:\Users\venka\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe" (
    set "JAVA_CMD=C:\Users\venka\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe"
)

echo Using Java: %JAVA_CMD%
echo Starting server on http://localhost:8080 ...
echo Please wait a few seconds until you see "Started EvChargingApplication"...
echo.

"%JAVA_CMD%" -Dspring.profiles.active=dev-h2 -jar target\ev-charging-system-1.0.0.jar

pause
