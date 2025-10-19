@echo off
echo ========================================
echo   DEPLOIEMENT MODULE PRET
echo ========================================

REM Compilation
echo [1/5] Compilation Maven...
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Compilation echouee
    exit /b 1
)

REM Copie du WAR
echo [2/5] Copie du WAR vers WildFly...
copy /Y target\pret.war %WILDFLY_HOME%\standalone\deployments\
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Copie du WAR echouee
    exit /b 1
)

REM Copie de la DataSource (si pas deja fait)
echo [3/5] Verification de la DataSource...
if not exist "%WILDFLY_HOME%\standalone\deployments\pret-ds.xml" (
    copy /Y src\main\webapp\WEB-INF\pret-ds.xml %WILDFLY_HOME%\standalone\deployments\
    echo DataSource copiee
) else (
    echo DataSource deja presente
)

REM Attente du deploiement
echo [4/5] Attente du deploiement...
timeout /t 10 /nobreak

REM Verification
echo [5/5] Verification du deploiement...
if exist "%WILDFLY_HOME%\standalone\deployments\pret.war.deployed" (
    echo ========================================
    echo   DEPLOIEMENT REUSSI !
    echo ========================================
    echo Module accessible sur: http://localhost:8180/pret
    echo EJBs disponibles sur: http-remoting://localhost:8180
) else if exist "%WILDFLY_HOME%\standalone\deployments\pret.war.failed" (
    echo ========================================
    echo   DEPLOIEMENT ECHOUE !
    echo ========================================
    echo Consulter: %WILDFLY_HOME%\standalone\deployments\pret.war.failed
) else (
    echo ========================================
    echo   DEPLOIEMENT EN COURS...
    echo ========================================
    echo Verifier les logs: %WILDFLY_HOME%\standalone\log\server.log
)

pause
