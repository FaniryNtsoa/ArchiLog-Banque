@echo off
echo ========================================
echo Deploiement du Systeme d'Administration
echo ========================================
echo.

REM Variables de configuration
set WILDFLY_HOME=C:\wildfly
set DEPLOYMENTS=%WILDFLY_HOME%\standalone\deployments

echo [1/4] Compilation de SituationBancaire...
cd SituationBancaire
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Compilation de SituationBancaire echouee
    pause
    exit /b 1
)

echo.
echo [2/4] Compilation du Centralisateur...
cd ..\Centralisateur
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Compilation du Centralisateur echouee
    pause
    exit /b 1
)

echo.
echo [3/4] Deploiement de SituationBancaire...
copy /Y "..\SituationBancaire\target\situation-bancaire.war" "%DEPLOYMENTS%\"
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible de copier situation-bancaire.war
    pause
    exit /b 1
)

echo.
echo [4/4] Deploiement du Centralisateur...
copy /Y "target\centralisateur.war" "%DEPLOYMENTS%\"
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Impossible de copier centralisateur.war
    pause
    exit /b 1
)

echo.
echo ========================================
echo Deploiement termine avec succes!
echo ========================================
echo.
echo Verifiez les logs WildFly pour confirmation
echo URL Admin: http://localhost:8080/centralisateur/admin/login
echo.
echo Comptes de test:
echo   - admin / admin123 (Tous les droits)
echo   - manager / manager123 (Lecture + Ecriture)
echo   - operateur / oper123 (Lecture + Insertion)
echo   - lecteur / lecteur123 (Lecture seule)
echo.
pause
