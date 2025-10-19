# 🏗️ Architecture de Déploiement - Système Bancaire

**Date :** 19 octobre 2025  
**Système :** Multi-modules avec EJB Remote  
**Serveur d'application :** WildFly 37

---

## 📐 Architecture Globale

```
┌─────────────────────────────────────────────────────────────────┐
│                    NAVIGATEUR WEB (Client)                       │
│                         http://localhost:9080                    │
└────────────────────────────────┬─────────────────────────────────┘
                                 │
                                 │ HTTP
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                   CENTRALISATEUR (Web App)                       │
│                   WildFly sur port 9080                          │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  Servlets Thymeleaf (Présentation)                         │ │
│  │  - LoginServlet, DashboardServlet                          │ │
│  │  - DepotServlet, RetraitServlet, VirementServlet          │ │
│  │  - SimulationPretServlet, DemandePretServlet, etc.        │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  EJB Client Factory (Communication)                        │ │
│  │  - Lookup EJB Remote                                       │ │
│  │  - Gestion des connexions                                  │ │
│  └────────────────────────────────────────────────────────────┘ │
└────────────────┬────────────────────────┬───────────────────────┘
                 │ EJB Remote             │ EJB Remote
                 │ http-remoting:8080     │ http-remoting:8180
                 ▼                        ▼
┌─────────────────────────────┐  ┌─────────────────────────────┐
│  SITUATION BANCAIRE         │  │  PRET                       │
│  WildFly sur port 8080      │  │  WildFly sur port 8180      │
│  ┌────────────────────────┐ │  │  ┌────────────────────────┐ │
│  │  EJB Stateless         │ │  │  │  EJB Stateless         │ │
│  │  @Remote Services      │ │  │  │  @Remote Services      │ │
│  │  - ClientService       │ │  │  │  - PretService         │ │
│  │  - CompteService       │ │  │  │  - EcheanceService     │ │
│  │  - MouvementService    │ │  │  │  - TypePretService     │ │
│  └────────────────────────┘ │  │  └────────────────────────┘ │
│             │                │  │             │                │
│             │ JPA            │  │             │ JPA            │
│             ▼                │  │             ▼                │
│  ┌────────────────────────┐ │  │  ┌────────────────────────┐ │
│  │  Entities + Repos      │ │  │  │  Entities + Repos      │ │
│  │  (Hibernate)           │ │  │  │  (Hibernate)           │ │
│  └────────────────────────┘ │  │  └────────────────────────┘ │
└──────────────┬──────────────┘  └──────────────┬──────────────┘
               │ JDBC                            │ JDBC
               │ postgres:5432                   │ postgres:5432
               ▼                                 ▼
┌─────────────────────────────┐  ┌─────────────────────────────┐
│  PostgreSQL                 │  │  PostgreSQL                 │
│  situation_bancaire_db      │  │  pret_db                    │
│  Port: 5432                 │  │  Port: 5432                 │
└─────────────────────────────┘  └─────────────────────────────┘
```

---

## 🔧 Configuration des Ports

| Module | HTTP | Management | Remoting | Database |
|--------|------|------------|----------|----------|
| **SituationBancaire** | 8080 | 9990 | 4447 | 5432 |
| **Prêt** | 8180 | 10090 | 4547 | 5432 |
| **Centralisateur** | 9080 | 10190 | 4647 | - |

### Offset des ports

- **SituationBancaire** : Offset **0** (ports par défaut)
- **Prêt** : Offset **+100** (tous les ports + 100)
- **Centralisateur** : Offset **+1000** (tous les ports + 1000)

---

## 📁 Structure des Répertoires WildFly

```
C:\wildfly-37.0.1.Final\
├── bin\
│   ├── standalone.bat          # Script de démarrage
│   └── jboss-cli.bat            # Client d'administration
│
├── standalone\                  # Instance pour SituationBancaire (port 8080)
│   ├── configuration\
│   │   └── standalone.xml
│   ├── deployments\
│   │   ├── situation-bancaire.war
│   │   ├── situation-bancaire.war.deployed
│   │   └── situationbancaire-ds.xml
│   └── log\
│       └── server.log
│
├── standalone-pret\             # Instance pour Prêt (port 8180)
│   ├── configuration\
│   │   └── standalone.xml
│   ├── deployments\
│   │   ├── pret.war
│   │   ├── pret.war.deployed
│   │   └── pret-ds.xml
│   └── log\
│       └── server.log
│
└── standalone-centralisateur\   # Instance pour Centralisateur (port 9080)
    ├── configuration\
    │   └── standalone.xml
    ├── deployments\
    │   └── centralisateur.war
    └── log\
        └── server.log
```

---

## 🚀 Démarrage des Instances

### 1. SituationBancaire (Port 8080)

**Terminal 1 :**
```bash
cd %WILDFLY_HOME%\bin
standalone.bat
```

**Ou créer `start-wildfly-situation.bat` :**
```batch
@echo off
cd /d "%WILDFLY_HOME%\bin"
call standalone.bat
```

**Vérification :**
- URL : http://localhost:8080
- Management : http://localhost:9990

---

### 2. Prêt (Port 8180)

**Terminal 2 :**
```bash
cd %WILDFLY_HOME%\bin
standalone.bat -Djboss.socket.binding.port-offset=100 -Djboss.server.base.dir="%WILDFLY_HOME%\standalone-pret"
```

**Ou utiliser le script fourni `start-wildfly-pret.bat` :**
```batch
.\start-wildfly-pret.bat
```

**Vérification :**
- URL : http://localhost:8180
- Management : http://localhost:10090

---

### 3. Centralisateur (Port 9080)

**Terminal 3 :**
```bash
cd %WILDFLY_HOME%\bin
standalone.bat -Djboss.socket.binding.port-offset=1000 -Djboss.server.base.dir="%WILDFLY_HOME%\standalone-centralisateur"
```

**Ou créer `start-wildfly-centralisateur.bat` :**
```batch
@echo off
cd /d "%WILDFLY_HOME%\bin"
call standalone.bat -Djboss.socket.binding.port-offset=1000 -Djboss.server.base.dir="%WILDFLY_HOME%\standalone-centralisateur"
```

**Vérification :**
- URL : http://localhost:9080/centralisateur
- Management : http://localhost:10190

---

## 🔌 Communication EJB Remote

### Configuration Centralisateur → SituationBancaire

**Fichier :** `Centralisateur/src/main/resources/jboss-ejb-client.properties`

```properties
# Configuration du client EJB pour WildFly
endpoint.name=client-endpoint
remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED=false

# Connexion à SituationBancaire (port 8080)
remote.connections=situation

remote.connection.situation.host=localhost
remote.connection.situation.port=8080
remote.connection.situation.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS=false
remote.connection.situation.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT=false
```

### Configuration Centralisateur → Prêt

**Ajouter dans le même fichier :**
```properties
# Connexion à Prêt (port 8180)
remote.connections=situation,pret

remote.connection.pret.host=localhost
remote.connection.pret.port=8180
remote.connection.pret.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS=false
remote.connection.pret.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT=false
```

---

## 📝 Lookup JNDI des EJBs

### Depuis le Centralisateur

**Pour SituationBancaire :**
```java
String jndiName = "ejb:/situation-bancaire/ClientServiceImpl!" +
                  "com.banque.situationbancaire.ejb.remote.ClientServiceRemote";

Properties props = new Properties();
props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");

Context context = new InitialContext(props);
ClientServiceRemote clientService = (ClientServiceRemote) context.lookup(jndiName);
```

**Pour Prêt :**
```java
String jndiName = "ejb:/pret/PretServiceImpl!" +
                  "com.banque.pret.ejb.remote.PretServiceRemote";

Properties props = new Properties();
props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
props.put(Context.PROVIDER_URL, "http-remoting://localhost:8180");  // ← Port 8180

Context context = new InitialContext(props);
PretServiceRemote pretService = (PretServiceRemote) context.lookup(jndiName);
```

---

## 🗄️ Configuration des DataSources

### SituationBancaire DataSource

**Fichier :** `%WILDFLY_HOME%\standalone\deployments\situationbancaire-ds.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<datasources xmlns="http://www.jboss.org/ironjacamar/schema">
    <datasource jndi-name="java:jboss/datasources/SituationBancaireDS"
                pool-name="SituationBancaireDS"
                enabled="true">
        <connection-url>jdbc:postgresql://localhost:5432/situation_bancaire_db</connection-url>
        <driver>postgresql</driver>
        <security>
            <user-name>postgres</user-name>
            <password>postgres</password>
        </security>
    </datasource>
</datasources>
```

---

### Prêt DataSource

**Fichier :** `%WILDFLY_HOME%\standalone-pret\deployments\pret-ds.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<datasources xmlns="http://www.jboss.org/ironjacamar/schema">
    <datasource jndi-name="java:jboss/datasources/PretDS"
                pool-name="PretDS"
                enabled="true">
        <connection-url>jdbc:postgresql://localhost:5432/pret_db</connection-url>
        <driver>postgresql</driver>
        <security>
            <user-name>postgres</user-name>
            <password>postgres</password>
        </security>
    </datasource>
</datasources>
```

---

## 📦 Ordre de Déploiement

### Étape 1 : Créer les répertoires WildFly

```bash
# Copier la structure standalone pour Prêt
xcopy /E /I "%WILDFLY_HOME%\standalone" "%WILDFLY_HOME%\standalone-pret"

# Copier la structure standalone pour Centralisateur
xcopy /E /I "%WILDFLY_HOME%\standalone" "%WILDFLY_HOME%\standalone-centralisateur"

# Nettoyer les déploiements copiés
del "%WILDFLY_HOME%\standalone-pret\deployments\*.war"
del "%WILDFLY_HOME%\standalone-centralisateur\deployments\*.war"
```

---

### Étape 2 : Démarrer SituationBancaire

```bash
# Terminal 1
cd %WILDFLY_HOME%\bin
standalone.bat

# Attendre : "Started Server"
```

**Déployer :**
```bash
copy SituationBancaire\target\situation-bancaire.war %WILDFLY_HOME%\standalone\deployments\
copy SituationBancaire\src\main\webapp\WEB-INF\situationbancaire-ds.xml %WILDFLY_HOME%\standalone\deployments\
```

---

### Étape 3 : Démarrer Prêt

```bash
# Terminal 2
.\Pret\start-wildfly-pret.bat

# Attendre : "Started Server"
```

**Déployer :**
```bash
copy Pret\target\pret.war %WILDFLY_HOME%\standalone-pret\deployments\
copy Pret\src\main\webapp\WEB-INF\pret-ds.xml %WILDFLY_HOME%\standalone-pret\deployments\
```

---

### Étape 4 : Démarrer Centralisateur

```bash
# Terminal 3
cd %WILDFLY_HOME%\bin
standalone.bat -Djboss.socket.binding.port-offset=1000 -Djboss.server.base.dir="%WILDFLY_HOME%\standalone-centralisateur"

# Attendre : "Started Server"
```

**Déployer :**
```bash
copy Centralisateur\target\centralisateur.war %WILDFLY_HOME%\standalone-centralisateur\deployments\
```

---

## ✅ Vérification du Déploiement

### Test des URLs

| Module | URL | Statut Attendu |
|--------|-----|----------------|
| SituationBancaire | http://localhost:8080 | Page d'accueil |
| Prêt | http://localhost:8180 | Page d'accueil |
| Centralisateur | http://localhost:9080/centralisateur | Application complète |

---

### Test des EJBs

**Créer un test simple :**
```java
// Test SituationBancaire
Properties props1 = new Properties();
props1.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
Context ctx1 = new InitialContext(props1);
// ... lookup et test

// Test Prêt
Properties props2 = new Properties();
props2.put(Context.PROVIDER_URL, "http-remoting://localhost:8180");
Context ctx2 = new InitialContext(props2);
// ... lookup et test
```

---

## 🐛 Troubleshooting

### ❌ "Address already in use: bind /127.0.0.1:8080"

**Cause :** Une instance WildFly est déjà sur le port 8080

**Solution 1 : Arrêter l'instance existante**
```bash
# Trouver le processus
netstat -ano | findstr :8080

# Tuer le processus (PID trouvé)
taskkill /PID <PID> /F
```

**Solution 2 : Utiliser l'offset de port**
```bash
standalone.bat -Djboss.socket.binding.port-offset=100
```

---

### ❌ "Could not connect to remote+http://localhost:8180"

**Causes possibles :**
1. Instance Prêt non démarrée
2. Offset de port incorrect
3. Firewall bloque la connexion

**Solution :**
```bash
# Vérifier que l'instance tourne
netstat -ano | findstr :8180

# Vérifier les logs
type %WILDFLY_HOME%\standalone-pret\log\server.log
```

---

### ❌ "DataSource 'PretDS' not found"

**Cause :** DataSource non déployée sur la bonne instance

**Solution :**
```bash
# Copier vers la bonne instance
copy pret-ds.xml %WILDFLY_HOME%\standalone-pret\deployments\

# Vérifier le déploiement
dir %WILDFLY_HOME%\standalone-pret\deployments\pret-ds.xml.deployed
```

---

## 📊 Récapitulatif

### Processus en cours d'exécution

Vous devriez avoir **3 processus Java** (WildFly) actifs :

```
java.exe  -  WildFly (SituationBancaire)  -  Ports: 8080, 9990, 4447
java.exe  -  WildFly (Prêt)               -  Ports: 8180, 10090, 4547
java.exe  -  WildFly (Centralisateur)     -  Ports: 9080, 10190, 4647
```

### Flux de communication

```
1. Utilisateur → http://localhost:9080/centralisateur (Centralisateur)
2. Centralisateur → http-remoting://localhost:8080 (SituationBancaire EJB)
3. Centralisateur → http-remoting://localhost:8180 (Prêt EJB)
4. SituationBancaire → jdbc:postgresql://localhost:5432/situation_bancaire_db
5. Prêt → jdbc:postgresql://localhost:5432/pret_db
```

---

## 🎯 Checklist Finale

- [ ] 3 terminaux ouverts
- [ ] SituationBancaire démarré sur port 8080
- [ ] Prêt démarré sur port 8180
- [ ] Centralisateur démarré sur port 9080
- [ ] 2 bases de données créées (situation_bancaire_db, pret_db)
- [ ] DataSources déployées
- [ ] WARs déployés (fichiers .deployed présents)
- [ ] Aucune erreur dans les logs
- [ ] URLs accessibles

---

**Architecture complète et fonctionnelle ! 🎉**
