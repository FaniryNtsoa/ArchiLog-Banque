@echo off
REM ========================================
REM   DEMARRAGE WILDFLY - CENTRALISATEUR
REM   Port HTTP: 9080 (offset +1000)
REM ========================================

echo ========================================
echo   WILDFLY - CENTRALISATEUR
echo   Port HTTP:        9080
echo   Port Management:  10190
echo   Port Remoting:    4647
echo ========================================
echo.

REM Vérifier que WILDFLY_HOME est défini
if "%WILDFLY_HOME%"=="" (
    echo ERREUR: Variable WILDFLY_HOME non definie
    echo Exemple: set WILDFLY_HOME=C:\wildfly-37.0.1.Final
    pause
    exit /b 1
)

REM Démarrer WildFly avec offset de port +1000
echo Demarrage de WildFly sur port 9080...
echo.

cd /d "%WILDFLY_HOME%\bin"
call standalone.bat -Djboss.socket.binding.port-offset=1000 -Djboss.server.base.dir="%WILDFLY_HOME%\standalone-centralisateur"

pause
