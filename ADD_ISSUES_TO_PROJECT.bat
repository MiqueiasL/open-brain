@echo off
title OpenBrain â€” Vincular Issues ao GitHub Project 2
chcp 65001 > nul
cls

echo =====================================================================
echo    ðŸ§  OPENBRAIN â€” VINCULAR ISSUES AO GITHUB PROJECT #2
echo =====================================================================
echo.

echo [*] Adicionando as 14 issues ao seu GitHub Project 2...
echo.

for /L %%i in (1,1,14) do (
    echo [+] Adicionando Issue #%%i...
    gh project item-add 2 --owner MiqueiasL --url "https://github.com/MiqueiasL/open-brain/issues/%%i"
)

echo.
echo =====================================================================
echo  Todas as issues foram vinculadas ao seu GitHub Project!
echo  Acesse: https://github.com/users/MiqueiasL/projects/2
echo =====================================================================
pause
