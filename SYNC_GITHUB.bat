@echo off
title OpenBrain â€” Sincronizador GitHub Projects
chcp 65001 > nul
cls

echo =====================================================================
echo          ðŸ§  OPENBRAIN â€” SINCRONIZADOR GITHUB PROJECTS
echo =====================================================================
echo.

cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "scripts\sync_github_projects.ps1"

pause
