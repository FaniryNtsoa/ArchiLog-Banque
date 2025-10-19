# 🧪 TESTS FONCTIONNELS - Module Épargne

## ⚡ Tests Rapides

### Prérequis
- [ ] WildFly démarré sur port 8081 (Centralisateur)
- [ ] API Épargne démarrée sur port 8083
- [ ] API Situation Bancaire démarrée sur port 8082
- [ ] Utilisateur connecté

---

## 1️⃣ Test Nouveau Compte Épargne

**URL** : http://localhost:8081/centralisateur/epargne/nouveau

### Actions
1. Se connecter avec un compte client
2. Naviguer vers "Épargne" → "Nouveau Compte"
3. Vérifier l'affichage de la liste des types

### ✅ Résultat Attendu
```
Types de compte affichés :
- Livret A (Taux: 3.00%, Dépôt min: 10.00 €)
- PEL (Taux: 2.25%, Dépôt min: 225.00 €)
- CEL (Taux: 0.75%, Dépôt min: 300.00 €)
...
```

### ❌ Avant Correction
```
HTTP 500 - NullPointerException
at NouveauCompteEpargneServlet.java:67
```

### Test Complémentaire
- [ ] Sélectionner un type
- [ ] Saisir un montant initial
- [ ] Soumettre le formulaire
- [ ] Vérifier le message de succès

---

## 2️⃣ Test Liste Comptes Épargne

**URL** : http://localhost:8081/centralisateur/epargne/comptes

### Actions
1. Naviguer vers "Épargne" → "Mes Comptes"
2. Vérifier l'affichage de tous les comptes

### ✅ Résultat Attendu
```
Compte             Type        Solde        Statut      Date Ouverture
EP000001          Livret A    1250.00 €    ACTIF      15/10/2024
EP000002          PEL         5000.00 €    ACTIF      20/10/2024
```

### Vérifications
- [ ] Numéro de compte affiché
- [ ] Type de compte correct
- [ ] Solde formaté correctement
- [ ] Taux d'intérêt visible
- [ ] Statut (ACTIF/CLOTURE) affiché
- [ ] Date d'ouverture formatée

---

## 3️⃣ Test Dépôt Épargne

**URL** : http://localhost:8081/centralisateur/epargne/depot

### Actions
1. Naviguer vers "Épargne" → "Dépôt"
2. Vérifier la liste des comptes actifs
3. Sélectionner un compte
4. Saisir un montant (ex: 100.00)
5. Ajouter une description
6. Soumettre

### ✅ Résultat Attendu
```
✅ Dépôt de 100.00 effectué avec succès ! 
   Nouveau solde : 1350.00 €
```

### Vérifications
- [ ] Liste comptes actifs uniquement
- [ ] Solde actuel visible
- [ ] Message de succès affiché
- [ ] Redirection vers liste comptes
- [ ] Nouveau solde correct dans la liste

---

## 4️⃣ Test Retrait Épargne

**URL** : http://localhost:8081/centralisateur/epargne/retrait

### Actions
1. Naviguer vers "Épargne" → "Retrait"
2. Vérifier la liste des comptes actifs
3. Sélectionner un compte avec solde suffisant
4. Saisir un montant (ex: 50.00)
5. Ajouter une description
6. Soumettre

### ✅ Résultat Attendu
```
✅ Retrait de 50.00 effectué avec succès ! 
   Nouveau solde : 1300.00 €
```

### Vérifications
- [ ] Liste comptes actifs uniquement
- [ ] Solde actuel visible
- [ ] Message de succès affiché
- [ ] Nouveau solde correct

### Tests Négatifs
- [ ] Retrait > Solde → Message d'erreur
- [ ] Montant négatif → Validation échouée
- [ ] Montant = 0 → Validation échouée

---

## 5️⃣ Test Historique Opérations

**URL** : http://localhost:8081/centralisateur/epargne/historique

### Actions
1. Naviguer vers "Épargne" → "Historique"
2. Vérifier la liste des comptes
3. Sélectionner un compte (dropdown)
4. Vérifier l'affichage des opérations

### ✅ Résultat Attendu
```
Date              Type       Montant    Solde Avant  Solde Après   Description
20/12/2024 14:30  DEPOT      100.00 €   1250.00 €    1350.00 €    Dépôt mensuel
20/12/2024 15:00  RETRAIT     50.00 €   1350.00 €    1300.00 €    Retrait ATM
```

### Vérifications
- [ ] Liste des comptes affichée
- [ ] Sélection compte fonctionnelle
- [ ] Toutes les colonnes remplies
- [ ] Dates formatées correctement
- [ ] Montants formatés correctement
- [ ] Soldes avant/après cohérents
- [ ] Description affichée

---

## 🐛 Scénarios de Debug

### Si erreur 500 persiste
1. Vérifier les logs WildFly :
   ```bash
   tail -f wildfly-29.0.1.Final/standalone/log/server.log
   ```

2. Chercher les NullPointerException :
   ```bash
   grep "NullPointerException" standalone/log/server.log
   ```

3. Vérifier les réponses API :
   - API Épargne : http://localhost:8083/api/epargne/types-compte
   - Doit retourner JSON avec PascalCase

### Si valeurs vides/null
1. Vérifier `JsonHelper` utilisé partout
2. Vérifier les imports dans les servlets
3. Recompiler : `mvn clean package`
4. Redéployer le WAR

### Si types de compte non affichés
1. Vérifier API Épargne démarrée
2. Tester directement : 
   ```bash
   curl http://localhost:8083/api/epargne/types-compte
   ```
3. Vérifier la base de données `EpargneDB`

---

## 📊 Rapport de Tests

### Session de Test
- **Date** : __________
- **Testeur** : __________
- **Version** : 1.0.0

### Résultats

| Test | Status | Commentaire |
|------|--------|-------------|
| Nouveau Compte | ⬜ | |
| Liste Comptes | ⬜ | |
| Dépôt | ⬜ | |
| Retrait | ⬜ | |
| Historique | ⬜ | |

**Légende** : ✅ Réussi | ❌ Échoué | ⚠️ Partiellement réussi | ⬜ Non testé

### Bugs Trouvés
```
1. 
2. 
3. 
```

### Actions Correctives
```
1. 
2. 
3. 
```

---

## ✅ Validation Finale

- [ ] Tous les tests passent ✅
- [ ] Aucune erreur 500
- [ ] Aucun NullPointerException dans les logs
- [ ] Toutes les pages chargent en < 2 secondes
- [ ] Les données sont cohérentes
- [ ] Les messages d'erreur sont clairs
- [ ] La navigation est fluide

---

**Module Épargne : OPÉRATIONNEL** ✅
