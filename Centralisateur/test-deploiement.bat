@echo off
REM Script de test rapide pour vérifier le déploiement du CSS

echo.
echo ========================================
echo    TEST DE DEPLOIEMENT - CENTRALISATEUR
echo ========================================
echo.

REM Couleurs
set GREEN=[92m
set RED=[91m
set YELLOW=[93m
set NC=[0m

echo [ETAPE 1] Verification du WAR...
if exist "target\centralisateur.war" (
    echo %GREEN%OK - Le WAR existe%NC%
) else (
    echo %RED%ERREUR - Le WAR n'existe pas. Executez: mvn clean package%NC%
    exit /b 1
)

echo.
echo [ETAPE 2] Verification du contenu du WAR...
jar tf target\centralisateur.war | findstr /i "css/style.css" >nul
if %errorlevel% == 0 (
    echo %GREEN%OK - Le CSS est present dans le WAR%NC%
) else (
    echo %RED%ERREUR - Le CSS n'est pas dans le WAR%NC%
    exit /b 1
)

jar tf target\centralisateur.war | findstr /i "StaticResourceServlet.class" >nul
if %errorlevel% == 0 (
    echo %GREEN%OK - La servlet StaticResourceServlet est presente%NC%
) else (
    echo %RED%ERREUR - La servlet StaticResourceServlet n'est pas compilee%NC%
    exit /b 1
)

echo.
echo [ETAPE 3] Tests d'URL (si WildFly est demarre)...
echo.
echo Testez ces URLs dans votre navigateur :
echo.
echo %YELLOW%1. Test direct du CSS :%NC%
echo    http://localhost:8080/centralisateur/css/style.css
echo.
echo %YELLOW%2. Page de login :%NC%
echo    http://localhost:8080/centralisateur/login
echo.
echo %YELLOW%3. Page d'inscription :%NC%
echo    http://localhost:8080/centralisateur/register
echo.

echo [ETAPE 4] Test avec curl (si disponible)...
where curl >nul 2>&1
if %errorlevel% == 0 (
    echo.
    echo Test du CSS avec curl...
    curl -s -o nul -w "Status HTTP: %%{http_code}\n" http://localhost:8080/centralisateur/css/style.css
    echo.
) else (
    echo %YELLOW%curl n'est pas disponible - ignorez cette etape%NC%
)

echo.
echo ========================================
echo    INSTRUCTIONS DE DEPLOIEMENT
echo ========================================
echo.
echo 1. Si WildFly n'est pas demarre :
echo    cd %%WILDFLY_HOME%%\bin
echo    standalone.bat
echo.
echo 2. Copier le WAR dans WildFly :
echo    copy target\centralisateur.war %%WILDFLY_HOME%%\standalone\deployments\
echo.
echo 3. Attendre quelques secondes (WildFly auto-deploie)
echo.
echo 4. Verifier qu'un fichier .deployed apparait :
echo    dir %%WILDFLY_HOME%%\standalone\deployments\centralisateur.war.deployed
echo.
echo 5. Tester les URLs ci-dessus dans votre navigateur
echo.
echo ========================================
echo    VERIFICATION FINALE
echo ========================================
echo.
echo Dans le navigateur (F12 - DevTools) :
echo - Onglet Network
echo - Actualiser la page (Ctrl+R)
echo - Chercher "style.css"
echo - Verifier : Status 200, Type: text/css
echo.
echo ========================================

pause
