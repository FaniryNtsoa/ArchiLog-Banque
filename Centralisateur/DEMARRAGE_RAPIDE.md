# 🚀 Guide de Démarrage Rapide - Module Épargne Intégré

## 📋 Prérequis

Avant de commencer, assurez-vous d'avoir :

- ✅ **Java 17** installé
- ✅ **Maven 3.9+** installé
- ✅ **.NET 9.0 SDK** installé
- ✅ **PostgreSQL** en cours d'exécution
- ✅ **WildFly 31+** installé et configuré

---

## ⚡ Démarrage en 5 Étapes

### Étape 1 : Démarrer l'API Épargne (.NET)

```bash
cd Epargne
dotnet run
```

**Vérification** : Le message suivant doit apparaître :
```
Now listening on: http://localhost:5000
Application started. Press Ctrl+C to shut down.
```

**Test rapide** :
```bash
# Dans un autre terminal
curl http://localhost:5000/swagger
# Doit retourner la page HTML de Swagger
```

---

### Étape 2 : Compiler le Centralisateur

```bash
cd Centralisateur
mvn clean package
```

**Vérification** :
```
[INFO] BUILD SUCCESS
[INFO] Building war: ...\target\centralisateur.war
```

---

### Étape 3 : Déployer sur WildFly

**Option A - Copie manuelle** (plus simple) :
```powershell
copy target\centralisateur.war %WILDFLY_HOME%\standalone\deployments\
```

**Option B - CLI WildFly** :
```bash
%WILDFLY_HOME%\bin\jboss-cli.bat --connect --command="deploy --force target\centralisateur.war"
```

**Option C - Script batch** :
```bash
deployer-tous-modules.bat
```

**Vérification** : Dans les logs WildFly (`standalone/log/server.log`) :
```
INFO  [org.wildfly.extension.undertow] WFLYUT0021: Registered web context: '/centralisateur'
INFO  [org.jboss.as.server] WFLYSRV0010: Deployed "centralisateur.war"
```

---

### Étape 4 : Tester l'Inscription Multi-Module

1. Ouvrir : **http://localhost:8080/centralisateur**
2. Cliquer sur **"S'inscrire"**
3. Remplir le formulaire :
   ```
   Nom: Dupont
   Prénom: Jean
   Email: jean.dupont@test.com
   Mot de passe: Password123!
   (+ autres champs obligatoires)
   ```
4. Cliquer sur **"S'inscrire"**

**Résultat attendu** :
```
✅ Inscription réussie ! Votre numéro client : CLIxxxxxxxxx
```

**Vérification dans les bases** :
```sql
-- Base Situation Bancaire
SELECT * FROM client WHERE email = 'jean.dupont@test.com';

-- Base Prêt
SELECT * FROM client WHERE email = 'jean.dupont@test.com';

-- Base Épargne
SELECT * FROM client WHERE email = 'jean.dupont@test.com';
```

**Les 3 requêtes doivent retourner un résultat** ✅

---

### Étape 5 : Tester le Module Épargne

1. **Se connecter** avec le compte créé
2. Dans le **sidebar**, repérer la section **💰 Épargne**
3. Cliquer sur **"✨ Ouvrir un compte"**
4. Sélectionner **"Livret A"**
5. Saisir **10 000 MGA** comme dépôt initial
6. Cliquer sur **"Ouvrir le compte"**

**Résultat attendu** :
```
✅ Compte épargne créé avec succès ! Numéro de compte : CEP1234567890
```

7. Cliquer sur **"📂 Mes comptes épargne"**

**Résultat attendu** : Votre compte Livret A s'affiche avec :
- Numéro : CEP1234567890
- Solde : 10 000,00 MGA
- Type : Livret A
- Taux : 3.00%
- Statut : ✅ ACTIF

---

## 🎯 Tests Rapides des Fonctionnalités

### Test 1 : Dépôt

1. **💵 Déposer** → Sélectionner le compte → Saisir **50 000 MGA**
2. ✅ Message : "Dépôt de 50 000,00 MGA effectué avec succès ! Nouveau solde : 60 000,00 MGA"

### Test 2 : Retrait

1. **💸 Retirer** → Sélectionner le compte → Saisir **20 000 MGA**
2. ✅ Message : "Retrait de 20 000,00 MGA effectué avec succès ! Nouveau solde : 40 000,00 MGA"

### Test 3 : Historique

1. **📜 Historique épargne** → Sélectionner le compte
2. ✅ 3 opérations affichées :
   - 📥 OUVERTURE : +10 000,00 MGA
   - 📥 DEPOT : +50 000,00 MGA
   - 📤 RETRAIT : -20 000,00 MGA

---

## 🛠️ Dépannage Express

### Problème : "Connection refused to localhost:5000"

**Cause** : API Épargne non démarrée  
**Solution** :
```bash
cd Epargne
dotnet run
```

---

### Problème : "Template not found: epargne/comptes-epargne"

**Cause** : Templates non inclus dans le WAR  
**Solution** :
```bash
cd Centralisateur
mvn clean package
# Redéployer le WAR
```

---

### Problème : "Error 404" sur /epargne/comptes

**Cause** : Servlets non chargés  
**Solution** : Vérifier les logs WildFly pour les erreurs de déploiement

---

### Problème : Inscription réussit mais pas de client dans Épargne

**Cause** : API Épargne arrêtée pendant l'inscription  
**Solution** : C'est normal ! L'inscription est **non-bloquante** pour Épargne.
- Le client est créé dans SituationBancaire et Prêt
- Un warning est loggé : "Erreur Épargne (non bloquant)"
- Relancer l'API Épargne et créer un nouveau compte pour tester

---

## 📊 Architecture Simplifiée

```
┌──────────────┐         ┌──────────────┐
│  Navigateur  │────────▶│Centralisateur│
│              │         │   (Java)     │
└──────────────┘         └──────┬───────┘
                                │
                    ┌───────────┼───────────┐
                    │           │           │
                    ▼           ▼           ▼
              ┌─────────┐ ┌─────────┐ ┌─────────┐
              │Situation│ │  Prêt   │ │ Épargne │
              │Bancaire │ │ (Java)  │ │ (.NET)  │
              │ (Java)  │ │   EJB   │ │  REST   │
              │   EJB   │ └─────────┘ └─────────┘
              └─────────┘
```

---

## 🎓 Fonctionnalités Disponibles

### Types de Comptes Épargne

| Type | Taux | Plafond | Dépôt Min | Solde Min |
|------|------|---------|-----------|-----------|
| **Livret A** | 3% | 22 950 € | 10 MGA | 0 MGA |
| **CEL** | 2% | 15 300 € | 300 MGA | 300 MGA |
| **LDD** | 3% | 12 000 € | 15 MGA | 15 MGA |
| **PEL** | 2.25% | 61 200 € | 225 MGA | 225 MGA |

### Opérations Disponibles

- ✨ **Ouvrir un compte** : Créer un nouveau compte épargne
- 📂 **Mes comptes épargne** : Consulter tous vos comptes
- 💵 **Déposer** : Ajouter de l'argent sur un compte
- 💸 **Retirer** : Retirer de l'argent (avec restrictions)
- 📜 **Historique épargne** : Voir toutes les opérations

---

## 📚 Documentation Complète

Pour plus de détails, consultez :

- **INTEGRATION_EPARGNE.md** : Guide d'intégration complet
- **RECAP_INTEGRATION_EPARGNE.md** : Récapitulatif de l'intégration
- **Epargne/README.md** : Documentation du module Épargne
- **Epargne/GUIDE_DEMARRAGE.md** : Guide détaillé de l'API Épargne

---

## ✅ Checklist de Vérification

Avant de commencer à utiliser le module Épargne :

- [ ] PostgreSQL démarré
- [ ] API Épargne démarrée (http://localhost:5000)
- [ ] Centralisateur déployé sur WildFly
- [ ] Modules SituationBancaire et Prêt déployés (pour inscription complète)
- [ ] Inscription testée et client créé dans les 3 bases
- [ ] Section Épargne visible dans le sidebar
- [ ] Création de compte testée
- [ ] Dépôt testé
- [ ] Retrait testé
- [ ] Historique testé

---

## 🎉 Félicitations !

Si vous avez suivi toutes les étapes avec succès, votre **Centralisateur** est maintenant :

✅ **Connecté aux 3 modules** (SituationBancaire, Prêt, Épargne)  
✅ **Capable de gérer des comptes épargne**  
✅ **Fonctionnel et opérationnel**  

Vous pouvez maintenant gérer l'ensemble des services bancaires depuis une seule interface ! 🏦

---

**Besoin d'aide ?** Consultez la section **Dépannage** dans `INTEGRATION_EPARGNE.md`

**Date** : 19 janvier 2025  
**Version** : 1.0.0
