@echo off
REM ========================================
REM   DEMARRAGE WILDFLY - SITUATION BANCAIRE
REM   Port HTTP: 8080 (par défaut)
REM ========================================

echo ========================================
echo   WILDFLY - SITUATION BANCAIRE
echo   Port HTTP:        8080
echo   Port Management:  9990
echo   Port Remoting:    4447
echo ========================================
echo.

REM Vérifier que WILDFLY_HOME est défini
if "%WILDFLY_HOME%"=="" (
    echo ERREUR: Variable WILDFLY_HOME non definie
    echo Exemple: set WILDFLY_HOME=C:\wildfly-37.0.1.Final
    pause
    exit /b 1
)

REM Démarrer WildFly (ports par défaut)
echo Demarrage de WildFly sur port 8080...
echo.

cd /d "%WILDFLY_HOME%\bin"
call standalone.bat

pause
