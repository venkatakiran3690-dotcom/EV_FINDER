@echo off
title Smart EV Charging System
echo ========================================================
echo Starting Smart EV Charging System (Java 21 Mode)...
echo ========================================================

set "JAVA_21=C:\Users\venka\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe"

if exist "%JAVA_21%" (
    echo Using Java 21: "%JAVA_21%"
    "%JAVA_21%" -jar target\ev-charging-system-1.0.0.jar --spring.profiles.active=dev-h2
) else (
    echo Java 21 not found at default Adoptium path, trying default java...
    java -jar target\ev-charging-system-1.0.0.jar --spring.profiles.active=dev-h2
)

pause
