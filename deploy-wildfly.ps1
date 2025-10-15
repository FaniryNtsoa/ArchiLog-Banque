# Script PowerShell de déploiement pour WildFly
# Ce script déploie les modules SituationBancaire et Centralisateur

Write-Host "=== Déploiement des modules bancaires sur WildFly ===" -ForegroundColor Green

# Variables de configuration
$WILDFLY_HOME = if ($env:WILDFLY_HOME) { $env:WILDFLY_HOME } else { "C:\wildfly" }
$WILDFLY_CLI = Join-Path $WILDFLY_HOME "bin\jboss-cli.bat"

# Chemins des WAR files
$SITUATION_BANCAIRE_WAR = "c:\Users\fanir\Documents\ITU\Faniry\S5\ArchitectureLogiciel\banque\SituationBancaire\target\situation-bancaire.war"
$CENTRALISATEUR_WAR = "c:\Users\fanir\Documents\ITU\Faniry\S5\ArchitectureLogiciel\banque\Centralisateur\target\centralisateur.war"

Write-Host "1. Vérification de l'existence des fichiers WAR..." -ForegroundColor Yellow

if (-not (Test-Path $SITUATION_BANCAIRE_WAR)) {
    Write-Host "ERREUR: Le fichier $SITUATION_BANCAIRE_WAR n'existe pas" -ForegroundColor Red
    Write-Host "Exécutez 'mvn package' dans le répertoire SituationBancaire" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $CENTRALISATEUR_WAR)) {
    Write-Host "ERREUR: Le fichier $CENTRALISATEUR_WAR n'existe pas" -ForegroundColor Red
    Write-Host "Exécutez 'mvn package' dans le répertoire Centralisateur" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Fichiers WAR trouvés" -ForegroundColor Green

Write-Host "2. Vérification du serveur WildFly..." -ForegroundColor Yellow

# Vérifier si WildFly est déjà démarré
$wildflyProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like "*WildFly*" -or $_.CommandLine -like "*jboss-modules.jar*" }

if (-not $wildflyProcess) {
    Write-Host "WildFly n'est pas démarré. Veuillez démarrer WildFly manuellement avant d'exécuter ce script." -ForegroundColor Red
    Write-Host "Commande de démarrage: $WILDFLY_HOME\bin\standalone.bat" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "✓ WildFly est en cours d'exécution" -ForegroundColor Green
}

Write-Host "3. Déploiement du module SituationBancaire..." -ForegroundColor Yellow
& $WILDFLY_CLI --connect --command="deploy `"$SITUATION_BANCAIRE_WAR`" --force"

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERREUR: Échec du déploiement de SituationBancaire" -ForegroundColor Red
    exit 1
}

Write-Host "4. Attente de 10 secondes pour la stabilisation..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "5. Déploiement du module Centralisateur..." -ForegroundColor Yellow
& $WILDFLY_CLI --connect --command="deploy `"$CENTRALISATEUR_WAR`" --force"

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERREUR: Échec du déploiement du Centralisateur" -ForegroundColor Red
    exit 1
}

Write-Host "6. Vérification du statut des déploiements..." -ForegroundColor Yellow
& $WILDFLY_CLI --connect --command="deployment-info"

Write-Host ""
Write-Host "=== Déploiement terminé avec succès ===" -ForegroundColor Green
Write-Host "SituationBancaire disponible sur: http://localhost:8080/situation-bancaire/" -ForegroundColor Cyan
Write-Host "Centralisateur disponible sur: http://localhost:8080/centralisateur/" -ForegroundColor Cyan
Write-Host ""
Write-Host "URLs de test:" -ForegroundColor Yellow
Write-Host "- Test ping Centralisateur: http://localhost:8080/centralisateur/api/test/ping" -ForegroundColor Cyan
Write-Host "- Test connexion EJB: http://localhost:8080/centralisateur/api/test/ejb-connection" -ForegroundColor Cyan
Write-Host "- Liste des clients: http://localhost:8080/centralisateur/api/test/clients" -ForegroundColor Cyan