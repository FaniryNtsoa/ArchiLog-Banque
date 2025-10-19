# Module Prêt Bancaire

## Description
Ce module gère l'ensemble des opérations liées aux prêts bancaires dans le système bancaire multi-tiers.

## Fonctionnalités

### 1. Simulation de Prêt
- Calcul du montant de la mensualité
- Génération du tableau d'amortissement prévisionnel
- Calcul du coût total du crédit
- Affichage détaillé des intérêts et du capital

### 2. Gestion des Demandes de Prêt
- Création de demandes de prêt
- Validation des conditions d'éligibilité
- Workflow d'approbation/refus (manuel pour l'instant)
- Vérification des montants et durées selon le type de prêt

### 3. Tableau d'Amortissement
- Génération automatique à l'approbation du prêt
- Visualisation du détail capital/intérêts pour chaque échéance
- Mise à jour après chaque remboursement
- Affichage du capital restant dû

### 4. Gestion des Remboursements
- Enregistrement des paiements d'échéances
- Suivi des remboursements effectués
- Historique complet des paiements
- Gestion des retards et pénalités (prévu pour plus tard)

## Architecture

Le module suit l'architecture multi-couches suivante:

```
┌─────────────────────────────────┐
│     Centralisateur (Web)        │
│     (Thymeleaf + Servlets)      │
└───────────┬─────────────────────┘
            │ EJB Remote
            ▼
┌─────────────────────────────────┐
│    Services EJB (Stateless)     │
│  - PretServiceImpl              │
│  - ClientServiceImpl            │
│  - TypePretServiceImpl          │
│  - EcheanceServiceImpl          │
└───────────┬─────────────────────┘
            │
            ▼
┌─────────────────────────────────┐
│       Repositories              │
│  (Accès données JPA)            │
└───────────┬─────────────────────┘
            │
            ▼
┌─────────────────────────────────┐
│    Base de données PostgreSQL   │
│         (pret_db)               │
└─────────────────────────────────┘
```

## Structure du Projet

```
Pret/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/banque/pret/
│       │       ├── entity/           # Entités JPA
│       │       │   ├── Client.java
│       │       │   ├── TypePret.java
│       │       │   ├── Pret.java
│       │       │   ├── Echeance.java
│       │       │   ├── Remboursement.java
│       │       │   └── enums/
│       │       ├── dto/              # Data Transfer Objects
│       │       │   ├── ClientDTO.java
│       │       │   ├── PretDTO.java
│       │       │   ├── SimulationPretDTO.java
│       │       │   └── ...
│       │       ├── mapper/           # Conversions Entity <-> DTO
│       │       ├── repository/       # Accès données
│       │       ├── ejb/
│       │       │   ├── remote/       # Interfaces EJB Remote
│       │       │   └── impl/         # Implémentations EJB
│       │       └── util/             # Utilitaires (calculs)
│       ├── resources/
│       │   ├── META-INF/
│       │   │   └── persistence.xml
│       │   └── application.properties
│       └── webapp/
│           └── WEB-INF/
│               ├── beans.xml
│               ├── web.xml
│               └── pret-ds.xml
└── pom.xml
```

## Technologies Utilisées

- **Java 17**: Langage de programmation
- **Jakarta EE 9.1**: Pour les EJB, JPA, CDI
- **PostgreSQL**: Base de données
- **WildFly**: Serveur d'applications
- **Maven 3.9.9**: Gestion des dépendances et build
- **Hibernate**: Implémentation JPA
- **Lombok**: Réduction du code boilerplate

## Configuration

### Base de données
La configuration de la datasource se trouve dans `src/main/webapp/WEB-INF/pret-ds.xml`:

```xml
<datasource jndi-name="java:jboss/datasources/PretDS" ...>
    <connection-url>jdbc:postgresql://localhost:5432/pret_db</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>postgres</user-name>
        <password>postgres</password>
    </security>
</datasource>
```

### Persistence
La configuration JPA se trouve dans `src/main/resources/META-INF/persistence.xml`

## Compilation et Déploiement

### 1. Compilation
```bash
cd Pret
mvn clean package
```

Cela génère le fichier WAR: `target/pret.war`

### 2. Déploiement sur WildFly
Copier le fichier WAR dans le répertoire de déploiement de WildFly:
```bash
cp target/pret.war %WILDFLY_HOME%/standalone/deployments/
```

Ou via la console d'administration WildFly.

### 3. Configuration de la datasource
Le fichier `pret-ds.xml` sera automatiquement déployé avec le WAR.

## Services EJB Disponibles

### PretServiceRemote
- `simulerPret(SimulationPretDTO)`: Simulation de prêt
- `creerDemandePret(PretDTO)`: Création de demande
- `approuverPret(Long)`: Approbation de prêt
- `refuserPret(Long, String)`: Refus de prêt
- `listerPretsParClient(Long)`: Liste des prêts d'un client

### EcheanceServiceRemote
- `obtenirTableauAmortissement(Long)`: Récupération du tableau d'amortissement
- `enregistrerRemboursement(RemboursementDTO)`: Enregistrement d'un paiement
- `listerEcheancesImpayees(Long)`: Liste des échéances impayées

### ClientServiceRemote
- `creerClient(ClientDTO)`: Création de client
- `authentifierClient(String, String)`: Authentification
- `listerTousLesClients()`: Liste des clients

### TypePretServiceRemote
- `listerTypesPretsActifs()`: Liste des types de prêts disponibles
- `rechercherTypePretParId(Long)`: Détails d'un type de prêt

## Intégration avec le Centralisateur

Le module Prêt s'intègre au centralisateur via EJB Remote. Pour consommer les services depuis le centralisateur:

1. Ajouter la dépendance au client JAR du module Prêt dans le `pom.xml` du centralisateur
2. Utiliser `EJBClientFactory` pour obtenir les instances des services
3. Appeler les méthodes via les interfaces Remote

Exemple:
```java
PretServiceRemote pretService = EJBClientFactory.getPretService();
SimulationPretDTO simulation = pretService.simulerPret(params);
```

## Formules de Calcul

### Mensualité
```
M = C × (t / (1 - (1 + t)^(-n)))

Où:
- M = mensualité
- C = capital emprunté
- t = taux mensuel (taux annuel / 12 / 100)
- n = nombre de mois
```

### Coût Total du Crédit
```
Coût = (Mensualité × Durée) - Capital + Frais de dossier
```

## Notes Importantes

1. **Pénalités**: La gestion des pénalités de retard est prévue mais pas encore implémentée. Les champs sont présents dans la base de données.

2. **Validation Admin**: L'approbation des prêts est pour l'instant manuelle (pas d'interface admin).

3. **Base de Données Indépendante**: Ce module utilise sa propre base de données (`pret_db`) complètement indépendante du module Situation Bancaire.

4. **Sécurité**: Les mots de passe sont hashés avec SHA-256 (à améliorer en production avec bcrypt).

## Prochaines Étapes

1. Création des servlets et vues Thymeleaf dans le Centralisateur
2. Implémentation de la gestion des pénalités
3. Ajout d'un workflow de validation administrateur
4. Amélioration de la sécurité (bcrypt, JWT)
5. Tests unitaires et d'intégration
