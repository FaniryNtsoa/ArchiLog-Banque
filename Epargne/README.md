# Module Épargne - Système Bancaire Multi-tiers

## Description
Module .NET Core pour la gestion des comptes épargne, faisant partie d'un système bancaire multi-tiers hétérogène.

## Architecture
- **Backend**: ASP.NET Core Web API (.NET 9.0)
- **ORM**: Entity Framework Core
- **Base de données**: PostgreSQL
- **Serveur Web**: Kestrel

## Structure du Projet
```
Epargne/
├── Models/
│   ├── Entities/          # Entités EF Core (Client, CompteEpargne, etc.)
│   ├── DTOs/             # Data Transfer Objects pour l'API
│   └── Enums/            # Énumérations (StatutClient, CompteStatut, etc.)
├── Data/                 # DbContext et configuration EF Core
├── Repositories/         # Couche d'accès aux données
├── Services/             # Logique métier
├── Controllers/          # Contrôleurs API REST
└── Utilities/            # Utilitaires (hachage, calculs, etc.)
```

## Fonctionnalités Principales

### 1. Gestion des Clients
- ✅ Inscription/Connexion client
- ✅ Profil client compatible avec les modules Prêt et Situation Bancaire
- ✅ Authentification par email/mot de passe (SHA-256)

### 2. Ouverture de Compte Épargne
- ✅ Dépôt initial respectant le minimum requis
- ✅ Attribution automatique de numéro de compte unique
- ✅ Support de plusieurs types de comptes (Livret A, CEL, LDD, PEL)

### 3. Gestion des Dépôts/Retraits
- ✅ Contrôle des plafonds de retrait (% du solde)
- ✅ Vérification du solde minimum après retrait
- ✅ Historique complet des opérations
- ✅ Traçabilité avec références d'opération

### 4. Calcul des Intérêts
- ✅ Calcul périodique selon le type de compte (quotidien, mensuel, trimestriel)
- ✅ Méthode du solde moyen ou solde minimum
- ✅ Capitalisation automatique
- ✅ Formule: `Intérêts = Solde moyen × Taux annuel × (Nombre de jours / 365)`

## Règles Métier Implémentées

### Dépôts
- Respect du plafond de dépôt selon le type de compte
- Montant minimum selon le type de compte

### Retraits
- **Montant retrait max** = Solde × Pourcentage_autorisé
- **Solde après retrait** ≥ Solde minimum obligatoire
- Certains comptes (PEL) ne permettent pas de retraits partiels

### Intérêts
- **Quotidien**: Calcul tous les jours, capitalisation mensuelle
- **Mensuel**: Calcul en fin de mois
- **Trimestriel**: Calcul tous les 3 mois

## Types de Comptes Pré-configurés

| Type | Taux | Dépôt Min | Solde Min | Plafond | Retrait Max |
|------|------|-----------|-----------|---------|-------------|
| Livret A | 3.00% | 10 € | 10 € | 22 950 € | 100% |
| CEL | 2.00% | 300 € | 300 € | 15 300 € | 50% |
| LDD | 3.00% | 15 € | 15 € | 12 000 € | 100% |
| PEL | 2.25% | 225 € | 225 € | 61 200 € | 0% |

## API REST Endpoints

### Clients
- `POST /api/clients` - Créer un client
- `POST /api/clients/login` - Authentifier un client
- `GET /api/clients/{id}` - Récupérer un client
- `GET /api/clients/by-email/{email}` - Récupérer par email
- `PUT /api/clients/{id}` - Mettre à jour un client

### Types de Comptes
- `GET /api/typescomptes` - Liste tous les types
- `GET /api/typescomptes/actifs` - Liste les types actifs
- `GET /api/typescomptes/{id}` - Détails d'un type

### Comptes Épargne
- `POST /api/comptesepargne` - Créer un compte (avec dépôt initial)
- `GET /api/comptesepargne/{id}` - Détails d'un compte
- `GET /api/comptesepargne/numero/{numero}` - Recherche par numéro
- `GET /api/comptesepargne/client/{clientId}` - Comptes d'un client
- `POST /api/comptesepargne/{id}/depot` - Effectuer un dépôt
- `POST /api/comptesepargne/{id}/retrait` - Effectuer un retrait
- `GET /api/comptesepargne/{id}/operations` - Historique des opérations
- `GET /api/comptesepargne/{id}/solde-disponible` - Solde disponible
- `GET /api/comptesepargne/{id}/interets` - Historique des intérêts
- `POST /api/comptesepargne/{id}/calculer-interets` - Calculer les intérêts
- `POST /api/comptesepargne/{id}/capitaliser-interets` - Capitaliser les intérêts

## Installation et Configuration

### Prérequis
- .NET 9.0 SDK
- PostgreSQL 12+
- Visual Studio Code ou Visual Studio 2022

### Configuration de la Base de Données

1. Créer la base de données PostgreSQL:
```sql
CREATE DATABASE compte_epargne_db;
```

2. Configurer la chaîne de connexion dans `appsettings.json`:
```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Host=localhost;Port=5432;Database=compte_epargne_db;Username=postgres;Password=votre_mot_de_passe"
  }
}
```

3. Appliquer les migrations:
```bash
dotnet ef migrations add InitialCreate
dotnet ef database update
```

### Démarrage

```bash
cd Epargne
dotnet restore
dotnet build
dotnet run
```

L'API sera disponible sur: `http://localhost:5000`
La documentation Swagger sera disponible sur: `http://localhost:5000`

## Intégration avec le Centralisateur

Le module Épargne communique avec le Centralisateur Java via **Web Services REST**.

Configuration dans le Centralisateur (`application.properties`):
```properties
ejb.client.module.epargne.url=http://localhost:5000/api
```

## Sécurité
- Mots de passe hachés avec SHA-256 (compatible avec les modules Java)
- CORS configuré pour permettre les appels du Centralisateur
- Validation des données avec Data Annotations

## Logging
Les logs sont configurés pour:
- Information générale des opérations
- Détails des transactions (dépôts, retraits)
- Calculs d'intérêts
- Erreurs avec stack traces

## Tests

Pour tester l'API avec Swagger:
1. Démarrer l'application
2. Naviguer vers `http://localhost:5000`
3. Utiliser l'interface Swagger UI pour tester les endpoints

Exemple de création de client:
```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "dateNaissance": "1990-01-01",
  "numCin": "123456789",
  "email": "jean.dupont@email.com",
  "telephone": "0612345678",
  "adresse": "123 Rue Example",
  "codePostal": "75001",
  "ville": "Paris",
  "profession": "Ingénieur",
  "revenuMensuel": 3000.00,
  "chargesMensuelles": 800.00,
  "soldeInitial": 0.00,
  "situationFamiliale": "CELIBATAIRE",
  "motDePasse": "password123"
}
```

Exemple de création de compte épargne:
```json
{
  "idClient": 1,
  "idTypeCompte": 1,
  "libelleCompte": "Mon Livret A",
  "depotInitial": 100.00
}
```

## Développement Futur
- [ ] Ajout d'un système de notifications
- [ ] Tableau de bord client
- [ ] Export des relevés en PDF
- [ ] Gestion des virements entre comptes épargne
- [ ] API de prévision d'intérêts

## Auteurs
Module développé dans le cadre du projet Architecture Logicielle Multi-tiers

## Licence
Projet académique - ITU
