@echo off
setlocal enabledelayedexpansion

set N=3

cd bin
start cmd /k "java -cp .;../lib/* MasterMain"
start cmd /k "java -cp .;../lib/* ReducerMain"

for /l %%i in (0, 1, %N%-1) do (
    set /a num=%%i
    start cmd /k "java -cp .;../lib/* WorkerMain !num! !N!"
)
