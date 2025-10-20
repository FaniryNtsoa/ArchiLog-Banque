# 🚀 DÉPLOIEMENT RAPIDE - Module Épargne Corrigé

## ⚡ Déploiement en 5 Minutes

### Étape 1 : Compilation
```powershell
cd Centralisateur
mvn clean package
```

**Résultat attendu** :
```
[INFO] BUILD SUCCESS
[INFO] Total time: ~35 seconds
[INFO] WAR créé : target/centralisateur.war
```

---

### Étape 2 : Arrêt WildFly (si démarré)
```powershell
# Trouver le processus
Get-Process -Name java | Where-Object { $_.CommandLine -like "*wildfly*" }

# Arrêter proprement
# Dans le terminal WildFly, taper : shutdown
# OU
# Ctrl+C dans le terminal WildFly
```

---

### Étape 3 : Suppression Ancien Déploiement
```powershell
# Supprimer le WAR
Remove-Item "wildfly-29.0.1.Final\standalone\deployments\centralisateur.war" -Force -ErrorAction SilentlyContinue

# Supprimer le dossier déployé
Remove-Item "wildfly-29.0.1.Final\standalone\deployments\centralisateur.war.deployed" -Force -ErrorAction SilentlyContinue

# Supprimer le dossier temporaire
Remove-Item "wildfly-29.0.1.Final\standalone\deployments\centralisateur.war" -Recurse -Force -ErrorAction SilentlyContinue
```

---

### Étape 4 : Copie du Nouveau WAR
```powershell
Copy-Item "Centralisateur\target\centralisateur.war" "wildfly-29.0.1.Final\standalone\deployments\"
```

---

### Étape 5 : Démarrage WildFly
```powershell
cd wildfly-29.0.1.Final\bin
.\standalone.bat
```

**Attendez le message** :
```
WFLYSRV0025: WildFly Full 29.0.1.Final (WildFly Core 21.0.5.Final) started in XXXXms
```

---

### Étape 6 : Vérification Déploiement
```powershell
# Attendre quelques secondes puis vérifier
Get-ChildItem "wildfly-29.0.1.Final\standalone\deployments" -Filter "*.deployed"
```

**Résultat attendu** :
```
centralisateur.war.deployed
```

---

## 🧪 Test Rapide

### 1. Accéder à l'application
```
http://localhost:8081/centralisateur/
```

### 2. Se connecter
- **Email** : client@banque.com
- **Mot de passe** : password123

### 3. Tester Épargne
- Cliquer sur "Épargne" dans le menu
- Cliquer sur "Nouveau Compte"
- **✅ Vérifier** : Liste des types de compte s'affiche SANS erreur 500

### 4. Test Complet
- "Mes Comptes" → Vérifier liste comptes
- "Dépôt" → Vérifier formulaire
- "Retrait" → Vérifier formulaire
- "Historique" → Vérifier opérations

---

## 🔍 Vérification des Logs

### Pas d'erreurs critiques
```powershell
# Vérifier aucun NullPointerException
Select-String -Path "wildfly-29.0.1.Final\standalone\log\server.log" -Pattern "NullPointerException" | Select-Object -Last 10
```

**Résultat attendu** : Aucune ligne récente (après le déploiement)

### Logs de déploiement réussi
```powershell
Select-String -Path "wildfly-29.0.1.Final\standalone\log\server.log" -Pattern "centralisateur.war.*deployed" | Select-Object -Last 1
```

**Résultat attendu** :
```
INFO  [org.jboss.as.server] WFLYSRV0010: Deployed "centralisateur.war"
```

---

## 📋 Checklist Post-Déploiement

### Vérifications Techniques
- [ ] BUILD SUCCESS pour `mvn clean package`
- [ ] Fichier `centralisateur.war` créé (taille ~50 MB)
- [ ] Fichier `.deployed` présent dans deployments/
- [ ] Aucun fichier `.failed` dans deployments/
- [ ] WildFly démarré sans erreur
- [ ] Port 8081 accessible

### Vérifications Fonctionnelles
- [ ] Page d'accueil charge
- [ ] Login fonctionne
- [ ] Menu Épargne visible
- [ ] Page "Nouveau Compte" affiche types sans erreur
- [ ] Page "Mes Comptes" affiche liste sans erreur
- [ ] Page "Dépôt" affiche formulaire sans erreur
- [ ] Page "Retrait" affiche formulaire sans erreur
- [ ] Page "Historique" affiche opérations sans erreur

---

## 🐛 Résolution Problèmes Courants

### Problème 1 : WAR ne se déploie pas
**Symptôme** : Pas de fichier `.deployed`, ou fichier `.failed` présent

**Solution** :
```powershell
# Vérifier les logs
Get-Content "wildfly-29.0.1.Final\standalone\log\server.log" -Tail 50

# Supprimer et recommencer
Remove-Item "wildfly-29.0.1.Final\standalone\deployments\centralisateur.war*" -Force
Copy-Item "Centralisateur\target\centralisateur.war" "wildfly-29.0.1.Final\standalone\deployments\"
```

---

### Problème 2 : Erreur 500 sur pages Épargne
**Symptôme** : Pages blanches avec "HTTP 500 - Internal Server Error"

**Solution** :
1. Vérifier JsonHelper présent :
   ```powershell
   Test-Path "Centralisateur\src\main\java\com\banque\centralisateur\util\JsonHelper.java"
   ```

2. Vérifier compilation inclut JsonHelper :
   ```powershell
   Test-Path "Centralisateur\target\classes\com\banque\centralisateur\util\JsonHelper.class"
   ```

3. Recompiler si nécessaire :
   ```powershell
   cd Centralisateur
   mvn clean package
   ```

---

### Problème 3 : API Épargne non accessible
**Symptôme** : Listes vides, messages "Service indisponible"

**Solution** :
1. Vérifier API Épargne démarrée :
   ```powershell
   Test-NetConnection -ComputerName localhost -Port 8083
   ```

2. Démarrer si nécessaire :
   ```powershell
   cd Pret
   .\start-wildfly-pret.bat
   ```

3. Tester API directement :
   ```powershell
   curl http://localhost:8083/api/epargne/types-compte
   ```

---

### Problème 4 : Dépendances manquantes
**Symptôme** : ClassNotFoundException dans les logs

**Solution** :
```powershell
cd Centralisateur
mvn clean install
mvn dependency:tree
```

---

## 🔄 Redéploiement Rapide

### Script PowerShell (copier-coller)
```powershell
# Arrêter WildFly (Ctrl+C dans son terminal)
Start-Sleep -Seconds 3

# Recompiler
cd Centralisateur
mvn clean package

# Redéployer
Remove-Item "..\wildfly-29.0.1.Final\standalone\deployments\centralisateur.war*" -Force -ErrorAction SilentlyContinue
Copy-Item "target\centralisateur.war" "..\wildfly-29.0.1.Final\standalone\deployments\"

# Redémarrer WildFly
cd ..\wildfly-29.0.1.Final\bin
Start-Process -FilePath ".\standalone.bat" -NoNewWindow

Write-Host "✅ Redéploiement terminé ! Attendre 30 secondes avant de tester." -ForegroundColor Green
```

---

## 📦 Backup Avant Déploiement

### Créer une sauvegarde
```powershell
# Créer dossier backup si inexistant
New-Item -Path "backups" -ItemType Directory -Force

# Sauvegarder WAR actuel
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
Copy-Item "wildfly-29.0.1.Final\standalone\deployments\centralisateur.war" "backups\centralisateur_$timestamp.war" -ErrorAction SilentlyContinue
```

### Restaurer une sauvegarde
```powershell
# Lister les backups
Get-ChildItem "backups\centralisateur_*.war" | Sort-Object LastWriteTime -Descending

# Restaurer le dernier backup
$lastBackup = Get-ChildItem "backups\centralisateur_*.war" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
Copy-Item $lastBackup.FullName "wildfly-29.0.1.Final\standalone\deployments\centralisateur.war" -Force
```

---

## ✅ Validation Finale

Après déploiement, tester cette séquence complète :

1. ✅ Login : `http://localhost:8081/centralisateur/login`
2. ✅ Dashboard : `http://localhost:8081/centralisateur/dashboard`
3. ✅ Nouveau compte épargne : `http://localhost:8081/centralisateur/epargne/nouveau`
4. ✅ Mes comptes épargne : `http://localhost:8081/centralisateur/epargne/comptes`
5. ✅ Dépôt : `http://localhost:8081/centralisateur/epargne/depot`
6. ✅ Retrait : `http://localhost:8081/centralisateur/epargne/retrait`
7. ✅ Historique : `http://localhost:8081/centralisateur/epargne/historique`

**Si tous les tests passent : 🎉 DÉPLOIEMENT RÉUSSI !**

---

## 📞 Support

En cas de problème persistant :

1. Consulter `CORRECTION_COMPLETE_EPARGNE.md` pour les détails techniques
2. Consulter `TESTS_EPARGNE.md` pour les tests détaillés
3. Vérifier les logs WildFly : `wildfly-29.0.1.Final\standalone\log\server.log`
4. Vérifier les logs API : Pret/logs/ et SituationBancaire/logs/

---

**Dernière mise à jour** : 20 Décembre 2024  
**Version** : 1.0.0  
**Status** : ✅ PRODUCTION READY
