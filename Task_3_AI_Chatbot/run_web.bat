@echo off
echo Starting Java AI Chatbot Web Server on http://localhost:8080 ...
start http://localhost:8080
java -cp bin com.chatbot.App --port 8080
