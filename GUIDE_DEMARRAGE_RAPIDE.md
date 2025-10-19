# 🚀 Guide de Démarrage Rapide - Système Multi-Modules

**Date :** 19 octobre 2025  
**Objectif :** Démarrer les 3 modules simultanément

---

## ⚡ Démarrage Rapide (5 minutes)

### Étape 1 : Configuration Initiale (Une seule fois)

```bash
# Définir la variable d'environnement
set WILDFLY_HOME=C:\Users\fanir\Documents\utils\wildfly-37.0.1.Final\wildfly-37.0.1.Final

# Créer les instances WildFly
cd c:\Users\fanir\Documents\ITU\Faniry\S5\ArchitectureLogiciel\banque
.\setup-wildfly-instances.bat
```

**Résultat attendu :**
```
✅ Instance standalone créée (SituationBancaire - port 8080)
✅ Instance standalone-pret créée (Prêt - port 8180)
✅ Instance standalone-centralisateur créée (Centralisateur - port 9080)
```

---

### Étape 2 : Déployer les Modules

**Terminal PowerShell :**
```powershell
cd c:\Users\fanir\Documents\ITU\Faniry\S5\ArchitectureLogiciel\banque

# Déployer SituationBancaire
copy SituationBancaire\target\situation-bancaire.war $env:WILDFLY_HOME\standalone\deployments\

# Déployer Prêt
copy Pret\target\pret.war $env:WILDFLY_HOME\standalone-pret\deployments\
copy Pret\src\main\webapp\WEB-INF\pret-ds.xml $env:WILDFLY_HOME\standalone-pret\deployments\

# Déployer Centralisateur
copy Centralisateur\target\centralisateur.war $env:WILDFLY_HOME\standalone-centralisateur\deployments\
```

---

### Étape 3 : Démarrer les 3 Instances (3 Terminaux)

#### 🟢 Terminal 1 : SituationBancaire (Port 8080)

```bash
cd c:\Users\fanir\Documents\ITU\Faniry\S5\ArchitectureLogiciel\banque\SituationBancaire
.\start-wildfly-situation.bat
```

**Attendre le message :**
```
✅ WFLYSRV0025: WildFly 37.0.1.Final (WildFly Core 25.0.0.Final) started in XXXXms
```

---

#### 🔵 Terminal 2 : Prêt (Port 8180)

```bash
cd c:\Users\fanir\Documents\ITU\Faniry\S5\ArchitectureLogiciel\banque\Pret
.\start-wildfly-pret.bat
```

**Attendre le message :**
```
✅ WFLYSRV0025: WildFly 37.0.1.Final (WildFly Core 25.0.0.Final) started in XXXXms
✅ WFLYSRV0010: Deployed "pret.war"
```

---

#### 🟣 Terminal 3 : Centralisateur (Port 9080)

```bash
cd c:\Users\fanir\Documents\ITU\Faniry\S5\ArchitectureLogiciel\banque\Centralisateur
.\start-wildfly-centralisateur.bat
```

**Attendre le message :**
```
✅ WFLYSRV0025: WildFly 37.0.1.Final (WildFly Core 25.0.0.Final) started in XXXXms
✅ WFLYSRV0010: Deployed "centralisateur.war"
```

---

### Étape 4 : Vérification

**Ouvrir dans le navigateur :**

| Module | URL | Statut |
|--------|-----|--------|
| SituationBancaire | http://localhost:8080 | ✅ Page d'accueil |
| Prêt | http://localhost:8180 | ✅ Page d'accueil |
| **Centralisateur** | **http://localhost:9080/centralisateur** | ✅ **Application complète** |

---

## 🔧 Architecture des Ports

```
┌────────────────────────────────────────────┐
│  CENTRALISATEUR (Port 9080)                │
│  └─ Communique avec:                       │
│     ├─ SituationBancaire via EJB (8080)    │
│     └─ Prêt via EJB (8180)                 │
└────────────────────────────────────────────┘
         │                      │
         ▼                      ▼
┌─────────────────┐    ┌─────────────────┐
│ SITUATION       │    │ PRET            │
│ BANCAIRE        │    │                 │
│ Port: 8080      │    │ Port: 8180      │
│ Remoting: 4447  │    │ Remoting: 4547  │
└────────┬────────┘    └────────┬────────┘
         │                      │
         ▼                      ▼
┌─────────────────┐    ┌─────────────────┐
│ PostgreSQL      │    │ PostgreSQL      │
│ situation_      │    │ pret_db         │
│ bancaire_db     │    │ Port: 5432      │
│ Port: 5432      │    │                 │
└─────────────────┘    └─────────────────┘
```

---

## 📝 Explications Détaillées

### Pourquoi 3 instances WildFly séparées ?

**Problème résolu :**
- ❌ **Avant :** Un seul WildFly, port 8080 utilisé → Conflit
- ✅ **Après :** 3 instances, chacune sur son port → Aucun conflit

**Avantages :**
1. **Isolation** : Chaque module a son propre environnement
2. **Logs séparés** : Facile à debugger
3. **Déploiement indépendant** : Redémarrer un module sans affecter les autres
4. **Configuration distincte** : DataSources, ports, ressources différents

---

### Comment fonctionne l'offset de port ?

**Offset = +100 pour Prêt :**
```
Port HTTP :       8080 + 100 = 8180
Port Management : 9990 + 100 = 10090
Port Remoting :   4447 + 100 = 4547
Port AJP :        8009 + 100 = 8109
```

**Offset = +1000 pour Centralisateur :**
```
Port HTTP :       8080 + 1000 = 9080
Port Management : 9990 + 1000 = 10190
Port Remoting :   4447 + 1000 = 4647
```

**Command Line :**
```bash
standalone.bat -Djboss.socket.binding.port-offset=100
```

---

### Comment le Centralisateur communique avec les 2 modules ?

**Fichier : `jboss-ejb-client.properties`**
```properties
# Deux connexions configurées
remote.connections=situation,pret

# Connexion 1 : SituationBancaire
remote.connection.situation.host=localhost
remote.connection.situation.port=8080

# Connexion 2 : Prêt
remote.connection.pret.host=localhost
remote.connection.pret.port=8180
```

**Dans le code Java (EJBClientFactory) :**
```java
// Pour appeler SituationBancaire
Properties props = new Properties();
props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
ClientServiceRemote clientService = lookup(...);

// Pour appeler Prêt
Properties props2 = new Properties();
props2.put(Context.PROVIDER_URL, "http-remoting://localhost:8180");
PretServiceRemote pretService = lookup(...);
```

---

### Structure des répertoires WildFly

```
C:\wildfly-37.0.1.Final\
│
├── bin\
│   └── standalone.bat                      # Script de démarrage
│
├── standalone\                              # Instance 1 : SituationBancaire
│   ├── configuration\standalone.xml
│   ├── deployments\
│   │   ├── situation-bancaire.war
│   │   └── situationbancaire-ds.xml
│   └── log\server.log
│
├── standalone-pret\                         # Instance 2 : Prêt
│   ├── configuration\standalone.xml
│   ├── deployments\
│   │   ├── pret.war
│   │   └── pret-ds.xml
│   └── log\server.log
│
└── standalone-centralisateur\               # Instance 3 : Centralisateur
    ├── configuration\standalone.xml
    ├── deployments\
    │   └── centralisateur.war
    └── log\server.log
```

**Chaque instance a :**
- ✅ Sa propre configuration
- ✅ Ses propres logs
- ✅ Ses propres déploiements
- ✅ Ses propres ports

---

## 🐛 Résolution de Problèmes

### ❌ "Address already in use: bind /127.0.0.1:8080"

**Cause :** Le port 8080 est déjà utilisé

**Solution 1 : Vérifier et tuer le processus**
```powershell
# Trouver le processus
netstat -ano | findstr :8080

# Tuer le processus (remplacer <PID>)
taskkill /PID <PID> /F
```

**Solution 2 : Vérifier qu'aucun autre WildFly ne tourne**
```powershell
# Lister les processus Java
tasklist | findstr java.exe

# Si plusieurs java.exe, les arrêter
# CTRL+C dans chaque terminal WildFly
```

---

### ❌ "Could not connect to http-remoting://localhost:8180"

**Cause :** Instance Prêt non démarrée ou port incorrect

**Vérification :**
```powershell
# 1. Vérifier que l'instance tourne
netstat -ano | findstr :8180

# 2. Vérifier les logs
type %WILDFLY_HOME%\standalone-pret\log\server.log | findstr "Started"

# 3. Vérifier que pret.war est déployé
dir %WILDFLY_HOME%\standalone-pret\deployments\pret.war.deployed
```

**Solution :**
```powershell
# Redémarrer l'instance Prêt
cd Pret
.\start-wildfly-pret.bat
```

---

### ❌ Module déployé mais erreurs dans les logs

**Vérifier les logs de chaque instance :**
```powershell
# SituationBancaire
type %WILDFLY_HOME%\standalone\log\server.log | findstr "ERROR"

# Prêt
type %WILDFLY_HOME%\standalone-pret\log\server.log | findstr "ERROR"

# Centralisateur
type %WILDFLY_HOME%\standalone-centralisateur\log\server.log | findstr "ERROR"
```

**Erreurs courantes :**
- `DataSource not found` → Vérifier que le fichier `-ds.xml` est déployé
- `ClassNotFoundException` → Vérifier les dépendances dans `pom.xml`
- `JNDI lookup failed` → Vérifier le nom JNDI de l'EJB

---

## ✅ Checklist de Démarrage

### Configuration (Une seule fois)
- [ ] Variable `WILDFLY_HOME` définie
- [ ] Script `setup-wildfly-instances.bat` exécuté
- [ ] 3 répertoires créés : `standalone`, `standalone-pret`, `standalone-centralisateur`
- [ ] Bases de données créées : `situation_bancaire_db`, `pret_db`
- [ ] Scripts SQL exécutés

### Compilation
- [ ] `SituationBancaire\target\situation-bancaire.war` existe
- [ ] `Pret\target\pret.war` existe
- [ ] `Centralisateur\target\centralisateur.war` existe

### Déploiement
- [ ] WARs copiés dans les bons répertoires `deployments\`
- [ ] DataSources copiées (situationbancaire-ds.xml, pret-ds.xml)
- [ ] Fichiers `.deployed` présents

### Démarrage
- [ ] Terminal 1 : SituationBancaire démarré (port 8080)
- [ ] Terminal 2 : Prêt démarré (port 8180)
- [ ] Terminal 3 : Centralisateur démarré (port 9080)
- [ ] Message "Started" visible dans les 3 terminaux
- [ ] Aucune erreur dans les logs

### Vérification
- [ ] http://localhost:8080 accessible
- [ ] http://localhost:8180 accessible
- [ ] http://localhost:9080/centralisateur accessible
- [ ] Login fonctionnel sur Centralisateur
- [ ] Opérations bancaires fonctionnelles
- [ ] (À venir) Opérations de prêt fonctionnelles

---

## 🎯 Résumé

**3 Instances WildFly :**
1. **SituationBancaire** → Port 8080 → Gestion des comptes
2. **Prêt** → Port 8180 → Gestion des prêts
3. **Centralisateur** → Port 9080 → Interface utilisateur unifiée

**Communication :**
```
Centralisateur (9080)
    ├─ EJB Remote → SituationBancaire (8080)
    └─ EJB Remote → Prêt (8180)
```

**Pour démarrer :**
```bash
# Terminal 1
.\SituationBancaire\start-wildfly-situation.bat

# Terminal 2
.\Pret\start-wildfly-pret.bat

# Terminal 3
.\Centralisateur\start-wildfly-centralisateur.bat
```

**URL d'accès :**
http://localhost:9080/centralisateur

---

**Tout est prêt ! 🚀**
