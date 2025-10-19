@echo off
REM Script de démarrage du module Épargne

echo ========================================
echo   Module Épargne - Démarrage
echo ========================================
echo.

REM Se positionner dans le répertoire du projet
cd /d "%~dp0"

echo [1/4] Restauration des packages NuGet...
dotnet restore
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: La restauration des packages a échoué
    pause
    exit /b 1
)

echo.
echo [2/4] Compilation du projet...
dotnet build
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: La compilation a échoué
    pause
    exit /b 1
)

echo.
echo [3/4] Application des migrations de base de données...
dotnet ef database update
if %ERRORLEVEL% NEQ 0 (
    echo ATTENTION: Les migrations n'ont pas pu être appliquées
    echo Assurez-vous que PostgreSQL est démarré et que la base de données existe
    echo.
)

echo.
echo [4/4] Démarrage de l'API Épargne...
echo L'API sera disponible sur: http://localhost:5000
echo Documentation Swagger: http://localhost:5000
echo.
echo Appuyez sur Ctrl+C pour arrêter le serveur
echo.

dotnet run

pause
