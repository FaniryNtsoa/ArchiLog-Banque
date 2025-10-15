# Guide de déploiement et test du Centralisateur Bancaire

Ce guide explique comment déployer et tester la communication EJB entre le Centralisateur et le module SituationBancaire.

## Architecture

```
┌─────────────────┐    EJB Remote    ┌─────────────────────┐
│  Centralisateur │ ────────────────► │ SituationBancaire  │
│                 │                  │                     │
│ - REST API      │                  │ - EJB Services      │
│ - EJB Client    │                  │ - Business Logic    │
│ - DTO mapping   │                  │ - JPA Entities      │
└─────────────────┘                  └─────────────────────┘
```

## Prérequis

1. **Java 17** installé et configuré    
2. **Maven 3.9.9** installé et configuré
3. **WildFly Application Server** téléchargé et configuré
4. **PostgreSQL** (optionnel pour les tests complets)

## Structure des projets

```
banque/
├── SituationBancaire/          # Module métier avec EJB
│   ├── src/main/java/
│   │   └── com/banque/situationbancaire/
│   │       ├── ejb/remote/     # Interfaces EJB Remote
│   │       ├── ejb/impl/       # Implémentations EJB
│   │       ├── entity/         # Entités JPA
│   │       └── repository/     # Repositories
│   └── target/
│       └── situation-bancaire.war
├── Centralisateur/             # Hub de communication
│   ├── src/main/java/
│   │   └── com/banque/centralisateur/
│   │       ├── client/         # Client EJB
│   │       ├── service/        # Services centralisés
│   │       ├── rest/           # Endpoints REST de test
│   │       └── dto/            # DTOs pour la communication
│   └── target/
│       └── centralisateur.war
├── deploy-wildfly.ps1          # Script de déploiement
└── README.md                   # Ce fichier
```

## Étapes de déploiement

### 1. Compilation des modules

```powershell
# Compilation SituationBancaire
cd "SituationBancaire"
mvn clean package

# Compilation Centralisateur
cd "../Centralisateur"
mvn clean package
```

### 2. Démarrage de WildFly

```powershell
# Démarrer WildFly (adapter le chemin selon votre installation)
C:\wildfly\bin\standalone.bat
```

WildFly sera accessible sur : http://localhost:8080

### 3. Déploiement automatique

```powershell
# Exécuter le script de déploiement
.\deploy-wildfly.ps1
```

Ou déploiement manuel :
```powershell
# Déployer SituationBancaire
C:\wildfly\bin\jboss-cli.bat --connect --command="deploy SituationBancaire\target\situation-bancaire.war --force"

# Déployer Centralisateur
C:\wildfly\bin\jboss-cli.bat --connect --command="deploy Centralisateur\target\centralisateur.war --force"
```

## Tests de communication EJB

### URLs de test disponibles

Une fois les modules déployés, vous pouvez tester la communication via ces URLs :

#### 1. Test de base (ping)
```
GET http://localhost:8080/centralisateur/api/test/ping
```

**Réponse attendue :**
```json
{
  "status": "OK",
  "message": "Centralisateur is running",
  "timestamp": 1697123456789
}
```

#### 2. Test de connexion EJB
```
GET http://localhost:8080/centralisateur/api/test/ejb-connection
```

**Réponse attendue (succès) :**
```json
{
  "ejbConnectionStatus": "SUCCESS",
  "message": "Connexion EJB avec SituationBancaire établie"
}
```

#### 3. Test de récupération des clients
```
GET http://localhost:8080/centralisateur/api/test/clients
```

**Réponse attendue :**
```json
{
  "clients": [
    {
      "idClient": 1,
      "numeroClient": "CLI001",
      "nom": "Dupont",
      "prenom": "Jean",
      "email": "jean.dupont@email.com",
      "telephone": "0123456789",
      "adresse": "123 Rue de la Paix",
      "numCin": "123456789",
      "situationFamiliale": "CELIBATAIRE",
      "statut": "ACTIF"
    }
  ],
  "count": 2
}
```

#### 4. Test de récupération d'un client par ID
```
GET http://localhost:8080/centralisateur/api/test/clients/1
```

#### 5. Test de récupération d'un client par numéro
```
GET http://localhost:8080/centralisateur/api/test/clients/numero/CLI001
```

### Tests avec curl

```powershell
# Test ping
curl http://localhost:8080/centralisateur/api/test/ping

# Test connexion EJB
curl http://localhost:8080/centralisateur/api/test/ejb-connection

# Test liste des clients
curl http://localhost:8080/centralisateur/api/test/clients

# Test client par ID
curl http://localhost:8080/centralisateur/api/test/clients/1

# Test client par numéro
curl http://localhost:8080/centralisateur/api/test/clients/numero/CLI001
```

### Tests avec un navigateur web

Ouvrez simplement les URLs dans votre navigateur pour voir les réponses JSON.

## Configuration EJB

### Configuration client (Centralisateur)

Le fichier `jboss-ejb-client.properties` configure la connexion EJB :

```properties
endpoint.name=client-endpoint
remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED=false
remote.connections=default

remote.connection.default.host=localhost
remote.connection.default.port=8080
remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS=false
remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT=false
```

### JNDI Lookup Pattern

Le client utilise le pattern JNDI suivant pour les lookups EJB :
```
ejb:<app-name>/<module-name>/<distinct-name>/<bean-name>!<interface-name>
```

Exemple :
```
ejb:situation-bancaire/situation-bancaire//ClientServiceImpl!com.banque.situationbancaire.ejb.remote.ClientServiceRemote
```

## Dépannage

### Erreur de connexion EJB

Si vous obtenez une erreur de connexion :

1. Vérifiez que WildFly est bien démarré
2. Vérifiez que le module SituationBancaire est correctement déployé
3. Consultez les logs WildFly : `C:\wildfly\standalone\log\server.log`
4. Vérifiez la configuration dans `jboss-ejb-client.properties`

### Erreur de déploiement

1. Vérifiez que les ports 8080 et 9990 ne sont pas occupés
2. Assurez-vous que Java 17 est utilisé
3. Vérifiez la compilation Maven (`mvn clean package`)

### Tests qui échouent

1. Attendez quelques secondes après le déploiement
2. Vérifiez que les deux modules sont déployés : http://localhost:8080/
3. Consultez les logs d'application dans WildFly

## Prochaines étapes

1. **Intégration avec base de données** : Configurer PostgreSQL et tester avec de vraies données
2. **Interface utilisateur** : Ajouter Thymeleaf pour les pages web
3. **Sécurité** : Configurer l'authentification et l'autorisation
4. **Monitoring** : Ajouter des métriques et logs avancés
5. **Tests unitaires** : Créer des tests pour les services EJB

## Ressources

- [WildFly Documentation](https://docs.wildfly.org/)
- [EJB 3.2 Specification](https://jakarta.ee/specifications/enterprise-beans/)
- [JAX-RS Documentation](https://jakarta.ee/specifications/restful-ws/)
- [Maven Documentation](https://maven.apache.org/guides/)