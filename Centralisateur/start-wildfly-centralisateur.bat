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

REM Définir le chemin WildFly
set WILDFLY_HOME=C:\Users\fanir\Documents\utils\wildfly-37.0.1.Final\wildfly-37.0.1.Final

REM Démarrer WildFly avec offset de port +1000
echo Demarrage de WildFly sur port 9080...
echo.

cd /d "%WILDFLY_HOME%\bin"
call standalone.bat -Djboss.socket.binding.port-offset=1000 -Djboss.server.base.dir="%WILDFLY_HOME%\standalone-centralisateur"

pause
