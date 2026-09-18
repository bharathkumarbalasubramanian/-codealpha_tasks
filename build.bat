@echo off
echo ===================================================
echo Compiling Full-Stack Hotel Reservation System...
echo ===================================================

if not exist out mkdir out

javac -d out ^
  src/com/hotel/model/*.java ^
  src/com/hotel/util/*.java ^
  src/com/hotel/repository/*.java ^
  src/com/hotel/service/*.java ^
  src/com/hotel/api/*.java ^
  src/com/hotel/ui/*.java ^
  src/com/hotel/Main.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compilation successful! Class files built in out/
) else (
    echo.
    echo Compilation failed with error code %ERRORLEVEL%
)
