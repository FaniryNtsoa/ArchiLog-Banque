# 🎯 CORRECTION RAPIDE - JSON Array Parsing

## ❌ Le Problème

```
JsonParser#getObject() is valid only for START_OBJECT parser state. 
But current parser state is START_ARRAY
```

**Quand ?** 
- Page "Nouveau Compte Épargne" → Liste types vide
- Page "Dépôt Épargne" → Liste comptes vide

---

## 🔍 La Cause

**API .NET renvoie** :
```json
[{...}, {...}]
```

**Code Java attend** :
```json
{"success": true, "data": [{...}, {...}]}
```

---

## ✅ La Solution

**Fichier** : `EpargneRestClient.java`  
**Méthode** : `readJsonResponse()`

**AVANT** :
```java
return jsonReader.readObject(); // ❌ Plante si tableau
```

**APRÈS** :
```java
JsonStructure structure = jsonReader.read();

if (structure instanceof JsonObject) {
    return (JsonObject) structure;
} else if (structure instanceof JsonArray) {
    return Json.createObjectBuilder()
        .add("success", true)
        .add("data", (JsonArray) structure)
        .build();
}
```

---

## 🧪 Tester

```bash
# Recompiler
cd Centralisateur
mvn clean package

# Résultat attendu
✅ BUILD SUCCESS
✅ centralisateur.war généré
```

---

## ✅ Résultat

**AVANT** :
- ❌ Page erreur 500
- ❌ Listes vides
- ❌ Logs pleins d'exceptions

**APRÈS** :
- ✅ Page s'affiche
- ✅ Listes remplies
- ✅ Aucune erreur

---

**Status** : ✅ **RÉSOLU**  
**Version** : 1.0.2
