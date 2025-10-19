# 🎓 Explication : Pourquoi 3 Instances WildFly ?

**Date :** 19 octobre 2025  
**Question :** Pourquoi avons-nous besoin de 3 instances WildFly séparées ?

---

## 🤔 Le Problème Initial

### Erreur Rencontrée
```
WFLYCTL0186: Services which failed to start:
  service org.wildfly.undertow.server.listener.default-server.default: 
  Address already in use: bind /127.0.0.1:8080
```

### Cause
Vous essayiez de déployer **deux modules** (SituationBancaire ET Prêt) sur le **même WildFly** utilisant le **même port 8080**.

**Analogie :** C'est comme essayer de brancher deux appareils sur la même prise électrique en même temps → ❌ Conflit !

---

## ✅ La Solution : 3 Instances Séparées

### Architecture Adoptée

```
┌──────────────────────────────────────────────────────┐
│                   UTILISATEUR                         │
│              (Navigateur Web)                         │
└────────────────────────┬─────────────────────────────┘
                         │
                         │ Accès via http://localhost:9080/centralisateur
                         ▼
┌──────────────────────────────────────────────────────┐
│              CENTRALISATEUR                           │
│              WildFly Instance #3                      │
│              Port 9080 (offset +1000)                │
│  ┌────────────────────────────────────────────────┐  │
│  │  Interface Web (Servlets + Thymeleaf)         │  │
│  │  - Login/Register                              │  │
│  │  - Dashboard                                   │  │
│  │  - Opérations bancaires                        │  │
│  │  - Opérations de prêt                          │  │
│  └────────────────────────────────────────────────┘  │
│                      │                                │
│  ┌────────────────────────────────────────────────┐  │
│  │  EJB Client (Communication)                    │  │
│  │  - Lookup EJB Remote SituationBancaire (8080)│  │
│  │  - Lookup EJB Remote Prêt (8180)              │  │
│  └────────────────────────────────────────────────┘  │
└────────────┬──────────────────────┬──────────────────┘
             │                      │
             │ EJB Remote           │ EJB Remote
             │ Port 8080            │ Port 8180
             ▼                      ▼
┌──────────────────────┐    ┌──────────────────────┐
│ SITUATION BANCAIRE   │    │ PRET                 │
│ WildFly Instance #1  │    │ WildFly Instance #2  │
│ Port 8080            │    │ Port 8180            │
│ (offset 0)           │    │ (offset +100)        │
│  ┌────────────────┐  │    │  ┌────────────────┐  │
│  │ EJB Services   │  │    │  │ EJB Services   │  │
│  │ @Remote        │  │    │  │ @Remote        │  │
│  │ - Client       │  │    │  │ - Pret         │  │
│  │ - Compte       │  │    │  │ - Echeance     │  │
│  │ - Mouvement    │  │    │  │ - TypePret     │  │
│  └────────────────┘  │    │  └────────────────┘  │
│         │             │    │         │             │
│         │ JPA         │    │         │ JPA         │
│         ▼             │    │         ▼             │
│  ┌────────────────┐  │    │  ┌────────────────┐  │
│  │ Entities       │  │    │  │ Entities       │  │
│  │ Repositories   │  │    │  │ Repositories   │  │
│  └────────────────┘  │    │  └────────────────┘  │
└──────────┬───────────┘    └──────────┬───────────┘
           │                            │
           │ JDBC                       │ JDBC
           │ Port 5432                  │ Port 5432
           ▼                            ▼
┌──────────────────────┐    ┌──────────────────────┐
│ PostgreSQL           │    │ PostgreSQL           │
│ situation_bancaire_db│    │ pret_db              │
└──────────────────────┘    └──────────────────────┘
```

---

## 🎯 Pourquoi Cette Architecture ?

### 1. **Séparation des Responsabilités**

| Instance | Rôle | Responsabilité |
|----------|------|----------------|
| **SituationBancaire (8080)** | Métier | Gestion des comptes, mouvements, clients |
| **Prêt (8180)** | Métier | Gestion des prêts, échéances, remboursements |
| **Centralisateur (9080)** | Présentation | Interface utilisateur unifiée |

**Principe :** Chaque instance fait **une seule chose** et la fait **bien**.

---

### 2. **Éviter les Conflits de Ports**

**Sans séparation (❌ Ne fonctionne pas) :**
```
WildFly (port 8080)
├─ situation-bancaire.war  ← Utilise le port 8080
└─ pret.war                ← Veut aussi le port 8080 ❌ CONFLIT !
```

**Avec séparation (✅ Fonctionne) :**
```
WildFly #1 (port 8080)
└─ situation-bancaire.war  ← Utilise le port 8080 ✅

WildFly #2 (port 8180)
└─ pret.war                ← Utilise le port 8180 ✅

WildFly #3 (port 9080)
└─ centralisateur.war      ← Utilise le port 9080 ✅
```

**Résultat :** Aucun conflit, chacun sur son port !

---

### 3. **Isolation des Environnements**

Chaque instance WildFly a :
- ✅ **Son propre fichier de log** : Facile de débugger
- ✅ **Sa propre configuration** : DataSource distincte
- ✅ **Ses propres déploiements** : Indépendants
- ✅ **Son propre cycle de vie** : Redémarrage sans impact

**Exemple :**
```
# Redémarrer Prêt sans toucher SituationBancaire
CTRL+C dans Terminal #2 (Prêt)
.\start-wildfly-pret.bat

# SituationBancaire continue de fonctionner ! ✅
```

---

### 4. **Scalabilité Future**

Si demain vous voulez ajouter un **nouveau module** (exemple : "Assurance"), vous pouvez :

```
WildFly #4 (port 8280)
└─ assurance.war           ← Nouveau module indépendant
```

Vous n'avez **rien à changer** aux autres instances !

---

## 🔧 Comment Ça Fonctionne Techniquement ?

### Port Offset

**Concept :** Ajouter un nombre à **tous** les ports d'une instance WildFly.

**Exemple pour Prêt (offset +100) :**
```
Port par défaut → Port avec offset +100
────────────────────────────────────────
8080 (HTTP)     → 8180
9990 (Management) → 10090
4447 (Remoting) → 4547
8009 (AJP)      → 8109
```

**Commande :**
```bash
standalone.bat -Djboss.socket.binding.port-offset=100
```

**Avantage :** Une seule option change **tous** les ports automatiquement !

---

### Répertoires Séparés

**Commande pour créer une nouvelle instance :**
```bash
xcopy /E /I "%WILDFLY_HOME%\standalone" "%WILDFLY_HOME%\standalone-pret"
```

**Résultat :**
```
C:\wildfly\
├── standalone\              ← Instance originale (SituationBancaire)
├── standalone-pret\         ← Copie pour Prêt
└── standalone-centralisateur\ ← Copie pour Centralisateur
```

Chaque répertoire contient :
```
standalone-pret\
├── configuration\
│   └── standalone.xml       ← Configuration indépendante
├── deployments\
│   └── pret.war             ← Déploiement indépendant
└── log\
    └── server.log           ← Logs indépendants
```

---

### Communication EJB Remote

**Question :** Comment le Centralisateur communique avec les 2 modules métier ?

**Réponse :** Via **EJB Remote** (appels réseau).

#### Configuration (`jboss-ejb-client.properties`)

```properties
# Deux connexions configurées
remote.connections=situation,pret

# Connexion 1 : SituationBancaire sur port 8080
remote.connection.situation.host=localhost
remote.connection.situation.port=8080

# Connexion 2 : Prêt sur port 8180
remote.connection.pret.host=localhost
remote.connection.pret.port=8180
```

#### Code Java (Exemple)

```java
// Appeler SituationBancaire (port 8080)
Properties props1 = new Properties();
props1.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
Context ctx1 = new InitialContext(props1);
ClientServiceRemote clientService = (ClientServiceRemote) ctx1.lookup(
    "ejb:/situation-bancaire/ClientServiceImpl!" +
    "com.banque.situationbancaire.ejb.remote.ClientServiceRemote"
);

// Utiliser le service
ClientDTO client = clientService.getClientById(1L);

// Appeler Prêt (port 8180)
Properties props2 = new Properties();
props2.put(Context.PROVIDER_URL, "http-remoting://localhost:8180");
Context ctx2 = new InitialContext(props2);
PretServiceRemote pretService = (PretServiceRemote) ctx2.lookup(
    "ejb:/pret/PretServiceImpl!" +
    "com.banque.pret.ejb.remote.PretServiceRemote"
);

// Utiliser le service
List<PretDTO> prets = pretService.listerPretsParClient(1L);
```

**Flux de communication :**
```
1. Utilisateur clique sur "Mes Prêts" dans le Centralisateur
2. Centralisateur → Appel EJB Remote vers localhost:8180
3. Module Prêt traite la requête
4. Prêt renvoie les données au Centralisateur
5. Centralisateur affiche les résultats dans Thymeleaf
```

---

## 🎓 Analogie Simple

Imaginez une **banque physique** :

```
┌─────────────────────────────────────────┐
│      AGENCE (Centralisateur)            │
│      Accueil Client                     │
│  ┌───────────────────────────────────┐  │
│  │  Guichet Unique                   │  │
│  │  - Reçoit tous les clients        │  │
│  │  - Redirige vers les services     │  │
│  └───────────────────────────────────┘  │
└─────────┬──────────────────┬────────────┘
          │                  │
          │ Téléphone        │ Téléphone
          ▼                  ▼
┌─────────────────┐  ┌─────────────────┐
│ SERVICE COMPTES │  │ SERVICE PRETS   │
│ (Bureau séparé) │  │ (Bureau séparé) │
│                 │  │                 │
│ - Dépôts        │  │ - Simulations   │
│ - Retraits      │  │ - Demandes      │
│ - Virements     │  │ - Remboursements│
└─────────────────┘  └─────────────────┘
```

- **Guichet (Centralisateur)** : Reçoit les clients, ne fait pas le travail lui-même
- **Service Comptes (SituationBancaire)** : Gère uniquement les comptes
- **Service Prêts (Prêt)** : Gère uniquement les prêts
- **Communication** : Le guichet appelle les services par téléphone (EJB Remote)

**Avantage :** Si le service Prêts est occupé, le service Comptes continue de fonctionner !

---

## 📊 Comparaison

### ❌ Une Seule Instance (Ne Fonctionne Pas)

```
Avantages:
- Simple à démarrer (1 seul terminal)

Inconvénients:
❌ Conflit de ports (8080)
❌ Logs mélangés (difficile à débugger)
❌ Un crash affecte tout
❌ Redémarrage = tout s'arrête
❌ Configuration unique (rigide)
```

### ✅ Trois Instances (Architecture Actuelle)

```
Avantages:
✅ Aucun conflit de ports
✅ Logs séparés (facile à débugger)
✅ Isolation (un crash n'affecte qu'un module)
✅ Redémarrage indépendant
✅ Configuration flexible
✅ Scalable (facile d'ajouter des modules)
✅ Séparation des responsabilités (Clean Architecture)

Inconvénients:
- 3 terminaux à gérer
- Consomme plus de RAM (~1.5 GB par instance)
```

**Verdict :** Les avantages surpassent largement les inconvénients ! ✅

---

## 🚀 En Résumé

### Ce Que Vous Devez Retenir

1. **3 Instances = 3 Serveurs WildFly Indépendants**
   - SituationBancaire (port 8080)
   - Prêt (port 8180)
   - Centralisateur (port 9080)

2. **Chaque Instance a Son Propre Port**
   - Utilisation de `port-offset` pour éviter les conflits

3. **Communication via EJB Remote**
   - Centralisateur appelle les services métier par réseau
   - Configuration dans `jboss-ejb-client.properties`

4. **Isolation Complète**
   - Logs séparés
   - Configurations séparées
   - Déploiements séparés

5. **Démarrage Simple**
   ```bash
   # Terminal 1
   .\SituationBancaire\start-wildfly-situation.bat
   
   # Terminal 2
   .\Pret\start-wildfly-pret.bat
   
   # Terminal 3
   .\Centralisateur\start-wildfly-centralisateur.bat
   ```

6. **Accès Unique**
   - Utilisateur accède uniquement à : http://localhost:9080/centralisateur
   - Le Centralisateur gère tout en arrière-plan

---

## 🎯 Prochaine Étape

Maintenant que vous comprenez l'architecture, suivez le **GUIDE_DEMARRAGE_RAPIDE.md** pour démarrer les 3 instances ! 🚀

---

**Questions ? Consultez ARCHITECTURE_DEPLOIEMENT.md pour plus de détails techniques.**
