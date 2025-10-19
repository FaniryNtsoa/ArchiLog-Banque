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

REM Définir le chemin WildFly
set WILDFLY_HOME=C:\Users\fanir\Documents\utils\wildfly-37.0.1.Final\wildfly-37.0.1.Final

REM Démarrer WildFly avec offset de port +100
echo Demarrage de WildFly sur port 8180...
echo.

cd /d "%WILDFLY_HOME%\bin"
call standalone.bat -Djboss.socket.binding.port-offset=100 -Djboss.server.base.dir="%WILDFLY_HOME%\standalone-pret"

pause
