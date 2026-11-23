@echo off
setlocal
echo ========================================================
echo       ðŸ§  OPENBRAIN ANDROID - COMPILACAO DO APK
echo ========================================================
echo.

cd /d "%~dp0"

echo [1/3] Verificando ambiente Java e Android...
where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERRO] Java Development Kit (JDK) nao encontrado no PATH!
    pause
    exit /b 1
)

set GRADLE_BIN=D:\-Arquivos\Miqueias\Next wave\Projetos\mercado-livre-motorista\build_tools\gradle-8.4\bin\gradle.bat

echo [2/3] Executando Gradle Build (AssembleDebug)...
call "%GRADLE_BIN%" assembleDebug

if %ERRORLEVEL% equ 0 (
    echo.
    echo ========================================================
    echo   BUILD CONCLUIDO COM SUCESSO!
    echo   APK gerado em:
    echo   app\build\outputs\apk\debug\app-debug.apk
    echo ========================================================
    echo.
    echo Deseja instalar agora no smartphone via ADB? (S/N)
    set /p INSTALL=
    if /i "%INSTALL%"=="S" (
        adb install -r app\build\outputs\apk\debug\app-debug.apk
    )
) else (
    echo.
    echo [ERRO] Falha na compilacao do Gradle.
)

pause
