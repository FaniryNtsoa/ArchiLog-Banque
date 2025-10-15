# API REST Complète - Centralisateur Bancaire

Ce document décrit toutes les fonctionnalités disponibles dans le Centralisateur via l'API REST.

## 🏗️ Architecture

```
┌─────────────────┐    EJB Remote    ┌─────────────────────┐
│  Centralisateur │ ────────────────► │ SituationBancaire  │
│                 │                  │                     │
│ - REST API      │                  │ - ClientService     │
│ - Client EJB    │                  │ - CompteService     │
│ - Services      │                  │ - OperationService  │
└─────────────────┘                  └─────────────────────┘
```

## 📋 Fonctionnalités Implémentées

### ✅ **Gestion des Clients**
- Création de clients
- Recherche par ID, numéro, email
- Modification et suppression
- Validation des données

### ✅ **Gestion des Comptes**
- Création de comptes courants
- Consultation des soldes
- Blocage/déblocage de comptes
- Gestion des paramètres de compte

### ✅ **Opérations Bancaires**
- Dépôts et retraits
- Virements entre comptes
- Historique des mouvements
- Application des frais et intérêts

## 🚀 Endpoints REST

### 📊 **Tests et Monitoring**

#### Ping du système
```http
GET /api/test/ping
```
**Réponse :**
```json
{
  "status": "OK",
  "message": "Centralisateur is running",
  "timestamp": 1697123456789,
  "version": "2.0.0"
}
```

#### Test de connexion EJB
```http
GET /api/test/ejb-connection
```
**Réponse :**
```json
{
  "ejbConnectionStatus": "SUCCESS",
  "message": "Connexion EJB avec SituationBancaire établie"
}
```

#### Test de scénario complet
```http
POST /api/test/scenario-complet
```
**Description :** Crée un client, un compte, effectue un dépôt et consulte le solde.

---

### 👥 **Gestion des Clients**

#### Créer un client
```http
POST /api/clients
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@email.com",
  "telephone": "0123456789",
  "numCin": "123456789",
  "dateNaissance": "1985-05-15",
  "adresse": "123 Rue de la Paix",
  "profession": "Ingénieur",
  "revenuMensuel": 3500.00
}
```

#### Récupérer tous les clients
```http
GET /api/clients
```

#### Récupérer un client par ID
```http
GET /api/clients/{id}
```

#### Récupérer un client par numéro
```http
GET /api/clients/numero/{numeroClient}
```

#### Récupérer un client par email
```http
GET /api/clients/email/{email}
```

#### Modifier un client
```http
PUT /api/clients/{id}
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "nouveau.email@example.com",
  "telephone": "0987654321"
}
```

#### Supprimer un client
```http
DELETE /api/clients/{id}
```

---

### 💳 **Gestion des Comptes**

#### Créer un compte pour un client
```http
POST /api/clients/{clientId}/comptes
Content-Type: application/json

{
  "numeroCompte": "CPT001234",
  "soldeActuel": 1000.00,
  "decouvertAutorise": -500.00
}
```

#### Récupérer les comptes d'un client
```http
GET /api/clients/{clientId}/comptes
```

#### Récupérer un compte par numéro
```http
GET /api/comptes/{numeroCompte}
```

#### Consulter le solde d'un compte
```http
GET /api/comptes/{numeroCompte}/solde
```
**Réponse :**
```json
{
  "numeroCompte": "CPT001234",
  "soldeActuel": 1250.75,
  "timestamp": 1697123456789
}
```

#### Bloquer un compte
```http
PUT /api/comptes/{numeroCompte}/bloquer
```

#### Débloquer un compte
```http
PUT /api/comptes/{numeroCompte}/debloquer
```

---

### 💰 **Opérations Bancaires**

#### Effectuer un dépôt
```http
POST /api/comptes/{numeroCompte}/depot
Content-Type: application/json

{
  "montant": 250.00,
  "libelle": "Dépôt espèces"
}
```

#### Effectuer un retrait
```http
POST /api/comptes/{numeroCompte}/retrait
Content-Type: application/json

{
  "montant": 100.00,
  "libelle": "Retrait DAB"
}
```

#### Effectuer un virement
```http
POST /api/virements
Content-Type: application/json

{
  "compteDebiteur": "CPT001234",
  "compteCrediteur": "CPT005678",
  "montant": 500.00,
  "libelle": "Virement vers compte épargne"
}
```

#### Consulter l'historique d'un compte
```http
GET /api/comptes/{numeroCompte}/historique
GET /api/comptes/{numeroCompte}/historique?dateDebut=2024-01-01&dateFin=2024-12-31
```

**Réponse :**
```json
{
  "numeroCompte": "CPT001234",
  "mouvements": [
    {
      "reference": "MOV001",
      "montant": 250.00,
      "libelle": "Dépôt espèces",
      "dateOperation": "2024-10-15T10:30:00"
    }
  ],
  "count": 1,
  "periode": "2024-01-01 - 2024-12-31"
}
```

---

## 🧪 **Exemples de Tests avec curl**

### Tests de base
```bash
# Test de connectivité
curl http://localhost:8080/centralisateur/api/test/ping

# Test connexion EJB
curl http://localhost:8080/centralisateur/api/test/ejb-connection

# Scénario complet
curl -X POST http://localhost:8080/centralisateur/api/test/scenario-complet
```

### Gestion des clients
```bash
# Créer un client
curl -X POST http://localhost:8080/centralisateur/api/clients \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Martin",
    "prenom": "Marie",
    "email": "marie.martin@email.com",
    "telephone": "0123456789",
    "numCin": "987654321",
    "dateNaissance": "1990-03-20"
  }'

# Lister tous les clients
curl http://localhost:8080/centralisateur/api/clients

# Rechercher un client par email
curl http://localhost:8080/centralisateur/api/clients/email/marie.martin@email.com
```

### Gestion des comptes
```bash
# Créer un compte (remplacer {clientId} par l'ID réel)
curl -X POST http://localhost:8080/centralisateur/api/clients/1/comptes \
  -H "Content-Type: application/json" \
  -d '{
    "soldeActuel": 1500.00,
    "decouvertAutorise": -300.00
  }'

# Consulter le solde
curl http://localhost:8080/centralisateur/api/comptes/CPT001234/solde
```

### Opérations bancaires
```bash
# Effectuer un dépôt
curl -X POST http://localhost:8080/centralisateur/api/comptes/CPT001234/depot \
  -H "Content-Type: application/json" \
  -d '{
    "montant": 200.00,
    "libelle": "Dépôt salaire"
  }'

# Consulter l'historique
curl http://localhost:8080/centralisateur/api/comptes/CPT001234/historique
```

---

## 🎯 **Points Clés de l'Implémentation**

### **Architecture Sans DTOs**
- Utilisation directe des entités JPA du module SituationBancaire
- Communication via reflection pour éviter les problèmes de dépendances
- Retour des objets métiers complets

### **Gestion des Erreurs**
- Logging complet avec java.util.logging
- Codes de retour HTTP appropriés
- Messages d'erreur détaillés

### **Communication EJB**
- Lookup dynamique des beans EJB distants
- Pattern JNDI : `ejb:situation-bancaire/situation-bancaire//BeanName!InterfaceName`
- Gestion des timeouts et reconnexions

### **Fonctionnalités Métiers**
- ✅ Création et gestion complète des clients
- ✅ Gestion des comptes avec soldes en temps réel
- ✅ Toutes les opérations bancaires (dépôt, retrait, virement)
- ✅ Historiques et consultations
- ✅ Blocage/déblocage de comptes
- ✅ Validation des données métiers

---

## 🚀 **Prochaines Étapes**

1. **Interface Frontend** : Intégration avec Thymeleaf
2. **Sécurité** : Authentification et autorisation
3. **Reporting** : Génération de relevés et statistiques
4. **Monitoring** : Métriques et alertes
5. **API Documentation** : Swagger/OpenAPI

Cette implémentation offre maintenant toutes les fonctionnalités bancaires principales via une API REST robuste, prête pour l'intégration frontend !