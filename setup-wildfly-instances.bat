@echo off
REM ========================================
REM   CONFIGURATION INITIALE WILDFLY
REM   Création des instances séparées
REM ========================================

echo ========================================
echo   CONFIGURATION WILDFLY
echo   Creation des instances separees
echo ========================================
echo.

REM Vérifier que WILDFLY_HOME est défini
if "%WILDFLY_HOME%"=="" (
    echo ERREUR: Variable WILDFLY_HOME non definie
    echo Exemple: set WILDFLY_HOME=C:\wildfly-37.0.1.Final
    pause
    exit /b 1
)

echo WILDFLY_HOME: %WILDFLY_HOME%
echo.

REM Créer l'instance pour Prêt
echo [1/2] Creation de l'instance WildFly pour Pret...
if not exist "%WILDFLY_HOME%\standalone-pret" (
    xcopy /E /I /Q "%WILDFLY_HOME%\standalone" "%WILDFLY_HOME%\standalone-pret"
    echo Instance standalone-pret creee
    
    REM Nettoyer les déploiements
    del /Q "%WILDFLY_HOME%\standalone-pret\deployments\*.war" 2>nul
    del /Q "%WILDFLY_HOME%\standalone-pret\deployments\*.xml" 2>nul
    echo Deployments nettoyes
) else (
    echo Instance standalone-pret existe deja
)
echo.

REM Créer l'instance pour Centralisateur
echo [2/2] Creation de l'instance WildFly pour Centralisateur...
if not exist "%WILDFLY_HOME%\standalone-centralisateur" (
    xcopy /E /I /Q "%WILDFLY_HOME%\standalone" "%WILDFLY_HOME%\standalone-centralisateur"
    echo Instance standalone-centralisateur creee
    
    REM Nettoyer les déploiements
    del /Q "%WILDFLY_HOME%\standalone-centralisateur\deployments\*.war" 2>nul
    del /Q "%WILDFLY_HOME%\standalone-centralisateur\deployments\*.xml" 2>nul
    echo Deployments nettoyes
) else (
    echo Instance standalone-centralisateur existe deja
)
echo.

echo ========================================
echo   CONFIGURATION TERMINEE
echo ========================================
echo.
echo Instances creees:
echo   - %WILDFLY_HOME%\standalone              (SituationBancaire - port 8080)
echo   - %WILDFLY_HOME%\standalone-pret         (Pret - port 8180)
echo   - %WILDFLY_HOME%\standalone-centralisateur (Centralisateur - port 9080)
echo.
echo Prochaines etapes:
echo   1. Demarrer SituationBancaire: SituationBancaire\start-wildfly-situation.bat
echo   2. Demarrer Pret:              Pret\start-wildfly-pret.bat
echo   3. Demarrer Centralisateur:    Centralisateur\start-wildfly-centralisateur.bat
echo.

pause
