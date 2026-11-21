@echo off
title Smart EV Charging System - Demo Mode
cd /d "%~dp0"
echo ========================================================
echo  Starting Smart EV Station Finder & Billing System
echo  Mode: In-Memory Demo (dev-h2 profile, Zero-Setup)
echo ========================================================
echo.

set "JAVA_CMD=java"
if exist "C:\Users\venka\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe" (
    set "JAVA_CMD=C:\Users\venka\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe"
)

echo Java Runtime: %JAVA_CMD%
echo Application URL: http://localhost:8080
echo.
echo Launching server and opening browser in 6 seconds...
echo.

start "" cmd /c "timeout /t 6 >nul && start http://localhost:8080"

"%JAVA_CMD%" -jar target\ev-charging-system-1.0.0.jar --spring.profiles.active=dev-h2

pause
