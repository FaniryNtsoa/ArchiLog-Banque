# Système Bancaire Hétérogène

## Vue d'ensemble
Système de gestion bancaire composé de **4 modules indépendants** communiquant via une **architecture en étoile** avec le module **Centralisateur** comme hub central.

## Architecture

```
                    ┌─────────────────────┐
                    │   Centralisateur    │
                    │      (JAVA)         │
                    │    Hub Central      │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              │                │                │
    ┌─────────▼─────────┐  ┌──▼──────────┐  ┌──▼──────────┐
    │ SituationBancaire │  │    Prêt     │  │   Épargne   │
    │      (JAVA)       │  │   (JAVA)    │  │    (.NET)   │
    │       EJB         │  │     EJB     │  │ WebService  │
    │   PostgreSQL      │  │ PostgreSQL  │  │  SQL Server │
    └───────────────────┘  └─────────────┘  └─────────────┘
```

## Modules

### 1. 🌟 **Centralisateur** (JAVA - WAR)
- **Rôle** : Hub central orchestrant toute la communication
- **Technologies** : Java 11, Jakarta EE 8, EJB, JAX-RS
- **Communication** :
  - EJB Remote avec SituationBancaire et Prêt
  - Web Services avec Épargne (.NET)
- **Base de données** : Aucune (consomme les APIs des autres modules)

### 2. 💳 **SituationBancaire** (JAVA - WAR) ✅ **Initialisé**
- **Rôle** : Gestion des comptes courants
- **Technologies** : Java 11, JPA/Hibernate, EJB, PostgreSQL
- **Communication** : Expose des EJB Remote au Centralisateur
- **Fonctionnalités** :
  - Gestion des clients
  - Gestion des comptes courants
  - Opérations bancaires (dépôt, retrait, virement)
  - Calcul des intérêts
  - Gestion des frais

### 3. 💰 **Prêt** (JAVA - WAR) 🔄 **À développer**
- **Rôle** : Gestion des prêts bancaires
- **Technologies** : Java 11, JPA/Hibernate, EJB, PostgreSQL
- **Communication** : Expose des EJB Remote au Centralisateur
- **Fonctionnalités** : Demandes de prêt, remboursements, échéanciers

### 4. 🏦 **Épargne** (C# .NET - Web API) 🔄 **À développer**
- **Rôle** : Gestion des comptes d'épargne
- **Technologies** : .NET Core/Framework, Entity Framework, SQL Server
- **Communication** : Expose des Web Services (SOAP/REST) au Centralisateur
- **Fonctionnalités** : Comptes épargne, placements, taux d'intérêt

## Principes architecturaux

### Architecture en étoile
- **Aucune communication directe** entre les modules métier
- **Toute communication** passe par le Centralisateur
- **Indépendance totale** : chaque module a sa propre base de données
- **Résilience** : la défaillance d'un module n'affecte pas les autres

### Technologies de communication
| Module | Technologie | Protocole |
|--------|-------------|-----------|
| SituationBancaire → Centralisateur | EJB Remote | RMI/IIOP |
| Prêt → Centralisateur | EJB Remote | RMI/IIOP |
| Épargne → Centralisateur | Web Services | SOAP/REST |

## Structure du projet

```
banque/
├── Centralisateur/              ✅ Initialisé
│   ├── src/
│   │   ├── main/java/com/banque/centralisateur/
│   │   │   ├── client/          # Clients EJB
│   │   │   ├── config/          # Configuration
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── dto/             # DTOs
│   │   │   ├── filter/          # Filtres HTTP
│   │   │   └── service/         # Services
│   │   └── resources/
│   ├── pom.xml
│   └── README.md
│
├── SituationBancaire/           ✅ Initialisé & Configuré
│   ├── src/
│   │   ├── main/java/com/banque/situationbancaire/
│   │   │   ├── config/          # Configuration
│   │   │   ├── dto/             # DTOs
│   │   │   ├── ejb/remote/      # Interfaces EJB Remote
│   │   │   ├── entity/          # Entités JPA
│   │   │   ├── repository/      # Repositories
│   │   │   ├── service/         # Services métier
│   │   │   └── exception/       # Exceptions
│   │   └── resources/
│   │       ├── META-INF/persistence.xml
│   │       └── application.properties
│   ├── pom.xml
│   └── README.md
│
├── Pret/                        🔄 À développer
│   └── (structure similaire à SituationBancaire)
│
├── Epargne/                     🔄 À développer
│   └── (structure .NET)
│
├── script_situation.sql         ✅ Fourni
└── README.md                    ✅ Ce fichier
```

## État d'avancement

### ✅ Module SituationBancaire (Terminé)
- [x] Structure Maven initialisée
- [x] Configuration JPA/Hibernate
- [x] Toutes les entités JPA créées (10 entités)
- [x] Configuration PostgreSQL
- [x] Interfaces EJB Remote définies
- [x] Repositories (couche DAO) créés
- [x] DTOs créés
- [ ] Implémentation des Session Beans (à faire)
- [ ] Services métier (à faire)
- [ ] Tests unitaires (à faire)

### ✅ Module Centralisateur (Terminé - Structure de base)
- [x] Structure Maven initialisée
- [x] Configuration EJB Client
- [x] Client EJB pour SituationBancaire
- [x] Configuration JAX-RS
- [x] Filtre CORS
- [ ] Contrôleurs REST (à faire)
- [ ] Services orchestrateurs (à faire)

### 🔄 Prochaines étapes prioritaires

1. **Finaliser SituationBancaire** (En cours)
   - Implémenter les Session Beans (ClientServiceBean, CompteCourantServiceBean, OperationServiceBean)
   - Créer les services métier avec logique bancaire
   - Ajouter la gestion des exceptions
   - Tests unitaires

2. **Finaliser Centralisateur**
   - Implémenter les contrôleurs REST
   - Créer les services orchestrateurs
   - Tester la communication EJB

3. **Développer le module Prêt**
   - Structure similaire à SituationBancaire
   - Modèle de données pour les prêts

4. **Développer le module Épargne** (.NET)
   - Web API .NET
   - Communication via Web Services

5. **Frontend avec Thymeleaf**
   - Interface utilisateur web
   - Consommation des APIs du Centralisateur

## Installation & Configuration

### Prérequis
- Java 11+
- Maven 3.6+
- PostgreSQL 12+
- Serveur d'applications Jakarta EE (WildFly 20+, Payara 5+)
- .NET Core 3.1+ (pour le module Épargne)

### Installation rapide

#### 1. Base de données PostgreSQL
```bash
# Créer la base pour SituationBancaire
psql -U postgres
CREATE DATABASE situation_bancaire_db;
\q

# Exécuter le script SQL
psql -U postgres -d situation_bancaire_db -f script_situation.sql
```

#### 2. Compiler les modules Java
```bash
# Module SituationBancaire
cd SituationBancaire
mvn clean package

# Module Centralisateur
cd ../Centralisateur
mvn clean package
```

#### 3. Déployer sur WildFly
```bash
# Copier les WARs dans deployments
cp SituationBancaire/target/situation-bancaire.war $WILDFLY_HOME/standalone/deployments/
cp Centralisateur/target/centralisateur.war $WILDFLY_HOME/standalone/deployments/
```

## Frontend (Thymeleaf - À venir)
Le frontend sera développé avec Thymeleaf et consommera les APIs REST exposées par le Centralisateur.

## Convention de nommage

### Packages
- `com.banque.[module].entity` - Entités JPA
- `com.banque.[module].repository` - Repositories
- `com.banque.[module].service` - Services métier
- `com.banque.[module].ejb.remote` - Interfaces EJB Remote
- `com.banque.[module].dto` - Data Transfer Objects
- `com.banque.[module].exception` - Exceptions personnalisées

### Base de données
- Tables : `snake_case` (ex: `compte_courant`)
- Colonnes : `snake_case` (ex: `id_client`)

## Contribution
Ce projet est organisé de manière claire et modulaire pour faciliter le développement collaboratif.

## Support
Pour toute question, consultez les README de chaque module.

---

**Projet réalisé dans le cadre du cours d'Architecture Logicielle**  
**ITU - Université**
