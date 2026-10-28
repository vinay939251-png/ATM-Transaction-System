@echo off
REM ============================================================
REM ATM Transaction System — Automated Test Suite Script
REM ============================================================

REM Check if compiled
if not exist "bin\com\atm\test\ATMSuiteTestRunner.class" (
    echo [ATM] Compiling tests...
    call build.bat
    if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
)

echo [ATM] Running automated test suite...
java -cp "bin;lib/*" com.atm.test.ATMSuiteTestRunner
