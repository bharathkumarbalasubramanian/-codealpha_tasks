@echo off
echo Compiling Java AI Chatbot...
if not exist bin mkdir bin
javac -d bin -sourcepath src src\com\chatbot\App.java
if %errorlevel% equ 0 (
    echo Compilation successful!
) else (
    echo Compilation failed!
)
