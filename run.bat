@echo off

cd bin
start cmd /k "java MasterMain"
start cmd /k "java ReducerMain"
start cmd /k "java WorkerMain 0"
start cmd /k "java WorkerMain 1"
start cmd /k "java WorkerMain 2"