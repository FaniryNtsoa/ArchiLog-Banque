@echo off
REM ========================================
REM   DEMARRAGE WILDFLY POUR MODULE PRET
REM   Port HTTP: 8180 (offset +100)
REM ========================================

echo ========================================
echo   WILDFLY - MODULE PRET
echo   Port HTTP:        8180
echo   Port Management:  10090
echo   Port Remoting:    4547
echo ========================================
echo.

REM Vérifier que WILDFLY_HOME est défini
if "%WILDFLY_HOME%"=="" (
    echo ERREUR: Variable WILDFLY_HOME non definie
    echo Exemple: set WILDFLY_HOME=C:\wildfly-37.0.1.Final
    pause
    exit /b 1
)

REM Démarrer WildFly avec offset de port +100
echo Demarrage de WildFly sur port 8180...
echo.

cd /d "%WILDFLY_HOME%\bin"
call standalone.bat -Djboss.socket.binding.port-offset=100 -Djboss.server.base.dir="%WILDFLY_HOME%\standalone-pret"

pause
