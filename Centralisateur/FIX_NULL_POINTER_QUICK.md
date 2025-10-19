# 🔧 CORRECTION RAPIDE - NullPointerException dans Servlets Épargne

## ❌ Le Problème

```
NullPointerException: Cannot invoke "jakarta.json.JsonNumber.longValue()" 
because the return value of "jakarta.json.JsonObject.getJsonNumber(String)" is null
```

**Cause** : L'API .NET renvoie les champs JSON en **PascalCase** (`IdTypeCompte`, `CodeType`, etc.) mais le code Java s'attend à **camelCase** (`idTypeCompte`, `codeType`, etc.)

---

## ✅ La Solution

### 1. Classe Utilitaire Créée

**Fichier** : `JsonHelper.java`  
**Package** : `com.banque.centralisateur.util`

Cette classe fournit des méthodes sûres pour extraire les valeurs JSON en gérant automatiquement les deux conventions de nommage (PascalCase et camelCase).

**Méthodes disponibles** :
- `getSafeString(JsonObject json, String key)` 
- `getSafeString(JsonObject json, String key, String defaultValue)`
- `getSafeLong(JsonObject json, String key)`
- `getSafeLong(JsonObject json, String key, Long defaultValue)`
- `getSafeInt(JsonObject json, String key)`
- `getSafeInt(JsonObject json, String key, Integer defaultValue)`
- `getSafeBigDecimal(JsonObject json, String key)`
- `getSafeBigDecimal(JsonObject json, String key, BigDecimal defaultValue)`
- `getSafeBoolean(JsonObject json, String key)`
- `getSafeBoolean(JsonObject json, String key, Boolean defaultValue)`

---

### 2. Corrections à Appliquer

#### A. NouveauCompteEpargneServlet.java

**Ajouter l'import** :
```java
import com.banque.centralisateur.util.JsonHelper;
```

**Remplacer** (lignes ~67-76) :
```java
// AVANT
view.setIdTypeCompte(type.getJsonNumber("idTypeCompte").longValue());
view.setLibelle(type.getString("libelle"));
view.setCodeType(type.getString("codeType"));
view.setDescription(type.getString("description", ""));
view.setTauxInteretAnnuel(type.getJsonNumber("tauxInteretAnnuel").bigDecimalValue());
view.setDepotInitialMin(type.getJsonNumber("depotInitialMin").bigDecimalValue());
view.setSoldeMinObligatoire(type.getJsonNumber("soldeMinObligatoire").bigDecimalValue());
view.setPlafondDepot(type.getJsonNumber("plafondDepot").bigDecimalValue());
view.setPeriodiciteCalculInteret(type.getString("periodiciteCalculInteret"));

// APRÈS
view.setIdTypeCompte(JsonHelper.getSafeLong(type, "idTypeCompte"));
view.setLibelle(JsonHelper.getSafeString(type, "libelle", ""));
view.setCodeType(JsonHelper.getSafeString(type, "codeType", ""));
view.setDescription(JsonHelper.getSafeString(type, "description", ""));
view.setTauxInteretAnnuel(JsonHelper.getSafeBigDecimal(type, "tauxInteretAnnuel", BigDecimal.ZERO));
view.setDepotInitialMin(JsonHelper.getSafeBigDecimal(type, "depotInitialMin", BigDecimal.ZERO));
view.setSoldeMinObligatoire(JsonHelper.getSafeBigDecimal(type, "soldeMinObligatoire", BigDecimal.ZERO));
view.setPlafondDepot(JsonHelper.getSafeBigDecimal(type, "plafondDepot", BigDecimal.ZERO));
view.setPeriodiciteCalculInteret(JsonHelper.getSafeString(type, "periodiciteCalculInteret", ""));
```

**Remplacer** (ligne ~143) :
```java
// AVANT
String numeroCompte = compte.getString("numeroCompte");

// APRÈS
String numeroCompte = JsonHelper.getSafeString(compte, "numeroCompte", "");
```

---

#### B. ComptesEpargneServlet.java

**Ajouter l'import** :
```java
import com.banque.centralisateur.util.JsonHelper;
```

**Remplacer** (lignes ~69-79) :
```java
// AVANT
view.setIdCompte(compte.getJsonNumber("idCompte").longValue());
view.setNumeroCompte(compte.getString("numeroCompte"));
view.setSolde(compte.getJsonNumber("solde").bigDecimalValue());
view.setDateOuverture(compte.getString("dateOuverture"));
view.setStatut(compte.getString("statut"));

if (compte.containsKey("typeCompte")) {
    JsonObject typeCompte = compte.getJsonObject("typeCompte");
    view.setTypeLibelle(typeCompte.getString("libelle"));
    view.setTauxInteret(typeCompte.getJsonNumber("tauxInteretAnnuel").bigDecimalValue());
}

// APRÈS
view.setIdCompte(JsonHelper.getSafeLong(compte, "idCompte"));
view.setNumeroCompte(JsonHelper.getSafeString(compte, "numeroCompte", ""));
view.setSolde(JsonHelper.getSafeBigDecimal(compte, "solde", BigDecimal.ZERO));
view.setDateOuverture(JsonHelper.getSafeString(compte, "dateOuverture", ""));
view.setStatut(JsonHelper.getSafeString(compte, "statut", ""));

if (compte.containsKey("typeCompte") || compte.containsKey("TypeCompte")) {
    JsonObject typeCompte = compte.containsKey("typeCompte") ? 
        compte.getJsonObject("typeCompte") : 
        compte.getJsonObject("TypeCompte");
    view.setTypeLibelle(JsonHelper.getSafeString(typeCompte, "libelle", ""));
    view.setTauxInteret(JsonHelper.getSafeBigDecimal(typeCompte, "tauxInteretAnnuel", BigDecimal.ZERO));
}
```

---

#### C. DepotEpargneServlet.java

**Ajouter l'import** :
```java
import com.banque.centralisateur.util.JsonHelper;
```

**Remplacer** (lignes ~67-76) :
```java
// AVANT
String statut = compte.getString("statut");

if ("ACTIF".equals(statut)) {
    view.setIdCompte(compte.getJsonNumber("idCompte").longValue());
    view.setNumeroCompte(compte.getString("numeroCompte"));
    view.setSolde(compte.getJsonNumber("solde").bigDecimalValue());
    
    if (compte.containsKey("typeCompte")) {
        JsonObject typeCompte = compte.getJsonObject("typeCompte");
        view.setTypeLibelle(typeCompte.getString("libelle"));
    }

// APRÈS
String statut = JsonHelper.getSafeString(compte, "statut", "");

if ("ACTIF".equals(statut)) {
    view.setIdCompte(JsonHelper.getSafeLong(compte, "idCompte"));
    view.setNumeroCompte(JsonHelper.getSafeString(compte, "numeroCompte", ""));
    view.setSolde(JsonHelper.getSafeBigDecimal(compte, "solde", BigDecimal.ZERO));
    
    if (compte.containsKey("typeCompte") || compte.containsKey("TypeCompte")) {
        JsonObject typeCompte = compte.containsKey("typeCompte") ? 
            compte.getJsonObject("typeCompte") : 
            compte.getJsonObject("TypeCompte");
        view.setTypeLibelle(JsonHelper.getSafeString(typeCompte, "libelle", ""));
    }
```

**Remplacer** (ligne ~148) :
```java
// AVANT
BigDecimal nouveauSolde = operation.getJsonNumber("soldeApres").bigDecimalValue();

// APRÈS
BigDecimal nouveauSolde = JsonHelper.getSafeBigDecimal(operation, "soldeApres", BigDecimal.ZERO);
```

---

#### D. RetraitEpargneServlet.java

**Ajouter l'import** :
```java
import com.banque.centralisateur.util.JsonHelper;
```

**Remplacer** (mêmes corrections que DepotEpargneServlet) :
- Lignes ~67-76 : Utiliser JsonHelper pour lecture comptes
- Ligne ~148 : Utiliser JsonHelper pour soldeApres

---

#### E. HistoriqueEpargneServlet.java

**Ajouter l'import** :
```java
import com.banque.centralisateur.util.JsonHelper;
```

**Remplacer** (lignes ~72-79) :
```java
// AVANT
view.setIdCompte(compte.getJsonNumber("idCompte").longValue());
view.setNumeroCompte(compte.getString("numeroCompte"));
view.setSolde(compte.getJsonNumber("solde").bigDecimalValue());

if (compte.containsKey("typeCompte")) {
    JsonObject typeCompte = compte.getJsonObject("typeCompte");
    view.setTypeLibelle(typeCompte.getString("libelle"));
}

// APRÈS
view.setIdCompte(JsonHelper.getSafeLong(compte, "idCompte"));
view.setNumeroCompte(JsonHelper.getSafeString(compte, "numeroCompte", ""));
view.setSolde(JsonHelper.getSafeBigDecimal(compte, "solde", BigDecimal.ZERO));

if (compte.containsKey("typeCompte") || compte.containsKey("TypeCompte")) {
    JsonObject typeCompte = compte.containsKey("typeCompte") ? 
        compte.getJsonObject("typeCompte") : 
        compte.getJsonObject("TypeCompte");
    view.setTypeLibelle(JsonHelper.getSafeString(typeCompte, "libelle", ""));
}
```

**Remplacer** (lignes ~104-110) :
```java
// AVANT
opView.setIdOperation(op.getJsonNumber("idOperation").longValue());
opView.setTypeOperation(op.getString("typeOperation"));
opView.setMontant(op.getJsonNumber("montant").bigDecimalValue());
opView.setSoldeAvant(op.getJsonNumber("soldeAvant").bigDecimalValue());
opView.setSoldeApres(op.getJsonNumber("soldeApres").bigDecimalValue());
opView.setDescription(op.getString("description", ""));
opView.setDateOperation(op.getString("dateOperation"));

// APRÈS
opView.setIdOperation(JsonHelper.getSafeLong(op, "idOperation"));
opView.setTypeOperation(JsonHelper.getSafeString(op, "typeOperation", ""));
opView.setMontant(JsonHelper.getSafeBigDecimal(op, "montant", BigDecimal.ZERO));
opView.setSoldeAvant(JsonHelper.getSafeBigDecimal(op, "soldeAvant", BigDecimal.ZERO));
opView.setSoldeApres(JsonHelper.getSafeBigDecimal(op, "soldeApres", BigDecimal.ZERO));
opView.setDescription(JsonHelper.getSafeString(op, "description", ""));
opView.setDateOperation(JsonHelper.getSafeString(op, "dateOperation", ""));
```

---

## 📝 Récapitulatif

### Fichiers Modifiés

| Fichier | Imports | Corrections |
|---------|---------|-------------|
| `JsonHelper.java` | - | ✅ CRÉÉ (classe utilitaire) |
| `NouveauCompteEpargneServlet.java` | ✅ | ✅ |
| `ComptesEpargneServlet.java` | ✅ | ✅ |
| `DepotEpargneServlet.java` | ❓ | ❓ |
| `RetraitEpargneServlet.java` | ❓ | ❓ |
| `HistoriqueEpargneServlet.java` | ❓ | ❓ |

### Compilation

```bash
cd Centralisateur
mvn clean compile
# ✅ Résultat attendu : BUILD SUCCESS
```

---

## 🎯 Avantages de la Solution

1. **Robustesse** : Aucun NullPointerException même si un champ est absent
2. **Flexibilité** : Gère PascalCase ET camelCase automatiquement
3. **Valeurs par défaut** : Retourne des valeurs sûres au lieu de null
4. **Maintenabilité** : Code plus propre et facile à comprendre

---

## 🧪 Vérification

Après compilation, tester :
1. ✅ Page "Nouveau Compte Épargne" → Liste types affichée
2. ✅ Page "Mes Comptes" → Liste comptes affichée
3. ✅ Page "Dépôt" → Liste comptes actifs affichée
4. ✅ Page "Retrait" → Liste comptes actifs affichée
5. ✅ Page "Historique" → Opérations affichées

---

**Status** : ⏳ **EN COURS**  
**Prochaine étape** : Appliquer les corrections sur DepotEpargneServlet, RetraitEpargneServlet, HistoriqueEpargneServlet
