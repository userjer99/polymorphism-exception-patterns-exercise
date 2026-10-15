@echo off
REM Parcel Delivery System - Build & Run Script for Windows

echo ====================================================
echo   Building Parcel Delivery System...
echo ====================================================

if not exist bin mkdir bin

REM Compile all java source files
javac --release 8 -Xlint:-options -d bin src\main\java\com\delivery\*.java src\main\java\com\delivery\model\*.java src\main\java\com\delivery\strategy\*.java src\main\java\com\delivery\service\*.java src\main\java\com\delivery\exception\*.java src\test\java\com\delivery\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    exit /b %ERRORLEVEL%
)

echo Compilation successful!
echo.

if "%1"=="test" (
    echo Running automated test suite...
    java -cp bin com.delivery.ParcelDeliverySystemTest
) else (
    echo Starting Parcel Delivery System CLI...
    java -cp bin com.delivery.Main
)
