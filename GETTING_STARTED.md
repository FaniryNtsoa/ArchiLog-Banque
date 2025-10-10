# Guide de démarrage - Système Bancaire Hétérogène

## 🎯 État actuel du projet

✅ **Module SituationBancaire** : Structure complète (entités, repositories, interfaces EJB)  
✅ **Module Centralisateur** : Structure de base (client EJB, configuration)  
🔄 **À compléter** : Implémentation des Session Beans, services métier, tests

## 📋 Prochaines étapes immédiates

### 1. Finaliser le module SituationBancaire

#### A. Implémenter les Session Beans
Créer les implémentations des interfaces Remote dans `src/main/java/com/banque/situationbancaire/ejb/impl/` :

- **ClientServiceBean.java** (implements ClientServiceRemote)
- **CompteCourantServiceBean.java** (implements CompteCourantServiceRemote)
- **OperationServiceBean.java** (implements OperationServiceRemote)

Exemple de structure :
```java
@Stateless
@Remote(ClientServiceRemote.class)
public class ClientServiceBean implements ClientServiceRemote {
    @Inject
    private ClientRepository clientRepository;
    // ... implémentation
}
```

#### B. Créer les services métier
Dans `src/main/java/com/banque/situationbancaire/service/` :

- **ClientService.java** : Logique métier client
- **CompteService.java** : Logique métier compte
- **OperationService.java** : Logique opérations bancaires (validations, limites)

#### C. Gestion des exceptions
Créer dans `src/main/java/com/banque/situationbancaire/exception/` :

- **CompteInexistantException.java**
- **SoldeInsuffisantException.java**
- **PlafondDepasseException.java**
- **CompteInactifException.java**

### 2. Finaliser le module Centralisateur

#### A. Créer les contrôleurs REST
Dans `src/main/java/com/banque/centralisateur/controller/` :

- **ClientController.java** : Endpoints client
- **CompteController.java** : Endpoints compte
- **OperationController.java** : Endpoints opérations

#### B. Créer les services orchestrateurs
Dans `src/main/java/com/banque/centralisateur/service/` :

- **SituationBancaireOrchestrateurService.java** : Orchestre les appels EJB vers SituationBancaire

### 3. Tester la communication EJB

#### Configuration WildFly
1. Configurer le driver PostgreSQL dans WildFly
2. Créer la datasource `SituationBancaireDS`
3. Déployer les deux modules

#### Test de communication
Créer un endpoint de test dans le Centralisateur qui appelle le module SituationBancaire via EJB.

## 🛠️ Commandes utiles

### Compiler les projets
```bash
# SituationBancaire
cd SituationBancaire
mvn clean package

# Centralisateur
cd ../Centralisateur
mvn clean package
```

### Créer la base de données
```bash
psql -U postgres
CREATE DATABASE situation_bancaire_db;
\c situation_bancaire_db
\i script_situation.sql
```

### Déployer sur WildFly
```bash
# Démarrer WildFly
$WILDFLY_HOME/bin/standalone.sh

# Déployer
cp SituationBancaire/target/situation-bancaire.war $WILDFLY_HOME/standalone/deployments/
cp Centralisateur/target/centralisateur.war $WILDFLY_HOME/standalone/deployments/
```

## 📚 Structure des fichiers créés

### SituationBancaire
```
✅ pom.xml
✅ persistence.xml
✅ application.properties
✅ web.xml
✅ situationbancaire-ds.xml
✅ 10 entités JPA (Client, CompteCourant, Mouvement, etc.)
✅ 6 enums (StatutClient, StatutCompte, etc.)
✅ 5 repositories
✅ 3 interfaces EJB Remote
✅ 3 DTOs
⏳ Session Beans (à implémenter)
⏳ Services métier (à implémenter)
⏳ Exceptions (à créer)
```

### Centralisateur
```
✅ pom.xml
✅ application.properties
✅ web.xml
✅ ApplicationConfig.java (JAX-RS)
✅ CorsFilter.java
✅ ReponseDTO.java
✅ SituationBancaireClient.java
⏳ Controllers REST (à créer)
⏳ Services orchestrateurs (à créer)
```

## 🎓 Points importants à retenir

1. **Architecture en étoile** : Toute communication passe par le Centralisateur
2. **Indépendance des modules** : Chaque module a sa propre BD
3. **Communication EJB** : Utilisation de Remote interfaces pour Java → Java
4. **Web Services** : Pour .NET → Java (module Épargne)
5. **JPA/Hibernate** : Pour la persistance des données
6. **Maven** : Gestion des dépendances et build

## 🐛 Résolution de problèmes courants

### Erreurs de compilation (import jakarta)
➡️ **Normal** : Les dépendances Maven doivent être téléchargées. Exécutez `mvn clean install`.

### Erreurs de schéma XML
➡️ **Normal** : Ce sont des warnings d'IDE. Les fichiers sont corrects.

### Problème de connexion PostgreSQL
➡️ Vérifiez que PostgreSQL est démarré et que les credentials dans `application.properties` sont corrects.

### Erreur de lookup JNDI
➡️ Vérifiez que le module SituationBancaire est bien déployé avant le Centralisateur.

## 📧 Contact
Pour toute question sur l'architecture ou l'implémentation, référez-vous aux README de chaque module.

---

**Bon développement ! 🚀**
