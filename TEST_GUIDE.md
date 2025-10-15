# Guide de Test Complet - Système Bancaire

## Prérequis
1. ✅ WildFly en cours d'exécution (port 8080)
2. ✅ PostgreSQL en cours d'exécution (port 5432)
3. ✅ SituationBancaire.war compilé et prêt pour déploiement
4. ✅ Centralisateur.war compilé et prêt pour déploiement

## Étapes de Test

### 1. Déploiement des Applications

#### Déployer SituationBancaire (Serveur EJB)
```bash
# Copier le WAR vers WildFly
copy target\situation-bancaire.war %WILDFLY_HOME%\standalone\deployments\

# Vérifier le déploiement dans les logs WildFly
```

#### Déployer Centralisateur (Client EJB)
```bash
# Depuis le répertoire Centralisateur
copy target\centralisateur.war %WILDFLY_HOME%\standalone\deployments\

# Vérifier le déploiement
```

### 2. Tests de Communication EJB

#### Test de Base - Connectivité EJB
**URL:** `GET http://localhost:8080/centralisateur/api/test/connection`
**Résultat attendu:** `EJB Connection successful!`

#### Test de Listing des Services
**URL:** `GET http://localhost:8080/centralisateur/api/test/services`
**Résultat attendu:** Liste des services EJB disponibles

### 3. Tests de Gestion des Clients

#### 3.1 Créer un Client
**URL:** `POST http://localhost:8080/centralisateur/api/test/clients`
**Body (JSON):**
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
**Résultat attendu:** Client créé avec ID généré

#### 3.2 Lister tous les Clients
**URL:** `GET http://localhost:8080/centralisateur/api/test/clients`
**Résultat attendu:** Liste de tous les clients

#### 3.3 Récupérer un Client par ID
**URL:** `GET http://localhost:8080/centralisateur/api/test/clients/{id}`
**Résultat attendu:** Détails du client spécifique

#### 3.4 Récupérer un Client par CIN
**URL:** `GET http://localhost:8080/centralisateur/api/test/clients/cin/{cin}`
**Exemple:** `GET http://localhost:8080/centralisateur/api/test/clients/cin/123456789012`
**Résultat attendu:** Client correspondant au CIN

#### 3.5 Modifier un Client
**URL:** `PUT http://localhost:8080/centralisateur/api/test/clients/{id}`
**Body (JSON):** Données modifiées du client
**Résultat attendu:** Client mis à jour

#### 3.6 Supprimer un Client
**URL:** `DELETE http://localhost:8080/centralisateur/api/test/clients/{id}`
**Résultat attendu:** Confirmation de suppression

### 4. Tests de Gestion des Comptes

#### 4.1 Créer un Compte
**URL:** `POST http://localhost:8080/centralisateur/api/test/comptes`
**Body (JSON):**
```json
{
    "numeroCompte": "FR7630001007941234567890185",
    "clientId": 1,
    "typeCompteId": 1,
    "soldeInitial": 1000.0
}
```
**Résultat attendu:** Compte créé avec succès

#### 4.2 Lister tous les Comptes
**URL:** `GET http://localhost:8080/centralisateur/api/test/comptes`
**Résultat attendu:** Liste de tous les comptes

#### 4.3 Récupérer un Compte par Numéro
**URL:** `GET http://localhost:8080/centralisateur/api/test/comptes/{numeroCompte}`
**Résultat attendu:** Détails du compte spécifique

#### 4.4 Consulter le Solde
**URL:** `GET http://localhost:8080/centralisateur/api/test/comptes/{numeroCompte}/solde`
**Résultat attendu:** Solde actuel du compte

#### 4.5 Lister les Comptes d'un Client
**URL:** `GET http://localhost:8080/centralisateur/api/test/clients/{clientId}/comptes`
**Résultat attendu:** Tous les comptes du client

#### 4.6 Bloquer/Débloquer un Compte
**URL:** `PUT http://localhost:8080/centralisateur/api/test/comptes/{numeroCompte}/block`
**URL:** `PUT http://localhost:8080/centralisateur/api/test/comptes/{numeroCompte}/unblock`
**Résultat attendu:** État du compte modifié

### 5. Tests des Opérations Bancaires

#### 5.1 Effectuer un Dépôt
**URL:** `POST http://localhost:8080/centralisateur/api/test/operations/depot`
**Body (JSON):**
```json
{
    "numeroCompte": "FR7630001007941234567890185",
    "montant": 500.0,
    "description": "Dépôt en espèces"
}
```
**Résultat attendu:** Dépôt effectué, solde mis à jour

#### 5.2 Effectuer un Retrait
**URL:** `POST http://localhost:8080/centralisateur/api/test/operations/retrait`
**Body (JSON):**
```json
{
    "numeroCompte": "FR7630001007941234567890185",
    "montant": 200.0,
    "description": "Retrait DAB"
}
```
**Résultat attendu:** Retrait effectué, solde mis à jour

#### 5.3 Effectuer un Virement
**URL:** `POST http://localhost:8080/centralisateur/api/test/operations/virement`
**Body (JSON):**
```json
{
    "compteSource": "FR7630001007941234567890185",
    "compteDestination": "FR7630001007941234567890186",
    "montant": 300.0,
    "description": "Virement vers ami"
}
```
**Résultat attendu:** Virement effectué, soldes mis à jour

#### 5.4 Consulter l'Historique des Opérations
**URL:** `GET http://localhost:8080/centralisateur/api/test/comptes/{numeroCompte}/mouvements`
**Résultat attendu:** Liste des mouvements du compte

#### 5.5 Consulter l'Historique des Virements
**URL:** `GET http://localhost:8080/centralisateur/api/test/comptes/{numeroCompte}/virements`
**Résultat attendu:** Liste des virements du compte

### 6. Tests de Gestion des Types de Comptes

#### 6.1 Lister les Types de Comptes
**URL:** `GET http://localhost:8080/centralisateur/api/test/types-comptes`
**Résultat attendu:** Liste des types disponibles

#### 6.2 Créer un Type de Compte
**URL:** `POST http://localhost:8080/centralisateur/api/test/types-comptes`
**Body (JSON):**
```json
{
    "nom": "Compte Épargne",
    "description": "Compte d'épargne avec intérêts"
}
```

### 7. Scénario de Test Complet

#### Étape 1: Créer des Clients
1. Créer Client A (Jean Dupont)
2. Créer Client B (Marie Martin)

#### Étape 2: Créer des Comptes
1. Créer Compte 1 pour Client A (solde: 1000€)
2. Créer Compte 2 pour Client B (solde: 500€)

#### Étape 3: Opérations Bancaires
1. Dépôt 200€ sur Compte 1
2. Retrait 100€ du Compte 1
3. Virement 300€ du Compte 1 vers Compte 2

#### Étape 4: Vérifications
1. Vérifier solde Compte 1 = 800€
2. Vérifier solde Compte 2 = 800€
3. Vérifier historiques des deux comptes

## Outils de Test Recommandés

### Postman
- Importer les requêtes depuis ce guide
- Configurer l'environnement avec l'URL de base: `http://localhost:8080/centralisateur`

### Curl (PowerShell)
```powershell
# Test de connexion
Invoke-RestMethod -Uri "http://localhost:8080/centralisateur/api/test/connection" -Method GET

# Créer un client
$client = @{
    nom = "Dupont"
    prenom = "Jean"
    cin = "123456789012"
    adresse = "123 Rue de la Paix"
    telephone = "0123456789"
    email = "jean.dupont@email.com"
}
Invoke-RestMethod -Uri "http://localhost:8080/centralisateur/api/test/clients" -Method POST -Body ($client | ConvertTo-Json) -ContentType "application/json"
```

### Navigateur Web
- Tests GET simples via navigateur
- URL de base: `http://localhost:8080/centralisateur/api/test/`

## Points de Contrôle

### Base de Données
- Vérifier que les tables sont créées automatiquement
- Contrôler l'intégrité des données après chaque opération

### Logs WildFly
- Surveiller les logs pour les erreurs EJB
- Vérifier les connexions JNDI

### Performance
- Temps de réponse des appels EJB
- Gestion des transactions

## Résultats Attendus

✅ **Succès complet** : Toutes les fonctionnalités marchent
⚠️ **Succès partiel** : Quelques fonctionnalités en erreur
❌ **Échec** : Problèmes de communication EJB ou base de données

---

**Remarque**: Ce guide teste l'intégration complète entre le Centralisateur (client EJB) et SituationBancaire (serveur EJB) sans utilisation de DTOs, en respectant l'architecture demandée.