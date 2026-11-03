@echo off
title EV Charging Finder Diagnostic & Auto-Start Tool
echo ===================================================
echo EV Charging Finder - Diagnostic & Auto-Start Script
echo ===================================================
echo.

echo [1/5] Checking Node.js installation...
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is NOT installed! 
    echo Please install Node.js (version 18 or newer) from: https://nodejs.org/
    echo after installing Node.js, restart VS Code and run this script again.
    echo.
    pause
    exit /b
)
echo Node.js is successfully installed.
echo.

echo [2/5] Terminating orphan processes on ports 5000 & 5173...
:: Kill port 5000
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5000 ^| findstr LISTENING') do (
    echo Found process PID %%a on port 5000. Terminating...
    taskkill /F /PID %%a >nul 2>&1
)
:: Kill port 5173
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173 ^| findstr LISTENING') do (
    echo Found process PID %%a on port 5173. Terminating...
    taskkill /F /PID %%a >nul 2>&1
)
echo Ports are now fully cleared and ready.
echo.

echo [3/5] Verifying node_modules dependencies...
if not exist node_modules (
    echo Installing root dependencies...
    call npm install
)
if not exist backend\node_modules (
    echo Installing backend dependencies...
    call npm install --prefix backend
)
if not exist frontend\node_modules (
    echo Installing frontend dependencies...
    call npm install --prefix frontend
)
echo All dependencies are set up.
echo.

echo [4/5] Testing backend server standalone...
echo Starting backend server in background test mode...
if exist backend_test_log.txt del backend_test_log.txt
start /b "EV Backend Test" cmd /c "node backend/server.js > backend_test_log.txt 2>&1"
echo Waiting 4 seconds for server to bind...
timeout /t 4 >nul

if exist backend_test_log.txt (
    echo.
    echo --- Standalone Backend Boot Logs ---
    type backend_test_log.txt
    echo -------------------------------------
)

echo.
echo [5/5] Launching full application (Frontend + Backend concurrently)...
echo Executing npm run dev...
echo (You can close this script window with Ctrl+C to terminate both servers)
echo.
call npm run dev
pause
