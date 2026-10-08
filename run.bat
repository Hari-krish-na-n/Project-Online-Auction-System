@echo off
cd /d "%~dp0"
set PATH=%PATH%;C:\Users\harik\Downloads\apache-maven-3.10.0-bin\apache-maven-3.10.0\bin
mvn compile -q
echo.
echo Starting Online Auction System...
echo.
java -cp target\classes com.auction.Main
