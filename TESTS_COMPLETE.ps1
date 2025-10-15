# Script de Test PowerShell - Système Bancaire
# Exécuter ce script après avoir déployé les applications sur WildFly

Write-Host "=== SCRIPT DE TEST SYSTÈME BANCAIRE ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080"
$centralisateurUrl = "$baseUrl/centralisateur/api/test"

# Fonction d'aide pour les requêtes
function Test-Endpoint {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Body = $null,
        [string]$Description
    )
    
    Write-Host "Testing: $Description" -ForegroundColor Yellow
    Write-Host "URL: $Url" -ForegroundColor Cyan
    
    try {
        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json
            Write-Host "Body: $jsonBody" -ForegroundColor Magenta
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Body $jsonBody -ContentType "application/json"
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method
        }
        
        Write-Host "✅ SUCCESS: $Description" -ForegroundColor Green
        Write-Host "Response: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
        return $response
    } catch {
        Write-Host "❌ ERROR: $Description" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
    Write-Host "---" -ForegroundColor DarkGray
}

Write-Host "ÉTAPE 1: Tests de connectivité" -ForegroundColor Magenta
Write-Host "================================" -ForegroundColor Magenta

# Test 1: Connectivité WildFly
Test-Endpoint -Url "$baseUrl/" -Description "WildFly Server"

# Test 2: Connectivité Centralisateur
Test-Endpoint -Url "$centralisateurUrl/connection" -Description "EJB Connection Test"

# Test 3: Services disponibles
Test-Endpoint -Url "$centralisateurUrl/services" -Description "Available EJB Services"

Write-Host ""
Write-Host "ÉTAPE 2: Tests de gestion des clients" -ForegroundColor Magenta
Write-Host "======================================" -ForegroundColor Magenta

# Test 4: Créer un client
$client1 = @{
    nom = "Dupont"
    prenom = "Jean"
    cin = "123456789012"
    adresse = "123 Rue de la Paix"
    telephone = "0123456789"
    email = "jean.dupont@email.com"
}
$createdClient1 = Test-Endpoint -Url "$centralisateurUrl/clients" -Method "POST" -Body $client1 -Description "Create Client 1 (Jean Dupont)"

# Test 5: Créer un second client
$client2 = @{
    nom = "Martin"
    prenom = "Marie"
    cin = "210987654321"
    adresse = "456 Avenue des Fleurs"
    telephone = "0987654321"
    email = "marie.martin@email.com"
}
$createdClient2 = Test-Endpoint -Url "$centralisateurUrl/clients" -Method "POST" -Body $client2 -Description "Create Client 2 (Marie Martin)"

# Test 6: Lister tous les clients
Test-Endpoint -Url "$centralisateurUrl/clients" -Description "List All Clients"

# Test 7: Récupérer client par CIN
if ($createdClient1) {
    Test-Endpoint -Url "$centralisateurUrl/clients/cin/123456789012" -Description "Get Client by CIN"
}

Write-Host ""
Write-Host "ÉTAPE 3: Tests de gestion des types de comptes" -ForegroundColor Magenta
Write-Host "===============================================" -ForegroundColor Magenta

# Test 8: Lister les types de comptes
Test-Endpoint -Url "$centralisateurUrl/types-comptes" -Description "List Account Types"

# Test 9: Créer un type de compte
$typeCompte = @{
    nom = "Compte Courant Standard"
    description = "Compte courant avec frais standard"
}
Test-Endpoint -Url "$centralisateurUrl/types-comptes" -Method "POST" -Body $typeCompte -Description "Create Account Type"

Write-Host ""
Write-Host "ÉTAPE 4: Tests de gestion des comptes" -ForegroundColor Magenta
Write-Host "=====================================" -ForegroundColor Magenta

# Test 10: Créer un compte pour le premier client
if ($createdClient1) {
    $compte1 = @{
        numeroCompte = "FR7630001007941234567890185"
        clientId = 1  # Supposé être l'ID du premier client
        typeCompteId = 1  # Supposé être l'ID du type créé
        soldeInitial = 1000.0
    }
    Test-Endpoint -Url "$centralisateurUrl/comptes" -Method "POST" -Body $compte1 -Description "Create Account 1"
}

# Test 11: Créer un compte pour le second client
if ($createdClient2) {
    $compte2 = @{
        numeroCompte = "FR7630001007941234567890186"
        clientId = 2  # Supposé être l'ID du second client
        typeCompteId = 1
        soldeInitial = 500.0
    }
    Test-Endpoint -Url "$centralisateurUrl/comptes" -Method "POST" -Body $compte2 -Description "Create Account 2"
}

# Test 12: Lister tous les comptes
Test-Endpoint -Url "$centralisateurUrl/comptes" -Description "List All Accounts"

# Test 13: Consulter le solde d'un compte
Test-Endpoint -Url "$centralisateurUrl/comptes/FR7630001007941234567890185/solde" -Description "Check Account Balance"

Write-Host ""
Write-Host "ÉTAPE 5: Tests des opérations bancaires" -ForegroundColor Magenta
Write-Host "=======================================" -ForegroundColor Magenta

# Test 14: Effectuer un dépôt
$depot = @{
    numeroCompte = "FR7630001007941234567890185"
    montant = 200.0
    description = "Dépôt en espèces"
}
Test-Endpoint -Url "$centralisateurUrl/operations/depot" -Method "POST" -Body $depot -Description "Make Deposit"

# Test 15: Effectuer un retrait
$retrait = @{
    numeroCompte = "FR7630001007941234567890185"
    montant = 100.0
    description = "Retrait DAB"
}
Test-Endpoint -Url "$centralisateurUrl/operations/retrait" -Method "POST" -Body $retrait -Description "Make Withdrawal"

# Test 16: Effectuer un virement
$virement = @{
    compteSource = "FR7630001007941234567890185"
    compteDestination = "FR7630001007941234567890186"
    montant = 300.0
    description = "Virement vers ami"
}
Test-Endpoint -Url "$centralisateurUrl/operations/virement" -Method "POST" -Body $virement -Description "Make Transfer"

# Test 17: Consulter l'historique des mouvements
Test-Endpoint -Url "$centralisateurUrl/comptes/FR7630001007941234567890185/mouvements" -Description "Get Transaction History"

# Test 18: Consulter les soldes finaux
Test-Endpoint -Url "$centralisateurUrl/comptes/FR7630001007941234567890185/solde" -Description "Final Balance Account 1"
Test-Endpoint -Url "$centralisateurUrl/comptes/FR7630001007941234567890186/solde" -Description "Final Balance Account 2"

Write-Host ""
Write-Host "=== RÉSUMÉ DES TESTS ===" -ForegroundColor Green
Write-Host ""
Write-Host "Solde attendu Compte 1: 800€ (1000 + 200 - 100 - 300)" -ForegroundColor Yellow
Write-Host "Solde attendu Compte 2: 800€ (500 + 300)" -ForegroundColor Yellow
Write-Host ""
Write-Host "Si tous les tests sont verts (✅), votre système fonctionne parfaitement !" -ForegroundColor Green
Write-Host "En cas d'erreur (❌), vérifiez les logs WildFly et la base de données." -ForegroundColor Red

# Instructions finales
Write-Host ""
Write-Host "=== INSTRUCTIONS POUR EXÉCUTER LES TESTS ===" -ForegroundColor Cyan
Write-Host "1. Assurez-vous que WildFly est démarré" -ForegroundColor White
Write-Host "2. Déployez SituationBancaire: copy SituationBancaire\target\situation-bancaire.war [WILDFLY_HOME]\standalone\deployments\" -ForegroundColor White  
Write-Host "3. Déployez Centralisateur: copy Centralisateur\target\centralisateur.war [WILDFLY_HOME]\standalone\deployments\" -ForegroundColor White
Write-Host "4. Attendez que les déploiements soient complets (vérifiez les logs)" -ForegroundColor White
Write-Host "5. Exécutez ce script: .\TESTS_COMPLETE.ps1" -ForegroundColor White