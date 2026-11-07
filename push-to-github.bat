@echo off
title Push EV_FINDER to GitHub
echo ========================================================
echo  Pushing EV_FINDER Project to GitHub
echo  Repository: https://github.com/venkatakiran3690-dotcom/EV_FINDER.git
echo ========================================================
echo.

cd /d "%~dp0"

echo Ensuring branch is 'main'...
git branch -M main

echo Remote URL configured:
git remote -v
echo.

echo Pushing code to GitHub...
echo (If a browser window pops up, click 'Sign in with your browser' to authorize)
echo.

git push -u origin main

echo.
if %ERRORLEVEL% equ 0 (
    echo ========================================================
    echo  SUCCESS! Your project has been pushed to GitHub.
    echo  View it at: https://github.com/venkatakiran3690-dotcom/EV_FINDER
    echo ========================================================
) else (
    echo ========================================================
    echo  Push failed. Check the error message above.
    echo ========================================================
)
echo.
pause
