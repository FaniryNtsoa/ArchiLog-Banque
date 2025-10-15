# Script de test rapide pour vérifier la communication EJB
# Exécute une série de tests HTTP pour valider le déploiement

Write-Host "=== Tests de validation du déploiement ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080/centralisateur"
$timeout = 30

function Test-Endpoint {
    param(
        [string]$url,
        [string]$description
    )
    
    Write-Host "Testing: $description" -ForegroundColor Yellow
    Write-Host "URL: $url" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method GET -TimeoutSec $timeout
        Write-Host "✓ SUCCESS" -ForegroundColor Green
        Write-Host "Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Cyan
        Write-Host ""
        return $true
    }
    catch {
        Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
        return $false
    }
}

# Attendre que WildFly soit prêt
Write-Host "Waiting for WildFly to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

$testResults = @()

# Test 1: Ping du Centralisateur
$testResults += Test-Endpoint "$baseUrl/api/test/ping" "Centralisateur Ping"

# Test 2: Connexion EJB
$testResults += Test-Endpoint "$baseUrl/api/test/ejb-connection" "EJB Connection Test"

# Test 3: Liste des clients
$testResults += Test-Endpoint "$baseUrl/api/test/clients" "Client List"

# Test 4: Client par ID
$testResults += Test-Endpoint "$baseUrl/api/test/clients/1" "Client by ID"

# Test 5: Client par numéro
$testResults += Test-Endpoint "$baseUrl/api/test/clients/numero/CLI001" "Client by Number"

# Résumé
Write-Host "=== Résumé des tests ===" -ForegroundColor Green
$successCount = ($testResults | Where-Object { $_ -eq $true }).Count
$totalCount = $testResults.Count

Write-Host "Tests réussis: $successCount/$totalCount" -ForegroundColor $(if ($successCount -eq $totalCount) { "Green" } else { "Yellow" })

if ($successCount -eq $totalCount) {
    Write-Host "🎉 Tous les tests sont RÉUSSIS ! La communication EJB fonctionne correctement." -ForegroundColor Green
} else {
    Write-Host "⚠️  Certains tests ont échoué. Vérifiez les logs de WildFly et la configuration." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Pour tester manuellement, ouvrez ces URLs dans votre navigateur:" -ForegroundColor Cyan
Write-Host "- $baseUrl/api/test/ping" -ForegroundColor Gray
Write-Host "- $baseUrl/api/test/ejb-connection" -ForegroundColor Gray
Write-Host "- $baseUrl/api/test/clients" -ForegroundColor Gray