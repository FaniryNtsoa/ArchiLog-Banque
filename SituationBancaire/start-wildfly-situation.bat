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

REM Définir le chemin WildFly
set WILDFLY_HOME=C:\Users\fanir\Documents\utils\wildfly-37.0.1.Final\wildfly-37.0.1.Final

REM Démarrer WildFly (ports par défaut)
echo Demarrage de WildFly sur port 8080...
echo.

cd /d "%WILDFLY_HOME%\bin"
call standalone.bat

pause
