# 📊 Distinction entre `capitalRestant` et `montantTotalDu`

## 🎯 Résumé Rapide

| Propriété | Classe | Signification | Utilisation |
|-----------|--------|---------------|-------------|
| `capitalRestant` | **EcheanceDTO** | Capital restant après une échéance spécifique | Tableau d'amortissement (par ligne) |
| `montantTotalDu` | **PretDTO** | Montant total restant pour tout le prêt | Vue d'ensemble du prêt |

---

## 💡 Explication Détaillée

### 1️⃣ `capitalRestant` (dans EcheanceDTO)

**Définition** : Le capital qui reste à rembourser **après le paiement de cette échéance**.

**Contexte** : Chaque échéance d'un prêt comporte :
- Une part de **capital** (remboursement du montant emprunté)
- Une part d'**intérêts** (coût de l'emprunt)

Le `capitalRestant` diminue à chaque échéance payée.

#### Exemple Concret :
```
Prêt de 50 000 € sur 120 mois à 5.5%

Échéance #1 (01/02/2025):
  - Capital payé: 400 €
  - Intérêts payés: 142,50 €
  - Mensualité: 542,50 €
  - Capital restant: 49 600 €  ← après paiement de l'échéance #1

Échéance #2 (01/03/2025):
  - Capital payé: 402 €
  - Intérêts payés: 140,50 €
  - Mensualité: 542,50 €
  - Capital restant: 49 198 €  ← après paiement de l'échéance #2

...

Échéance #120 (01/01/2035):
  - Capital payé: 540 €
  - Intérêts payés: 2,50 €
  - Mensualité: 542,50 €
  - Capital restant: 0 €  ← prêt totalement remboursé
```

**Utilisation dans le code** :
```html
<!-- Tableau d'amortissement - detail-pret.html -->
<tr th:each="echeance : ${tableauAmortissement}">
    <td th:text="${echeance.numeroEcheance}">#1</td>
    <td th:text="${#temporals.format(echeance.dateEcheance, 'dd/MM/yyyy')}">01/02/2025</td>
    <td th:text="${#numbers.formatCurrency(echeance.capital)}">400,00 €</td>
    <td th:text="${#numbers.formatCurrency(echeance.interet)}">142,50 €</td>
    <td th:text="${#numbers.formatCurrency(echeance.montantEcheance)}">542,50 €</td>
    <td th:text="${#numbers.formatCurrency(echeance.capitalRestant)}">49 600,00 €</td>
    <!-- ↑ Capital restant APRÈS cette échéance -->
</tr>
```

---

### 2️⃣ `montantTotalDu` (dans PretDTO)

**Définition** : Le montant total que le client doit encore payer pour rembourser **l'intégralité du prêt**.

**Calcul** :
```
montantTotalDu = Capital emprunté + Tous les intérêts futurs + Pénalités éventuelles
```

**Contexte** : Ce montant représente la **dette totale** du client pour ce prêt à un instant T.

#### Exemple Concret :
```
Prêt de 50 000 € sur 120 mois à 5.5%

Au moment de l'octroi du prêt:
  - Capital emprunté: 50 000 €
  - Intérêts totaux à payer: 15 100 €
  - Mensualité: 542,50 €
  - Montant total dû: 65 100 €  ← tout ce que le client paiera au total

Après 1 échéance payée:
  - Capital payé: 400 €
  - Intérêts payés: 142,50 €
  - Capital restant: 49 600 €
  - Intérêts restants: 14 957,50 €
  - Montant total dû: 64 557,50 €  ← ce qui reste à payer

Après 60 échéances payées (5 ans):
  - Capital payé: 26 350 €
  - Intérêts payés: 6 200 €
  - Capital restant: 23 650 €
  - Intérêts restants: 8 900 €
  - Montant total dû: 32 550 €  ← ce qui reste à payer

Après 120 échéances payées (10 ans):
  - Capital payé: 50 000 €
  - Intérêts payés: 15 100 €
  - Montant total dû: 0 €  ← prêt remboursé
```

**Utilisation dans le code** :
```html
<!-- Vue d'ensemble du prêt - mes-prets.html -->
<div class="pret-card">
    <h3 th:text="${pret.numeroPret}">PRE-2025-0001</h3>
    <div class="detail-item">
        <span class="detail-label">Montant total dû:</span>
        <span class="detail-value" th:text="${#numbers.formatCurrency(pret.montantTotalDu)}">
            64 557,50 €
        </span>
        <!-- ↑ Montant total restant à payer pour ce prêt -->
    </div>
</div>

<!-- Page de détail - detail-pret.html -->
<div class="detail-item">
    <span class="detail-label">Montant total dû:</span>
    <span class="detail-value" th:text="${#numbers.formatCurrency(pret.montantTotalDu)}">
        64 557,50 €
    </span>
</div>

<!-- Page de remboursement - remboursement.html -->
<select id="idPret" name="idPret">
    <option th:each="pret : ${pretsActifs}" 
            th:value="${pret.idPret}" 
            th:text="${pret.numeroPret} + ' - ' + ${#numbers.formatCurrency(pret.montantTotalDu)} + ' restant'">
        PRE-2025-0001 - 64 557,50 € restant
    </option>
</select>
```

---

## 📐 Formules Mathématiques

### Capital Restant (après échéance N)
```
CR(N) = Capital emprunté - Σ(capital payé des échéances 1 à N)

Où:
- CR(N) = Capital restant après l'échéance N
- Σ = Somme
```

**Exemple** :
```
Capital emprunté: 50 000 €
Échéance #1: capital payé = 400 €
Échéance #2: capital payé = 402 €
Échéance #3: capital payé = 404 €

CR(3) = 50 000 - (400 + 402 + 404)
CR(3) = 50 000 - 1 206
CR(3) = 48 794 €
```

### Montant Total Dû (à l'instant T)
```
MTD(T) = Capital restant(T) + Intérêts futurs(T) + Pénalités(T)

Où:
- MTD(T) = Montant Total Dû à l'instant T
- Capital restant(T) = Capital non encore remboursé
- Intérêts futurs(T) = Intérêts sur toutes les échéances futures
- Pénalités(T) = Pénalités de retard accumulées
```

**Exemple** :
```
Après 3 échéances payées:
- Capital restant: 48 794 €
- Intérêts futurs (échéances 4 à 120): 14 763 €
- Pénalités: 0 €

MTD = 48 794 + 14 763 + 0
MTD = 63 557 €
```

---

## 🔄 Évolution au Fil du Temps

### Graphique Conceptuel

```
Montant
  │
65k│ ████████████████████████████████  ← Montant Total Dû (diminue)
  │ 
50k│ ████████████████                  ← Capital Restant (diminue)
  │ 
25k│ ████                              
  │ 
 0 └──────────────────────────────────→ Temps
   0   20   40   60   80  100  120 mois
```

À chaque échéance payée :
- ✅ Le **capital restant** diminue (part capital de la mensualité)
- ✅ Le **montant total dû** diminue (capital + intérêts payés)

---

## 🎯 Utilisation Pratique dans l'Application

### 1. Page "Mes Prêts" (`mes-prets.html`)
```html
<div class="pret-card">
    <span>Montant total dû: ${pret.montantTotalDu}</span>
    <!-- Affiche 64 557,50 € -->
    <!-- ↑ Utile pour que le client sache combien il doit encore au total -->
</div>
```

### 2. Page "Détail du Prêt" (`detail-pret.html`)
```html
<!-- En-tête du prêt -->
<div>
    <span>Montant total dû: ${pret.montantTotalDu}</span>
    <!-- Affiche 64 557,50 € -->
</div>

<!-- Tableau d'amortissement -->
<tr th:each="echeance : ${tableauAmortissement}">
    <td>${echeance.numeroEcheance}</td>
    <td>${echeance.capital}</td>
    <td>${echeance.interet}</td>
    <td>${echeance.capitalRestant}</td>
    <!-- ↑ Affiche le capital restant après CETTE échéance -->
    <!-- Échéance #1: 49 600 € -->
    <!-- Échéance #2: 49 198 € -->
    <!-- Échéance #3: 48 794 € -->
</tr>
```

### 3. Page "Remboursement" (`remboursement.html`)
```html
<!-- Liste déroulante -->
<option th:each="pret : ${pretsActifs}">
    ${pret.numeroPret} - ${pret.montantTotalDu} restant
    <!-- Affiche: PRE-2025-0001 - 64 557,50 € restant -->
    <!-- ↑ Info claire pour le client -->
</option>

<!-- Après sélection -->
<div>
    <span>Montant total dû: ${pretSelectionne.montantTotalDu}</span>
    <!-- Affiche 64 557,50 € -->
</div>
```

---

## ⚠️ Erreurs Fréquentes

### ❌ Erreur 1: Confondre les deux champs
```html
<!-- INCORRECT -->
<td>${pret.capitalRestant}</td>
<!-- ↑ PretDTO n'a PAS ce champ ! -->

<!-- CORRECT -->
<td>${pret.montantTotalDu}</td>
<!-- ↑ PretDTO a ce champ -->
```

### ❌ Erreur 2: Utiliser montantTotalDu dans le tableau d'amortissement
```html
<!-- INCORRECT -->
<td>${echeance.montantTotalDu}</td>
<!-- ↑ EcheanceDTO n'a PAS ce champ ! -->

<!-- CORRECT -->
<td>${echeance.capitalRestant}</td>
<!-- ↑ EcheanceDTO a ce champ -->
```

---

## 📚 Référence Rapide

### Quand utiliser `capitalRestant` ?
✅ **OUI** - Dans le **tableau d'amortissement** (chaque ligne d'échéance)
```html
<tr th:each="echeance : ${tableauAmortissement}">
    <td th:text="${echeance.capitalRestant}">49 600 €</td>
</tr>
```

❌ **NON** - Pour afficher le solde global du prêt

---

### Quand utiliser `montantTotalDu` ?
✅ **OUI** - Pour afficher le **solde total du prêt**
```html
<span th:text="${pret.montantTotalDu}">64 557,50 €</span>
```

✅ **OUI** - Dans les listes de prêts
```html
<option th:text="${pret.numeroPret} + ' - ' + ${pret.montantTotalDu} + ' restant'">
    PRE-2025-0001 - 64 557,50 € restant
</option>
```

❌ **NON** - Dans le détail des échéances individuelles

---

## 🔍 Vérification dans le Code Source

### PretDTO.java
```java
@Data
public class PretDTO implements Serializable {
    private BigDecimal montantDemande;
    private BigDecimal montantAccorde;
    private BigDecimal montantTotalDu;    // ✅ Existe
    private BigDecimal mensualite;
    // ❌ capitalRestant n'existe PAS
}
```

### EcheanceDTO.java
```java
@Data
public class EcheanceDTO implements Serializable {
    private BigDecimal capital;
    private BigDecimal interet;
    private BigDecimal montantEcheance;
    private BigDecimal capitalRestant;    // ✅ Existe
    // ❌ montantTotalDu n'existe PAS
}
```

---

**Conclusion** : Ces deux champs ont des significations et des utilisations différentes. Ne pas les confondre permet d'éviter les erreurs `NoSuchPropertyException` ! 🎯
