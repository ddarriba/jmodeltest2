@echo off
cls
color 1B
mode con:cols=98
mode con:lines=50
if not exist PhyML_3.0_win32.exe goto error
if exist PhyML_3.0_win32.exe then goto launch
echo on


:launch
PhyML_3.0_win32
goto end

:error
echo Error - can't find `PhyML_3.0_win32.exe'
pause
goto end


:end
echo Execution finished
pause
