@echo off
REM ========================================
REM   DEPLOIEMENT AUTOMATIQUE - TOUS MODULES
REM   SituationBancaire + Pret + Centralisateur
REM ========================================

echo ========================================
echo   DEPLOIEMENT DES MODULES VERS WILDFLY
echo ========================================
echo.

REM Définir le chemin WildFly
set WILDFLY_HOME=C:\Users\fanir\Documents\utils\wildfly-37.0.1.Final\wildfly-37.0.1.Final

echo Chemin WildFly: %WILDFLY_HOME%
echo.

REM Vérifier que WildFly existe
if not exist "%WILDFLY_HOME%\bin\standalone.bat" (
    echo ERREUR: WildFly introuvable a %WILDFLY_HOME%
    pause
    exit /b 1
)

echo ========================================
echo   1. DEPLOIEMENT SITUATION BANCAIRE
echo ========================================

REM Vérifier que le WAR existe
if not exist "SituationBancaire\target\situation-bancaire.war" (
    echo ATTENTION: situation-bancaire.war introuvable
    echo Compilation en cours...
    cd SituationBancaire
    call mvn clean package -DskipTests
    cd ..
)

REM Copier le WAR
echo Copie de situation-bancaire.war...
copy /Y "SituationBancaire\target\situation-bancaire.war" "%WILDFLY_HOME%\standalone\deployments\"
if %errorlevel% neq 0 (
    echo ERREUR: Echec de copie de situation-bancaire.war
    pause
    exit /b 1
)
echo   [OK] situation-bancaire.war deploye sur port 8080
echo.

echo ========================================
echo   2. DEPLOIEMENT MODULE PRET
echo ========================================

REM Vérifier que le WAR existe
if not exist "Pret\target\pret.war" (
    echo ATTENTION: pret.war introuvable
    echo Compilation en cours...
    cd Pret
    call mvn clean package -DskipTests
    cd ..
)

REM Copier le WAR
echo Copie de pret.war...
copy /Y "Pret\target\pret.war" "%WILDFLY_HOME%\standalone-pret\deployments\"
if %errorlevel% neq 0 (
    echo ERREUR: Echec de copie de pret.war
    pause
    exit /b 1
)

REM Copier le DataSource
echo Copie de pret-ds.xml...
copy /Y "Pret\src\main\webapp\WEB-INF\pret-ds.xml" "%WILDFLY_HOME%\standalone-pret\deployments\"
if %errorlevel% neq 0 (
    echo ERREUR: Echec de copie de pret-ds.xml
    pause
    exit /b 1
)
echo   [OK] pret.war deploye sur port 8180
echo.

echo ========================================
echo   3. DEPLOIEMENT CENTRALISATEUR
echo ========================================

REM Vérifier que le WAR existe
if not exist "Centralisateur\target\centralisateur.war" (
    echo ATTENTION: centralisateur.war introuvable
    echo Compilation en cours...
    cd Centralisateur
    call mvn clean package -DskipTests
    cd ..
)

REM Copier le WAR
echo Copie de centralisateur.war...
copy /Y "Centralisateur\target\centralisateur.war" "%WILDFLY_HOME%\standalone-centralisateur\deployments\"
if %errorlevel% neq 0 (
    echo ERREUR: Echec de copie de centralisateur.war
    pause
    exit /b 1
)
echo   [OK] centralisateur.war deploye sur port 9080
echo.

echo ========================================
echo   DEPLOIEMENT TERMINE AVEC SUCCES
echo ========================================
echo.
echo Modules deployes:
echo   - SituationBancaire : http://localhost:8080
echo   - Pret              : http://localhost:8180
echo   - Centralisateur    : http://localhost:9080/centralisateur
echo.
echo Pour demarrer les instances WildFly:
echo   1. Terminal 1: cd SituationBancaire ^&^& start-wildfly-situation.bat
echo   2. Terminal 2: cd Pret ^&^& start-wildfly-pret.bat
echo   3. Terminal 3: cd Centralisateur ^&^& start-wildfly-centralisateur.bat
echo.
pause
