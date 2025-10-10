# Module Centralisateur

## Description
Le module centralisateur est le hub central du système bancaire hétérogène. Il orchestre la communication entre tous les autres modules (SituationBancaire, Prêt, Épargne).

## Architecture
- **Type**: Application Java EE (WAR)
- **Communication**: 
  - EJB Remote pour les modules Java (SituationBancaire, Prêt)
  - Web Services pour le module .NET (Épargne)

## Structure du projet
```
src/
├── main/
│   ├── java/
│   │   └── com/banque/centralisateur/
│   │       ├── client/          # Clients EJB pour les modules distants
│   │       ├── config/          # Configuration JAX-RS et autres
│   │       ├── controller/      # Contrôleurs REST
│   │       ├── dto/             # Data Transfer Objects
│   │       ├── filter/          # Filtres HTTP (CORS, etc.)
│   │       └── service/         # Logique métier du centralisateur
│   ├── resources/
│   │   └── application.properties
│   └── webapp/
│       └── WEB-INF/
│           └── web.xml
└── test/
    └── java/
```

## Dépendances principales
- Jakarta EE 8
- EJB API pour les appels distants
- JAX-RS pour les services REST
- Lombok pour réduire le boilerplate

## Configuration
Les paramètres de connexion aux modules distants sont configurés dans `application.properties`.

## Déploiement
Le module se déploie comme un fichier WAR sur un serveur d'applications compatible Jakarta EE (WildFly, Payara, etc.).

## Build
```bash
mvn clean package
```
