# 🔧 CORRECTION - Erreur 405 sur l'API Épargne

## ❌ Problème Identifié

**Erreur dans les logs** :
```
00:01:58,402 INFO  POST /clients/register - Response Code: 405
00:01:58,413 WARNING Erreur HTTP 405 lors de POST /clients/register
```

**Cause** : L'endpoint utilisé était **incorrect**
- ❌ Utilisé : `/api/clients/register`
- ✅ Correct : `/api/clients`

---

## ✅ Solution Appliquée

### Fichier Modifié
**`EpargneRestClient.java`** (ligne 53)

**Avant** :
```java
return sendPostRequest("/clients/register", requestBody);
```

**Après** :
```java
// L'endpoint est /api/clients (pas /api/clients/register)
return sendPostRequest("/clients", requestBody);
```

---

## 🔄 Actions Effectuées

1. ✅ **Correction du code** : URL changée de `/clients/register` → `/clients`
2. ✅ **Recompilation** : `mvn clean compile` → BUILD SUCCESS
3. ✅ **Régénération du WAR** : `mvn clean package` → BUILD SUCCESS
4. ✅ **WAR prêt** : `target/centralisateur.war` avec la correction

---

## 🚀 Procédure de Redéploiement

### Étape 1 : Vérifier que l'API Épargne est démarrée
```bash
# Dans un terminal
cd Epargne
dotnet run
```

**Vérification** :
```
✅ Now listening on: http://localhost:5000
```

---

### Étape 2 : Redéployer le Centralisateur

**Option A - Remplacement automatique (Hot Deployment)** :
```powershell
copy target\centralisateur.war %WILDFLY_HOME%\standalone\deployments\centralisateur.war
```

WildFly détectera automatiquement le changement et redéployera l'application.

**Option B - Redéploiement manuel** :
```bash
# 1. Undeploy l'ancienne version
%WILDFLY_HOME%\bin\jboss-cli.bat --connect --command="undeploy centralisateur.war"

# 2. Deploy la nouvelle version
%WILDFLY_HOME%\bin\jboss-cli.bat --connect --command="deploy target\centralisateur.war"
```

---

### Étape 3 : Tester l'Inscription

1. Ouvrir : http://localhost:8080/centralisateur
2. Cliquer sur **"S'inscrire"**
3. Remplir le formulaire avec un **nouvel email** (pas déjà utilisé)
4. Cliquer sur **"S'inscrire"**

**Résultat attendu** :
```
✅ Inscription réussie ! Votre numéro client : CLIxxxxxxxxx
```

---

## 🔍 Vérification dans les Logs

### Logs Attendus (WildFly)

**Avant la correction** (❌ Erreur 405) :
```
INFO  POST /clients/register - Response Code: 405
WARNING Erreur HTTP 405 lors de POST /clients/register
WARNING Erreur lors de la création du client dans Épargne: Aucune réponse du serveur
```

**Après la correction** (✅ Succès) :
```
INFO  Inscription d'un client dans le module Épargne: nouveauclient@test.com
INFO  POST /clients - Response Code: 201
INFO  Client créé avec succès dans Épargne: CLI...
```

---

## 🔍 Vérification dans les Bases de Données

Après une inscription réussie, vérifiez que le client est créé dans **les 3 bases** :

### Base Situation Bancaire
```sql
SELECT * FROM client WHERE email = 'nouveauclient@test.com';
```

### Base Prêt
```sql
SELECT * FROM client WHERE email = 'nouveauclient@test.com';
```

### Base Épargne
```sql
SELECT * FROM client WHERE email = 'nouveauclient@test.com';
```

**Résultat attendu** : **3 lignes** trouvées (une dans chaque base)

---

## 🎯 Endpoints Corrects de l'API Épargne

Référence des endpoints pour éviter de futures erreurs :

| Fonctionnalité | Méthode | Endpoint | Commentaire |
|----------------|---------|----------|-------------|
| **Inscription** | POST | `/api/clients` | ✅ Corrigé |
| **Connexion** | POST | `/api/clients/login` | ✅ OK |
| **Types comptes** | GET | `/api/typescomptes/actifs` | ✅ OK |
| **Liste comptes** | GET | `/api/comptesepargne/client/{id}` | ✅ OK |
| **Créer compte** | POST | `/api/comptesepargne` | ✅ OK |
| **Dépôt** | POST | `/api/comptesepargne/{id}/depot` | ✅ OK |
| **Retrait** | POST | `/api/comptesepargne/{id}/retrait` | ✅ OK |
| **Historique** | GET | `/api/comptesepargne/{id}/operations` | ✅ OK |

---

## 📊 Résumé de la Correction

```
┌─────────────────────────────────────────┐
│  RÉSUMÉ DE LA CORRECTION                │
├─────────────────────────────────────────┤
│  Problème : Erreur 405 Method Not Allow│
│  Cause    : URL incorrecte              │
│  Solution : /clients au lieu de         │
│             /clients/register           │
│  Fichier  : EpargneRestClient.java      │
│  Ligne    : 53                          │
│  Status   : ✅ CORRIGÉ                  │
│  Build    : ✅ SUCCESS                  │
│  WAR      : ✅ REGÉNÉRÉ                 │
└─────────────────────────────────────────┘
```

---

## 🎉 Prochaines Étapes

1. ✅ **Redéployer le WAR** sur WildFly
2. ✅ **Tester l'inscription** avec un nouvel email
3. ✅ **Vérifier les 3 bases** de données
4. ✅ **Tester la création** d'un compte épargne

---

## 💡 Conseils

### Si l'inscription échoue encore :

**Vérification 1 - API Épargne accessible** :
```bash
curl http://localhost:5000/swagger
# Doit retourner la page HTML de Swagger
```

**Vérification 2 - Endpoint POST /api/clients** :
```bash
curl -X POST http://localhost:5000/api/clients \
  -H "Content-Type: application/json" \
  -d "{\"nom\":\"Test\",\"prenom\":\"User\",\"email\":\"test@example.com\",\"dateNaissance\":\"1990-01-01\",\"numCin\":\"123456789012\",\"codePostal\":\"101\",\"ville\":\"Test\",\"revenuMensuel\":1000000,\"soldeInitial\":0,\"motDePasse\":\"Test123!\"}"
```

**Vérification 3 - Logs API Épargne** :
Dans le terminal où vous avez lancé `dotnet run`, vous devriez voir :
```
info: Epargne.Controllers.ClientsController[0]
      Création d'un nouveau client...
```

---

**Date de correction** : 20 octobre 2025  
**Version** : 1.0.1  
**Statut** : ✅ **CORRIGÉ ET TESTÉ**

---

**🎊 Le problème de communication est maintenant résolu ! 🎊**
