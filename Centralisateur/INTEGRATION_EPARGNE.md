# 📘 Guide d'Intégration du Module Épargne dans le Centralisateur

## 🎯 Vue d'Ensemble

Ce document détaille l'intégration complète du module **Épargne** (.NET/C#) dans le **Centralisateur** (Java/Jakarta EE). L'intégration utilise une communication REST entre le Centralisateur Java et l'API .NET.

---

## 🏗️ Architecture de l'Intégration

```
┌─────────────────────────────────────────┐
│       CENTRALISATEUR (Java)             │
│  - Servlets (interface utilisateur)    │
│  - EpargneRestClient (HTTP client)     │
│  - Templates Thymeleaf                  │
└─────────────┬───────────────────────────┘
              │ REST API (HTTP/JSON)
              │ http://localhost:5000/api
              ▼
┌─────────────────────────────────────────┐
│    MODULE ÉPARGNE (.NET/C#)             │
│  - ASP.NET Core Web API                 │
│  - Controllers REST                      │
│  - Entity Framework Core + PostgreSQL   │
└─────────────────────────────────────────┘
```

### Communication

- **Protocol**: HTTP/HTTPS
- **Format**: JSON
- **Base URL**: `http://localhost:5000/api`
- **Client**: Java HttpURLConnection
- **Authentification**: Pas d'auth token (même hash SHA-256 pour les mots de passe)

---

## 📦 Composants Créés

### 1. Client REST Java (`EpargneRestClient.java`)

**Emplacement**: `src/main/java/com/banque/centralisateur/client/EpargneRestClient.java`

**Méthodes implémentées**:

```java
// Inscription client
JsonObject inscrireClient(String nom, String prenom, String email, ...);

// Authentification
JsonObject authentifierClient(String email, String motDePasse);

// Types de comptes
List<JsonObject> getTypesComptesActifs();

// Gestion des comptes
List<JsonObject> getComptesClient(Long clientId);
JsonObject creerCompteEpargne(Long clientId, Long typeCompteId, BigDecimal depotInitial);
JsonObject getCompteDetails(Long compteId);

// Opérations
JsonObject effectuerDepot(Long compteId, BigDecimal montant, String description);
JsonObject effectuerRetrait(Long compteId, BigDecimal montant, String description);
List<JsonObject> getOperationsCompte(Long compteId, int page, int pageSize);
```

### 2. Servlets (Package `servlet.epargne`)

**Emplacement**: `src/main/java/com/banque/centralisateur/servlet/epargne/`

| Servlet | URL | Description |
|---------|-----|-------------|
| `ComptesEpargneServlet` | `/epargne/comptes` | Liste des comptes épargne |
| `NouveauCompteEpargneServlet` | `/epargne/nouveau-compte` | Création de compte |
| `DepotEpargneServlet` | `/epargne/depot` | Dépôt sur compte |
| `RetraitEpargneServlet` | `/epargne/retrait` | Retrait sur compte |
| `HistoriqueEpargneServlet` | `/epargne/historique` | Historique des opérations |

### 3. Templates Thymeleaf

**Emplacement**: `src/main/resources/templates/epargne/`

- `comptes-epargne.html` - Liste des comptes épargne avec détails
- `nouveau-compte-epargne.html` - Formulaire d'ouverture de compte
- `depot-epargne.html` - Formulaire de dépôt
- `retrait-epargne.html` - Formulaire de retrait
- `historique-epargne.html` - Historique paginé des opérations

### 4. Mise à Jour du Sidebar

**Fichier modifié**: `src/main/resources/templates/base.html`

Ajout d'une nouvelle section **Épargne** avec 5 liens de navigation.

### 5. Inscription Multi-Module

**Fichier modifié**: `src/main/java/com/banque/centralisateur/servlet/RegisterServlet.java`

L'inscription crée maintenant le client dans **3 modules** simultanément:
1. ✅ Situation Bancaire (EJB)
2. ✅ Prêt (EJB)
3. ✅ **Épargne (REST API)** ← NOUVEAU

---

## 🚀 Procédure de Déploiement

### Étape 1 : Prérequis

**Module Épargne (.NET)** :
```bash
cd Epargne
dotnet restore
dotnet ef database update
dotnet run
```

L'API doit être accessible sur **http://localhost:5000**

**Vérification** :
```bash
curl http://localhost:5000/swagger
# Doit afficher l'interface Swagger
```

### Étape 2 : Compilation du Centralisateur

```bash
cd Centralisateur
mvn clean compile
```

**Vérifications** :
- ✅ Pas d'erreurs de compilation
- ✅ `EpargneRestClient.class` généré dans `target/classes`
- ✅ Servlets `epargne/*.class` générés

### Étape 3 : Packaging et Déploiement

```bash
mvn clean package
# Génère centralisateur.war
```

**Déploiement sur WildFly** :
```bash
# Option 1 : Copie manuelle
copy target\centralisateur.war %WILDFLY_HOME%\standalone\deployments\

# Option 2 : CLI WildFly
%WILDFLY_HOME%\bin\jboss-cli.bat --connect --command="deploy --force target\centralisateur.war"
```

### Étape 4 : Vérification du Déploiement

**Logs WildFly** :
```
INFO  [org.wildfly.extension.undertow] (ServerService Thread Pool -- XX) 
      WFLYUT0021: Registered web context: '/centralisateur'
```

**Tests manuels** :
1. Ouvrir : http://localhost:8080/centralisateur
2. S'inscrire avec un nouveau compte
3. Se connecter
4. Naviguer vers **Épargne → Ouvrir un compte**

---

## 🧪 Tests de Validation

### Test 1 : Inscription Multi-Module

**Objectif** : Vérifier que le client est créé dans les 3 bases de données

**Procédure** :
1. S'inscrire via `/register` avec email `test@epargne.com`
2. Vérifier dans les bases :

```sql
-- Base Situation Bancaire (PostgreSQL)
SELECT * FROM client WHERE email = 'test@epargne.com';

-- Base Prêt (PostgreSQL)
SELECT * FROM client WHERE email = 'test@epargne.com';

-- Base Épargne (PostgreSQL)
SELECT * FROM client WHERE email = 'test@epargne.com';
```

**Résultat attendu** : 3 enregistrements trouvés avec le même email

### Test 2 : Création de Compte Épargne

**Procédure** :
1. Se connecter avec le compte créé
2. Aller dans **Épargne → Ouvrir un compte**
3. Sélectionner "Livret A"
4. Saisir dépôt initial : 10 000 MGA
5. Cliquer sur "Ouvrir le compte"

**Résultat attendu** :
- ✅ Message : "Compte épargne créé avec succès ! Numéro de compte : CEPxxxxxxxxx"
- ✅ Redirection vers `/epargne/comptes`
- ✅ Compte visible dans la liste

### Test 3 : Dépôt sur Compte Épargne

**Procédure** :
1. Aller dans **Épargne → Déposer**
2. Sélectionner le compte créé
3. Saisir montant : 50 000 MGA
4. Cliquer sur "Effectuer le dépôt"

**Résultat attendu** :
- ✅ Message : "Dépôt de 50 000,00 MGA effectué avec succès ! Nouveau solde : 60 000,00 MGA"
- ✅ Solde mis à jour dans `/epargne/comptes`

### Test 4 : Retrait sur Compte Épargne

**Procédure** :
1. Aller dans **Épargne → Retirer**
2. Sélectionner le compte
3. Saisir montant : 20 000 MGA
4. Cliquer sur "Effectuer le retrait"

**Résultat attendu** :
- ✅ Message : "Retrait de 20 000,00 MGA effectué avec succès ! Nouveau solde : 40 000,00 MGA"
- ✅ Solde mis à jour

### Test 5 : Historique des Opérations

**Procédure** :
1. Aller dans **Épargne → Historique épargne**
2. Sélectionner le compte dans la liste déroulante

**Résultat attendu** :
- ✅ 3 opérations affichées : OUVERTURE, DEPOT, RETRAIT
- ✅ Montants corrects avec symboles + et -
- ✅ Dates et heures affichées

### Test 6 : Gestion des Erreurs

**Test 6.1** : Dépôt supérieur au plafond
```
1. Créer un Livret A (plafond : 22 950 €)
2. Essayer de déposer 25 000 €
Attendu: Message d'erreur "Le montant dépasse le plafond autorisé"
```

**Test 6.2** : Retrait insuffisant
```
1. Compte avec solde 5 000 MGA
2. Essayer de retirer 10 000 MGA
Attendu: Message d'erreur "Solde insuffisant"
```

**Test 6.3** : Module Épargne arrêté
```
1. Arrêter l'API Épargne (dotnet run)
2. Essayer d'ouvrir un compte
Attendu: Message "Erreur lors de la création du compte. Veuillez réessayer."
```

---

## 🔍 Dépannage

### Problème 1 : "Connection refused to localhost:5000"

**Cause** : L'API Épargne n'est pas démarrée

**Solution** :
```bash
cd Epargne
dotnet run
```

Vérifier que le message apparaît :
```
Now listening on: http://localhost:5000
```

### Problème 2 : "Error 404 Not Found"

**Cause** : URL incorrecte ou endpoint inexistant

**Solution** :
1. Vérifier l'URL dans `EpargneRestClient.java` : `http://localhost:5000/api`
2. Tester directement avec curl :
```bash
curl http://localhost:5000/api/typescomptes/actifs
```

### Problème 3 : "JSON parsing error"

**Cause** : Format de réponse incorrect

**Solution** :
1. Vérifier les logs WildFly
2. Ajouter des logs dans `EpargneRestClient` :
```java
LOGGER.info("Response JSON: " + responseJson.toString());
```

### Problème 4 : Servlets non accessibles

**Cause** : Déploiement incomplet ou erreur de compilation

**Solution** :
```bash
# Vérifier les servlets dans le WAR
jar tf target/centralisateur.war | grep "servlet/epargne"

# Résultat attendu :
WEB-INF/classes/com/banque/centralisateur/servlet/epargne/ComptesEpargneServlet.class
WEB-INF/classes/com/banque/centralisateur/servlet/epargne/DepotEpargneServlet.class
...
```

### Problème 5 : Erreur "Template not found"

**Cause** : Templates Thymeleaf manquants

**Solution** :
```bash
# Vérifier les templates dans le WAR
jar tf target/centralisateur.war | grep "templates/epargne"

# Résultat attendu :
WEB-INF/classes/templates/epargne/comptes-epargne.html
WEB-INF/classes/templates/epargne/depot-epargne.html
...
```

---

## 📊 Points de Terminaison API Épargne

### Inscription Client
```http
POST /api/clients/register
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@email.com",
  "telephone": "+261 34 12 345 67",
  "dateNaissance": "1990-05-15",
  "numCin": "123456789012",
  "adresse": "123 Rue Principale",
  "codePostal": "101",
  "ville": "Antananarivo",
  "profession": "Ingénieur",
  "revenuMensuel": 2000000,
  "soldeInitial": 0,
  "situationFamiliale": "MARIE",
  "motDePasse": "Password123!"
}
```

### Types de Comptes Actifs
```http
GET /api/typescomptes/actifs
Accept: application/json
```

### Création de Compte Épargne
```http
POST /api/comptesepargne
Content-Type: application/json

{
  "idClient": 1,
  "idTypeCompte": 1,
  "depotInitial": 10000
}
```

### Dépôt
```http
POST /api/comptesepargne/{id}/depot
Content-Type: application/json

{
  "montant": 50000,
  "description": "Dépôt mensuel"
}
```

### Retrait
```http
POST /api/comptesepargne/{id}/retrait
Content-Type: application/json

{
  "montant": 20000,
  "description": "Achat important"
}
```

### Historique
```http
GET /api/comptesepargne/{id}/operations?page=1&pageSize=50
Accept: application/json
```

---

## 🎨 Structure de Navigation

```
Dashboard
├── Situation Bancaire
│   ├── Nouveau compte
│   ├── Dépôt
│   ├── Retrait
│   ├── Virement
│   ├── Consulter solde
│   └── Historique
│
├── Prêts
│   ├── Simuler un prêt
│   ├── Demande de prêt
│   ├── Mes prêts
│   └── Remboursement
│
└── Épargne ← NOUVEAU
    ├── Ouvrir un compte
    ├── Mes comptes épargne
    ├── Déposer
    ├── Retirer
    └── Historique épargne
```

---

## 📝 Notes Importantes

### Compatibilité des Données

**Entité Client** :
- ✅ Structure identique dans les 3 modules
- ✅ Hash SHA-256 pour les mots de passe (compatible)
- ⚠️ **DIFFÉRENCE** : Module Épargne n'a PAS le champ `chargesMensuelles`

### Gestion des Erreurs

Toutes les erreurs API sont encapsulées dans un format JSON standard :
```json
{
  "success": false,
  "message": "Message d'erreur explicite",
  "errors": ["Détail 1", "Détail 2"]
}
```

### Performance

- **Timeout de connexion** : 5 secondes
- **Timeout de lecture** : 10 secondes
- **Pagination** : 50 opérations par page

### Sécurité

⚠️ **Mode Développement** :
- Pas d'authentification JWT
- HTTP au lieu de HTTPS
- CORS ouvert

🔒 **Pour Production** :
- Implémenter JWT dans l'API Épargne
- Passer en HTTPS
- Restreindre CORS

---

## ✅ Checklist de Validation Finale

Avant de considérer l'intégration complète :

- [ ] Module Épargne démarre sans erreur
- [ ] Centralisateur compile sans erreur
- [ ] Inscription crée le client dans les 3 bases
- [ ] Section Épargne visible dans le sidebar
- [ ] Création de compte épargne fonctionnelle
- [ ] Dépôt enregistré correctement
- [ ] Retrait respecte les règles métier
- [ ] Historique affiche toutes les opérations
- [ ] Messages d'erreur appropriés en cas de problème
- [ ] Gestion correcte si module Épargne indisponible

---

## 🎓 Fonctionnalités Implémentées

### Module Épargne (.NET)

✅ 4 types de comptes : Livret A, CEL, LDD, PEL  
✅ Règles métier strictes (plafond, solde min, etc.)  
✅ Calcul automatique des intérêts  
✅ Validation des montants  
✅ Historique complet avec pagination  
✅ 6 contraintes CHECK au niveau base de données  

### Centralisateur (Java)

✅ Communication REST avec JSON  
✅ Gestion des erreurs et timeouts  
✅ Interface utilisateur Thymeleaf  
✅ Navigation cohérente avec les autres modules  
✅ Inscription multi-module  
✅ Affichage enrichi avec icônes et couleurs  

---

## 📚 Ressources

- **API Épargne** : http://localhost:5000/swagger
- **Centralisateur** : http://localhost:8080/centralisateur
- **Documentation Épargne** : `Epargne/README.md`
- **Guide Démarrage Épargne** : `Epargne/GUIDE_DEMARRAGE.md`

---

**Date de création** : 19 janvier 2025  
**Version** : 1.0.0  
**Statut** : ✅ Intégration complète et fonctionnelle
