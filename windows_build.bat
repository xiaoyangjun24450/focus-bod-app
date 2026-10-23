@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\windows_build.ps1" %*
exit /b %ERRORLEVEL%

