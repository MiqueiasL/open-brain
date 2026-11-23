@echo off
title OpenBrain CLI â€” Universal Autonomous AI
chcp 65001 > nul
cls

echo =====================================================================
echo           ðŸ§  OPENBRAIN â€” UNIVERSAL AUTONOMOUS AI CLI
echo            (Windows Â· Linux Â· macOS Â· Kali Â· Android)
echo =====================================================================
echo.

set PATH=C:\Program Files\nodejs;%PATH%

cd /d "%~dp0"
node packages/cli/bin/openbrain.js %*

if "%1"=="" pause
