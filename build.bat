@echo off
cd src
javac -d ./../bin -cp .;../lib/* *.java
pause