# CHANGELOG - Module Épargne

## Version 1.0.2 (20 octobre 2025)

### 🐛 Corrections
- **Fix Erreur DateTime UTC avec PostgreSQL**
  - Ajout de `AppContext.SetSwitch("Npgsql.EnableLegacyTimestampBehavior", true)` dans Program.cs
  - Ajout de la méthode `EnsureUtc()` dans ClientService pour convertir les DateTime
  - Création de la migration `EnableLegacyTimestamp` pour convertir les colonnes
  - Conversion de toutes les colonnes DateTime : `timestamp with time zone` → `timestamp without time zone`
  - Documentation complète dans `FIX_DATETIME_UTC.md`
  - **Problème résolu** : `Cannot write DateTime with Kind=Unspecified to PostgreSQL type 'timestamp with time zone'`

## Version 1.0.0 (20 octobre 2025)

### ✨ Fonctionnalités Initiales
- **API REST ASP.NET Core 9.0**
  - Gestion complète des clients
  - Gestion des comptes épargne (Livret A, PEL, CEL)
  - Opérations de dépôt et retrait
  - Calcul automatique des intérêts
  - Historique des opérations

### 🏗️ Architecture
- **Base de données** : PostgreSQL avec Entity Framework Core 9.0
- **Authentification** : Intégrée avec le système centralisé
- **Communication** : REST API HTTP/JSON
- **Technologies** : .NET 9.0, Npgsql, BCrypt

### 📊 Modèles de Données
- Client (synchronisé avec Centralisateur)
- Compte Épargne
- Type de Compte (Livret A, PEL, CEL)
- Opération Épargne
- Intérêt Épargne
- Restriction Épargne

### 🔌 Endpoints API
```
POST   /api/clients              - Créer un client
GET    /api/clients/{id}         - Récupérer un client
PUT    /api/clients/{id}         - Modifier un client
GET    /api/clients/numero/{num} - Récupérer par numéro

POST   /api/comptes              - Créer un compte épargne
GET    /api/comptes/{id}         - Récupérer un compte
GET    /api/comptes/client/{id}  - Comptes d'un client

POST   /api/operations/depot     - Effectuer un dépôt
POST   /api/operations/retrait   - Effectuer un retrait
GET    /api/operations/compte/{id} - Historique des opérations

POST   /api/interets/calculer    - Calculer les intérêts
GET    /api/interets/compte/{id} - Intérêts d'un compte
```

### 📚 Documentation
- `README.md` - Guide d'installation et utilisation
- `FIX_DATETIME_UTC.md` - Résolution problème DateTime PostgreSQL

---

**Développé dans le cadre du projet Architecture Logicielle - S5 ITU**
