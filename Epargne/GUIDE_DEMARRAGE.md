# Guide de démarrage rapide - Module Épargne

## Étape 1 : Vérifier les prérequis

### .NET SDK
```bash
dotnet --version
# Doit afficher 9.0.x ou supérieur
```

### PostgreSQL
```bash
# Vérifier que PostgreSQL est installé et démarré
psql --version
```

## Étape 2 : Créer la base de données

```bash
# Se connecter à PostgreSQL
psql -U postgres

# Créer la base de données
CREATE DATABASE compte_epargne_db;

# Quitter psql
\q
```

## Étape 3 : Configuration

Modifier le fichier `appsettings.json` si nécessaire pour ajuster la chaîne de connexion PostgreSQL :
```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Host=localhost;Port=5432;Database=compte_epargne_db;Username=postgres;Password=VOTRE_MOT_DE_PASSE"
  }
}
```

## Étape 4 : Installation et Migration

```bash
# Restaurer les packages
dotnet restore

# Créer la première migration
dotnet ef migrations add InitialCreate

# Appliquer les migrations
dotnet ef database update
```

## Étape 5 : Démarrer l'application

### Option 1 : Avec le script (Windows)
```bash
start-epargne.bat
```

### Option 2 : Manuellement
```bash
dotnet run
```

L'API sera disponible sur : **http://localhost:5000**

## Étape 6 : Tester l'API

Ouvrez votre navigateur et accédez à : **http://localhost:5000**

Vous verrez la documentation Swagger interactive.

## Tests rapides avec Swagger

### 1. Créer un client
POST `/api/clients`
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

### 2. Se connecter
POST `/api/clients/login`
```json
{
  "email": "jean.dupont@email.com",
  "motDePasse": "password123"
}
```

### 3. Lister les types de comptes disponibles
GET `/api/typescomptes/actifs`

### 4. Créer un compte épargne
POST `/api/comptesepargne`
```json
{
  "idClient": 1,
  "idTypeCompte": 1,
  "libelleCompte": "Mon Livret A",
  "depotInitial": 100.00
}
```

### 5. Effectuer un dépôt
POST `/api/comptesepargne/1/depot`
```json
{
  "montant": 50.00,
  "description": "Dépôt mensuel"
}
```

### 6. Effectuer un retrait
POST `/api/comptesepargne/1/retrait`
```json
{
  "montant": 20.00,
  "description": "Retrait espèces"
}
```

### 7. Consulter l'historique
GET `/api/comptesepargne/1/operations`

## Dépannage

### Erreur de connexion à PostgreSQL
- Vérifier que PostgreSQL est démarré
- Vérifier le mot de passe dans `appsettings.json`
- Vérifier que le port 5432 est ouvert

### Erreur de migration
```bash
# Supprimer les migrations existantes
rm -rf Migrations/

# Recréer les migrations
dotnet ef migrations add InitialCreate
dotnet ef database update
```

### Port déjà utilisé
Modifier le port dans `appsettings.json` :
```json
{
  "Kestrel": {
    "Endpoints": {
      "Http": {
        "Url": "http://localhost:NOUVEAU_PORT"
      }
    }
  }
}
```

## Intégration avec le Centralisateur

Une fois le module Épargne démarré, configurer le Centralisateur Java pour qu'il puisse communiquer avec ce module.

Dans le fichier `application.properties` du Centralisateur :
```properties
ejb.client.module.epargne.url=http://localhost:5000/api
```

## Commandes utiles

```bash
# Vérifier les migrations
dotnet ef migrations list

# Créer une nouvelle migration
dotnet ef migrations add NomDeLaMigration

# Revenir à une migration précédente
dotnet ef database update NomDeLaMigration

# Supprimer la dernière migration (non appliquée)
dotnet ef migrations remove

# Générer un script SQL des migrations
dotnet ef migrations script

# Nettoyer et recompiler
dotnet clean
dotnet build
```

## Prochaines étapes

1. Tester toutes les fonctionnalités via Swagger
2. Intégrer avec le Centralisateur
3. Implémenter l'interface utilisateur dans le Centralisateur
4. Ajouter des tests unitaires
