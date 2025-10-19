@echo off
echo ========================================
echo  RECREATION DE LA BASE DE DONNEES
echo  avec la nouvelle migration
echo ========================================
echo.

echo [1/3] Suppression de l'ancienne base de données...
echo.
echo ATTENTION: Vous devrez entrer le mot de passe PostgreSQL (postgres)
echo.

:: Fermer toutes les connexions actives
psql -U postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'compte_epargne_db' AND pid <> pg_backend_pid();"

:: Supprimer la base
psql -U postgres -c "DROP DATABASE IF EXISTS compte_epargne_db;"

:: Créer la nouvelle base
echo.
echo [2/3] Création de la nouvelle base de données...
psql -U postgres -c "CREATE DATABASE compte_epargne_db;"

echo.
echo [3/3] Application de la migration avec les ajustements...
echo.

:: Appliquer la migration
dotnet ef database update

echo.
echo ========================================
echo  TERMINÉ !
echo ========================================
echo.
echo La base de données a été recréée avec :
echo   - Client SANS charges_mensuelles
echo   - 6 contraintes CHECK sur les enums
echo.
echo Pour vérifier, exécutez :
echo   psql -U postgres -d compte_epargne_db -c "\d client"
echo.

pause
