# Module SituationBancaire

## Description
Module de gestion des comptes courants et situations bancaires. Ce module fait partie d'un système bancaire hétérogène et communique avec le module Centralisateur via EJB Remote.

## Technologies
- **Java 11**
- **Maven** - Gestion des dépendances
- **JPA / Hibernate** - ORM pour la persistance
- **PostgreSQL** - Base de données
- **EJB 3.2** - Communication inter-modules
- **Jakarta EE 8** - Spécifications entreprise

## Architecture
```
src/
├── main/
│   ├── java/
│   │   └── com/banque/situationbancaire/
│   │       ├── config/              # Configuration JAX-RS
│   │       ├── dto/                 # Data Transfer Objects
│   │       │   ├── ClientDTO.java
│   │       │   ├── CompteCourantDTO.java
│   │       │   └── OperationDTO.java
│   │       ├── ejb/
│   │       │   └── remote/          # Interfaces EJB Remote
│   │       │       ├── ClientServiceRemote.java
│   │       │       ├── CompteCourantServiceRemote.java
│   │       │       └── OperationServiceRemote.java
│   │       ├── entity/              # Entités JPA
│   │       │   ├── enums/           # Énumérations
│   │       │   ├── Client.java
│   │       │   ├── CompteCourant.java
│   │       │   ├── Mouvement.java
│   │       │   ├── Virement.java
│   │       │   ├── TypeCompte.java
│   │       │   ├── TypeOperation.java
│   │       │   ├── ParametresCompte.java
│   │       │   ├── TauxInteretDecouvert.java
│   │       │   ├── InteretJournalier.java
│   │       │   └── FraisTenueHistorique.java
│   │       ├── repository/          # Couche d'accès aux données
│   │       │   ├── ClientRepository.java
│   │       │   ├── CompteCourantRepository.java
│   │       │   ├── MouvementRepository.java
│   │       │   ├── TypeCompteRepository.java
│   │       │   └── TypeOperationRepository.java
│   │       ├── service/             # Logique métier (à implémenter)
│   │       └── exception/           # Exceptions personnalisées (à implémenter)
│   ├── resources/
│   │   ├── META-INF/
│   │   │   └── persistence.xml      # Configuration JPA
│   │   └── application.properties   # Configuration application
│   └── webapp/
│       └── WEB-INF/
│           ├── web.xml
│           └── situationbancaire-ds.xml  # DataSource configuration
└── test/
    └── java/                        # Tests unitaires
```

## Modèle de données

### Entités principales
1. **Client** - Informations client (nom, prénom, CIN, email, etc.)
2. **CompteCourant** - Compte bancaire d'un client
3. **Mouvement** - Opérations bancaires (débit/crédit)
4. **Virement** - Transferts entre comptes
5. **TypeCompte** - Types de compte (Étudiant, Standard, Business, Premium)
6. **TypeOperation** - Types d'opérations (Dépôt, Retrait, Virement, etc.)
7. **ParametresCompte** - Paramètres et limites par type de compte
8. **TauxInteretDecouvert** - Taux d'intérêt pour découverts
9. **InteretJournalier** - Calcul journalier des intérêts
10. **FraisTenueHistorique** - Historique des frais de tenue de compte

## Configuration

### Base de données
Modifiez `src/main/resources/application.properties` :
```properties
db.host=localhost
db.port=5432
db.name=situation_bancaire_db
db.username=postgres
db.password=postgres
```

### DataSource (WildFly/JBoss)
Le fichier `situationbancaire-ds.xml` configure la source de données PostgreSQL.

### Persistence
Le fichier `persistence.xml` configure l'unité de persistance JPA avec Hibernate.

## Build & Déploiement

### Prérequis
- Java 11+
- Maven 3.6+
- PostgreSQL 12+
- Serveur d'applications Jakarta EE (WildFly, Payara, etc.)

### Compilation
```bash
cd SituationBancaire
mvn clean package
```

### Déploiement
1. Créer la base de données PostgreSQL :
```bash
psql -U postgres
CREATE DATABASE situation_bancaire_db;
```

2. Exécuter le script SQL `script_situation.sql` pour créer les tables

3. Déployer le WAR généré sur le serveur d'applications :
```bash
# Sur WildFly
cp target/situation-bancaire.war $WILDFLY_HOME/standalone/deployments/
```

## Communication EJB

### Interfaces Remote exposées
- **ClientServiceRemote** - Gestion des clients
- **CompteCourantServiceRemote** - Gestion des comptes
- **OperationServiceRemote** - Gestion des opérations

Ces interfaces sont accessibles par le module Centralisateur via lookup JNDI.

## Prochaines étapes
1. Implémenter les Session Beans (implémentation des interfaces Remote)
2. Créer les services métier avec logique bancaire
3. Ajouter la gestion des exceptions
4. Implémenter les validations métier
5. Ajouter les tests unitaires
6. Créer les contrôleurs REST (optionnel)

## Auteur
Projet de gestion bancaire - Module SituationBancaire
