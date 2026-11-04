@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Smart EV Station Finder Project
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "DIR=%~dp0"
if "%JAVA_HOME%"=="" (
    if exist "C:\Users\venka\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.13.9-hotspot" (
        set "JAVA_HOME=C:\Users\venka\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
    )
)

set "MAVEN_OPTS=-Dmaven.repo.local=%DIR%.m2\repository"

"%DIR%apache-maven-3.9.9\bin\mvn.cmd" %*
