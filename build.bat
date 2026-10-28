@echo off
REM ============================================================
REM ATM Transaction System — Build Script for Windows
REM ============================================================
setlocal enabledelayedexpansion

echo [ATM] Building ATM Transaction System...

REM Check Java
where javac >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] javac is not found in PATH. Please install JDK 21 and configure PATH.
    pause
    exit /b 1
)

REM Create bin directory
if not exist "bin" mkdir "bin"

REM Collect all java files
echo [ATM] Collecting Java source files...
dir /s /b src\main\java\*.java src\test\java\*.java > sources.txt

REM Compile
echo [ATM] Compiling sources with JDK...
javac -encoding UTF-8 -d bin -cp "bin;lib/*" @sources.txt
set COMPILE_STATUS=%ERRORLEVEL%
del sources.txt

if %COMPILE_STATUS% equ 0 (
    echo [ATM] Compilation SUCCESSFUL! Classes compiled to bin\
) else (
    echo [ATM] Compilation FAILED with error code %COMPILE_STATUS%
    exit /b %COMPILE_STATUS%
)
