# Quick Test Guide - URLs Essentielles

## Déploiement Rapide
```powershell
# Dans PowerShell, exécutez:
.\DEPLOY.ps1
```

## Tests Rapides dans le Navigateur

### 🔍 Tests de Connectivité
- **WildFly:** http://localhost:8080/
- **Centralisateur:** http://localhost:8080/centralisateur/
- **Test EJB:** http://localhost:8080/centralisateur/api/test/connection
- **Services EJB:** http://localhost:8080/centralisateur/api/test/services

### 👥 Gestion des Clients
- **Lister clients:** http://localhost:8080/centralisateur/api/test/clients
- **Client par CIN:** http://localhost:8080/centralisateur/api/test/clients/cin/123456789012

### 💳 Gestion des Comptes
- **Lister comptes:** http://localhost:8080/centralisateur/api/test/comptes
- **Solde compte:** http://localhost:8080/centralisateur/api/test/comptes/FR7630001007941234567890185/solde
- **Mouvements:** http://localhost:8080/centralisateur/api/test/comptes/FR7630001007941234567890185/mouvements

### 🏦 Types de Comptes
- **Lister types:** http://localhost:8080/centralisateur/api/test/types-comptes

## Tests Complets avec PowerShell
```powershell
# Exécutez tous les tests automatiquement:
.\TESTS_COMPLETE.ps1
```

## Test Postman Collection

### 1. Créer un Client (POST)
**URL:** `http://localhost:8080/centralisateur/api/test/clients`
```json
{
    "nom": "Dupont",
    "prenom": "Jean",
    "cin": "123456789012",
    "adresse": "123 Rue de la Paix",
    "telephone": "0123456789",
    "email": "jean.dupont@email.com"
}
```

### 2. Créer un Compte (POST)
**URL:** `http://localhost:8080/centralisateur/api/test/comptes`
```json
{
    "numeroCompte": "FR7630001007941234567890185",
    "clientId": 1,
    "typeCompteId": 1,
    "soldeInitial": 1000.0
}
```

### 3. Faire un Dépôt (POST)
**URL:** `http://localhost:8080/centralisateur/api/test/operations/depot`
```json
{
    "numeroCompte": "FR7630001007941234567890185",
    "montant": 200.0,
    "description": "Dépôt en espèces"
}
```

### 4. Faire un Retrait (POST)
**URL:** `http://localhost:8080/centralisateur/api/test/operations/retrait`
```json
{
    "numeroCompte": "FR7630001007941234567890185",
    "montant": 100.0,
    "description": "Retrait DAB"
}
```

### 5. Faire un Virement (POST)
**URL:** `http://localhost:8080/centralisateur/api/test/operations/virement`
```json
{
    "compteSource": "FR7630001007941234567890185",
    "compteDestination": "FR7630001007941234567890186",
    "montant": 300.0,
    "description": "Virement vers ami"
}
```

## Résultats Attendus

### Après le Scénario Complet :
- **Compte 1:** 800€ (1000 + 200 - 100 - 300)
- **Compte 2:** 800€ (500 + 300)

### Vérification des Soldes :
- http://localhost:8080/centralisateur/api/test/comptes/FR7630001007941234567890185/solde
- http://localhost:8080/centralisateur/api/test/comptes/FR7630001007941234567890186/solde

## Troubleshooting

### Problèmes Courants :

#### 1. Erreur 404 - Application non trouvée
- Vérifiez que les WAR sont dans `wildfly/standalone/deployments/`
- Vérifiez les logs WildFly pour les erreurs de déploiement

#### 2. Erreur 500 - Erreur serveur
- Vérifiez la connexion PostgreSQL
- Consultez les logs WildFly : `wildfly/standalone/log/server.log`

#### 3. Erreur EJB/JNDI
- Vérifiez que SituationBancaire est déployé en premier
- Vérifiez la configuration `jboss-ejb-client.properties`

#### 4. Erreur Base de Données
- Vérifiez PostgreSQL (port 5432)
- Vérifiez les paramètres dans `persistence.xml`
- Les tables se créent automatiquement avec `hibernate.hbm2ddl.auto=update`

## Architecture Testée

```
[Frontend Thymeleaf] 
        ↓
[Centralisateur - REST API] 
        ↓ (EJB Remote)
[SituationBancaire - EJB Services] 
        ↓ (JPA)
[PostgreSQL Database]
```

## Status Check

✅ **Tout fonctionne** si :
- Tous les endpoints répondent
- Les opérations bancaires fonctionnent
- Les soldes sont corrects
- L'historique est présent

⚠️ **Problèmes partiels** si :
- Certains endpoints échouent
- Les calculs de solde sont incorrects

❌ **Problèmes majeurs** si :
- Pas de connexion EJB
- Erreurs de base de données
- Applications non déployées

---

**Prêt pour le Frontend !** Une fois tous les tests validés, vous pouvez intégrer Thymeleaf pour l'interface utilisateur.