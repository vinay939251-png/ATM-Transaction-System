@echo off
REM ============================================================
REM ATM Transaction System — One-Click Launch
REM Starts XAMPP MariaDB, initializes the database safely, and launches the UI.
REM ============================================================
setlocal
cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-atm.ps1"
set EXIT_CODE=%ERRORLEVEL%

if not "%EXIT_CODE%"=="0" (
    echo.
    echo [ATM] Launch failed with exit code %EXIT_CODE%.
    pause
)
exit /b %EXIT_CODE%
