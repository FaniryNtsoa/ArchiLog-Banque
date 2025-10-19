# 📋 MODULE PRÊT - RÈGLES MÉTIER IMPLÉMENTÉES

Ce document détaille l'implémentation complète des 5 phases du cycle de vie d'un prêt.

---

## 📋 PHASE 1 : SIMULATION (Avant la demande)

### 🎯 Objectif
Permettre au client de simuler un prêt avant de faire une demande formelle.

### 📝 Données d'entrée
- **Montant souhaité** (BigDecimal)
- **Durée souhaitée** (Integer - en mois)
- **Type de prêt** (Long - idTypePret)
- **Revenu mensuel** (BigDecimal - optionnel pour vérifier les 33%)

### ✅ Contrôles implémentés

#### 1. Vérification des plafonds du type de prêt
```java
// Dans PretServiceImpl.simulerPret()
if (!CalculPretUtil.estMontantValide(montant, typePret.getMontantMin(), typePret.getMontantMax())) {
    throw new IllegalArgumentException("Le montant doit être entre MIN et MAX");
}

if (!CalculPretUtil.estDureeValide(duree, typePret.getDureeMin(), typePret.getDureeMax())) {
    throw new IllegalArgumentException("La durée doit être entre MIN et MAX mois");
}
```

#### 2. Calcul de la mensualité selon la formule
**Formule implémentée :** `M = [C × i] / [1 - (1 + i)^-n]`

Où :
- `M` = Mensualité
- `C` = Capital emprunté
- `i` = Taux mensuel = (taux annuel / 12) / 100
- `n` = Nombre de mois

```java
// Dans CalculPretUtil.calculerMensualite()
BigDecimal tauxMensuel = tauxAnnuel.divide(12, 10, HALF_UP).divide(100, 10, HALF_UP);
BigDecimal unPlusTaux = BigDecimal.ONE.add(tauxMensuel);
BigDecimal puissance = pow(unPlusTaux, -dureeMois);
BigDecimal denominateur = BigDecimal.ONE.subtract(puissance);
BigDecimal mensualite = montant.multiply(tauxMensuel).divide(denominateur, 2, HALF_UP);
```

#### 3. Vérification des 33% du revenu
```java
// Règle : Mensualité ≤ 33% du revenu mensuel du client
if (revenuMensuel != null && revenuMensuel > 0) {
    BigDecimal seuilEndettement = revenuMensuel * 0.33;
    if (mensualite > seuilEndettement) {
        throw new IllegalArgumentException("La mensualité dépasse 33% du revenu");
    }
}
```

### 📊 Résultats de la simulation
- **Mensualité calculée**
- **Montant total dû** = Mensualité × Durée
- **Coût total du crédit** = (Montant total dû - Montant emprunté) + Frais
- **Total des intérêts**
- **Tableau d'amortissement prévisionnel complet** avec pour chaque échéance :
  - Numéro d'échéance
  - Date d'échéance
  - Montant de l'échéance
  - Part de capital
  - Part d'intérêt
  - Capital restant dû

### 📍 Implémentation
- **Classe** : `PretServiceImpl`
- **Méthode** : `simulerPret(SimulationPretDTO)`
- **Utilise** : `CalculPretUtil` pour tous les calculs

---

## 📝 PHASE 2 : DEMANDE DE PRÊT

### 🎯 Objectif
Enregistrer la demande formelle du client avec les paramètres simulés.

### 📝 Workflow implémenté

#### 1. Validation du client
```java
// Le client doit être actif
if (client.getStatut() != StatutClient.ACTIF) {
    throw new IllegalStateException("Le client doit être actif");
}
```

#### 2. Génération d'un numéro de prêt unique
```java
// Dans l'entité Pret avec @PrePersist
private String generateNumeroPret() {
    long timestamp = System.currentTimeMillis();
    int random = (int) (Math.random() * 9999);
    return String.format("PRET%d%04d", timestamp, random);
}
```

#### 3. Calculs automatiques
- **Mensualité** : Calculée avec la formule
- **Montant total dû** : Mensualité × Durée
- **Date première échéance** : Date demande + 1 mois
- **Date dernière échéance** : Date première échéance + (Durée - 1) mois

#### 4. Statut initial
```java
pret.setStatut(StatutPret.EN_ATTENTE);
pret.setDateDemande(LocalDate.now());
```

### 📋 Données enregistrées
- Montant demandé
- Durée en mois
- Type de prêt
- Mensualité calculée
- Montant total dû
- Dates des échéances théoriques
- **Statut : EN_ATTENTE**
- Numéro de prêt unique

### 📍 Implémentation
- **Classe** : `PretServiceImpl`
- **Méthode** : `creerDemandePret(PretDTO)`
- **Entité** : `Pret`

---

## 🔍 PHASE 3 : INSTRUCTION & VALIDATION (juste après l'enregistrement)

### 🎯 Objectif
Vérifier l'éligibilité complète et décider d'approuver ou refuser le prêt.

### ✅ Règles de vérification implémentées

#### 1. Client actif et en règle
```java
if (client.getStatut() != StatutClient.ACTIF) {
    throw new IllegalStateException("Le client doit être actif");
}
```

#### 2. Revenus stables et suffisants
```java
if (client.getRevenuMensuel() == null || client.getRevenuMensuel() <= 0) {
    throw new IllegalStateException("Le client doit avoir des revenus déclarés");
}
```

#### 3. Taux d'endettement < 33% après le nouveau prêt
```java
BigDecimal nouvellesCharges = chargesMensuelles + mensualitePret;
BigDecimal tauxEndettement = (nouvellesCharges / revenuMensuel) * 100;

if (tauxEndettement > 33) {
    throw new IllegalStateException("Taux d'endettement trop élevé: X% (max 33%)");
}
```

#### 4. Capacité de remboursement
**Règle** : `(Revenus - Charges existantes) ≥ Mensualité × 1.3`

```java
BigDecimal capaciteRemboursement = revenuMensuel - chargesMensuelles;
BigDecimal seuilCapacite = mensualite * 1.3;

if (capaciteRemboursement < seuilCapacite) {
    throw new IllegalStateException("Capacité de remboursement insuffisante");
}
```

### 🎯 Décision

#### ✅ APPROUVÉ
**Si tous les critères sont remplis :**
```java
pret.setStatut(StatutPret.APPROUVE);
pret.setDateApprobation(LocalDate.now());
// Montant accordé peut être ≤ montant demandé (ajustable)
pret.setMontantAccorde(montantAccorde);
```

**Actions automatiques après approbation :**
1. Génération automatique du tableau d'amortissement définitif (PHASE 4)
2. Passage du statut à `EN_COURS`

#### ❌ REFUSÉ
**Si au moins un critère non respecté :**
```java
pret.setStatut(StatutPret.REFUSE);
pret.setMotifRefus("Raison du refus"); // Obligatoire
```

### 📍 Implémentation
- **Classe** : `PretServiceImpl`
- **Méthodes** : `approuverPret(Long idPret)`, `refuserPret(Long idPret, String motif)`

---

## 📊 PHASE 4 : GÉNÉRATION DES ÉCHÉANCES (automatique après approbation)

### 🎯 Objectif
Créer automatiquement toutes les échéances du prêt après son approbation.

### ⚙️ Règles implémentées

#### 1. Création de N échéances (N = durée en mois)
```java
// Génération automatique appelée dans approuverPret()
private void genererTableauAmortissementDefinitif(Pret pret) {
    // Génère N échéances
}
```

#### 2. Date de première échéance
**Règle** : `Date d'approbation + 1 mois`

```java
LocalDate datePremiereEcheance = pret.getDateApprobation().plusMonths(1);
pret.setDatePremiereEcheance(datePremiereEcheance);
pret.setDateDerniereEcheance(datePremiereEcheance.plusMonths(duree - 1));
```

#### 3. Calcul de chaque échéance (méthode d'amortissement constant)

**Pour chaque échéance :**
```java
// Intérêts = Capital restant × Taux mensuel
BigDecimal interet = capitalRestant * tauxMensuel;

// Capital = Mensualité - Intérêts
BigDecimal capital = mensualite - interet;

// Capital restant = Capital restant précédent - Capital
capitalRestant = capitalRestant - capital;
```

#### 4. Statut initial de chaque échéance
```java
echeance.setStatut(StatutEcheance.A_VENIR);
echeance.setNumeroEcheance(i); // Numéro séquentiel (1, 2, 3...)
echeance.setDateEcheance(datePremiereEcheance.plusMonths(i - 1));
```

### 📋 Données de chaque échéance
- **Numéro séquentiel** (1, 2, 3, ..., N)
- **Montant de l'échéance** (mensualité)
- **Part de capital** (amortissement)
- **Part d'intérêt** (intérêts sur le capital restant)
- **Capital restant dû** (après paiement)
- **Date d'échéance**
- **Statut** : A_VENIR

### 📍 Implémentation
- **Classe** : `PretServiceImpl`
- **Méthode privée** : `genererTableauAmortissementDefinitif(Pret)`
- **Appelée automatiquement par** : `approuverPret()`
- **Utilise** : `CalculPretUtil.genererTableauAmortissement()`

---

## 💰 PHASE 5 : GESTION DES REMBOURSEMENTS

### 🎯 Objectif
Gérer les paiements mensuels et mettre à jour les statuts des échéances et du prêt.

### 📝 Workflow mensuel implémenté

#### 1. Vérification quotidienne des échéances
```java
// Méthode à appeler par un scheduler quotidien
public void verifierEcheancesMensuelles() {
    LocalDate aujourdhui = LocalDate.now();
    
    for (Echeance echeance : echeancesNonPayees) {
        if (aujourdhui.isEqual(echeance.getDateEcheance())) {
            echeance.setStatut(StatutEcheance.ECHEANCE_AUJOURDHUI);
        } 
        else if (joursRetard > DELAI_TOLERANCE) {
            echeance.setStatut(StatutEcheance.EN_RETARD);
            pret.setStatut(StatutPret.EN_RETARD);
        }
    }
}
```

#### 2. Enregistrement d'un remboursement

##### ✅ Paiement complet
```java
if (montantPaye >= montantEcheance) {
    if (joursRetard > DELAI_TOLERANCE) {
        echeance.setStatut(StatutEcheance.PAYE_AVEC_RETARD);
    } else {
        echeance.setStatut(StatutEcheance.PAYE);
    }
    echeance.setDatePaiement(LocalDate.now());
}
```

##### ⚠️ Paiement partiel/aucun
```java
if (joursRetard > DELAI_TOLERANCE) {
    echeance.setStatut(StatutEcheance.EN_RETARD);
}
```

#### 3. Remboursement couvrant plusieurs échéances
**Règle importante** : Un remboursement peut couvrir partiellement plusieurs échéances

**MAIS** : On ne peut pas rembourser une échéance si avant cette dernière il y a encore une/des échéances non payées.

```java
// Tri des échéances par ordre chronologique
List<Echeance> echeancesImpayees = echeances
    .stream()
    .filter(e -> !estPayee(e))
    .sorted(Comparator.comparing(Echeance::getNumeroEcheance))
    .collect(Collectors.toList());

BigDecimal montantRestant = montantRemboursement;

for (Echeance echeance : echeancesImpayees) {
    if (montantRestant <= 0) break;
    
    BigDecimal montantAPayer = min(montantRestant, echeance.getMontantEcheance());
    
    // Créer remboursement
    Remboursement remb = creerRemboursement(echeance, montantAPayer);
    
    if (montantAPayer >= echeance.getMontantEcheance()) {
        // Échéance complètement payée
        echeance.setStatut(PAYE);
        montantRestant -= echeance.getMontantEcheance();
    } else {
        // Paiement partiel => on s'arrête
        montantRestant = 0;
    }
}
```

#### 4. Calcul des pénalités
```java
// Pénalité après délai de tolérance (5 jours)
if (joursRetard > DELAI_TOLERANCE) {
    // 0.05% par jour de retard sur le montant de l'échéance
    BigDecimal penalite = montantEcheance 
        * 0.0005 
        * (joursRetard - DELAI_TOLERANCE);
    
    echeance.setPenaliteAppliquee(penalite);
    echeance.setJoursRetard(joursRetard);
}
```

#### 5. Remboursement anticipé
**Règle** : Possibilité de remboursement anticipé avec recalcul

- Si `montantRemboursement > montantEcheance`, le surplus est appliqué aux échéances suivantes
- Tant qu'il reste du montant, on continue à payer les échéances suivantes dans l'ordre
- Le montant restant éventuel peut être remboursé plus tard

#### 6. Mise à jour du statut du prêt

##### 🎉 Toutes les échéances payées
```java
boolean toutesPayees = echeances.stream()
    .allMatch(e -> e.getStatut() == PAYE || e.getStatut() == PAYE_AVEC_RETARD);

if (toutesPayees) {
    pret.setStatut(StatutPret.TERMINE);
}
```

##### ⚠️ Au moins une échéance en retard
```java
boolean aDesRetards = echeances.stream()
    .anyMatch(e -> e.getStatut() == EN_RETARD);

if (aDesRetards) {
    pret.setStatut(StatutPret.EN_RETARD);
}
```

### 📋 Données enregistrées pour chaque remboursement
- **ID de l'échéance**
- **Montant payé**
- **Date de remboursement**
- **Pénalités appliquées** (si retard)
- **ID du compte** (lien avec compte courant)
- **Type de paiement** (VIREMENT, PRELEVEMENT, etc.)
- **Numéro de transaction**

### 📍 Implémentation
- **Classe** : `EcheanceServiceImpl`
- **Méthodes** :
  - `enregistrerRemboursement(RemboursementDTO)` - Enregistre un paiement
  - `verifierEcheancesMensuelles()` - Vérification quotidienne (scheduler)
  - `verifierEtatPret(Pret)` - Mise à jour du statut du prêt
  - `listerEcheancesImpayees(Long idPret)` - Liste des échéances à payer
  - `listerEcheancesEnRetard()` - Liste toutes les échéances en retard

---

## 📊 DIAGRAMME DE FLUX COMPLET

```
┌─────────────────────────────────────────────────────────────┐
│                    PHASE 1 : SIMULATION                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Client renseigne : montant, durée, type de prêt      │   │
│  │ ↓                                                      │   │
│  │ Vérification des plafonds                             │   │
│  │ ↓                                                      │   │
│  │ Calcul mensualité : M = [C × i] / [1 - (1 + i)^-n]   │   │
│  │ ↓                                                      │   │
│  │ Vérification 33% du revenu                            │   │
│  │ ↓                                                      │   │
│  │ Affichage tableau d'amortissement prévisionnel        │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   PHASE 2 : DEMANDE DE PRÊT                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Client soumet demande avec paramètres simulés         │   │
│  │ ↓                                                      │   │
│  │ Génération numéro unique (PRETXXXXXXXXXX)             │   │
│  │ ↓                                                      │   │
│  │ Statut : EN_ATTENTE                                    │   │
│  │ ↓                                                      │   │
│  │ Notification agent de crédit                           │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│            PHASE 3 : INSTRUCTION & VALIDATION                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Vérifications :                                        │   │
│  │   ✓ Client actif et en règle                           │   │
│  │   ✓ Revenus stables et suffisants                      │   │
│  │   ✓ Taux d'endettement < 33%                           │   │
│  │   ✓ Capacité remboursement ≥ Mensualité × 1.3         │   │
│  │                                                         │   │
│  │         ┌─────────────┐      ┌─────────────┐          │   │
│  │         │  APPROUVÉ   │      │   REFUSÉ    │          │   │
│  │         │ + motif     │      │ + motif     │          │   │
│  │         └──────┬──────┘      └─────────────┘          │   │
│  └────────────────┼─────────────────────────────────────┘   │
└──────────────────┼──────────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────────────────────────┐
│        PHASE 4 : GÉNÉRATION DES ÉCHÉANCES (AUTO)             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Création de N échéances (N = durée en mois)           │   │
│  │ ↓                                                      │   │
│  │ Date première échéance = date approbation + 1 mois    │   │
│  │ ↓                                                      │   │
│  │ Pour chaque échéance (1 à N) :                         │   │
│  │   - Numéro séquentiel                                  │   │
│  │   - Intérêt = Capital restant × Taux mensuel          │   │
│  │   - Capital = Mensualité - Intérêt                     │   │
│  │   - Capital restant = Capital restant - Capital        │   │
│  │   - Statut : A_VENIR                                   │   │
│  │ ↓                                                      │   │
│  │ Statut prêt : EN_COURS                                 │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│           PHASE 5 : GESTION DES REMBOURSEMENTS               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ WORKFLOW MENSUEL (vérification quotidienne) :          │   │
│  │                                                         │   │
│  │ Date échéance = Aujourd'hui ?                          │   │
│  │   └─ OUI → Statut : ECHEANCE_AUJOURDHUI                │   │
│  │                                                         │   │
│  │ Paiement reçu ?                                         │   │
│  │   ├─ COMPLET                                            │   │
│  │   │   └─ Retard > 5 jours ?                            │   │
│  │   │       ├─ OUI → PAYE_AVEC_RETARD + pénalités        │   │
│  │   │       └─ NON → PAYE                                 │   │
│  │   │                                                     │   │
│  │   ├─ PARTIEL                                            │   │
│  │   │   └─ Remboursement enregistré, on s'arrête là      │   │
│  │   │                                                     │   │
│  │   └─ AUCUN                                              │   │
│  │       └─ Retard > 5 jours ?                            │   │
│  │           └─ OUI → EN_RETARD + Prêt EN_RETARD          │   │
│  │                                                         │   │
│  │ Montant > Mensualité ?                                  │   │
│  │   └─ OUI → Remboursement automatique des échéances     │   │
│  │            suivantes (ordre chronologique)              │   │
│  │            MAIS respect de l'ordre (pas de saut)        │   │
│  │                                                         │   │
│  │ Toutes échéances payées ?                              │   │
│  │   └─ OUI → Prêt TERMINE 🎉                             │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔐 ENTITÉS IMPLIQUÉES

### Client
- `chargesMensuelles` : **Ajouté** pour calculer le taux d'endettement

### TypePret
- Définit les limites : `montantMin`, `montantMax`, `dureeMin`, `dureeMax`
- Taux d'intérêt annuel
- Frais de dossier

### Pret
- Statuts : `EN_ATTENTE`, `APPROUVE`, `REFUSE`, `EN_COURS`, `EN_RETARD`, `TERMINE`
- `numeroPret` : Généré automatiquement (unique)
- `montantDemande` vs `montantAccorde`
- Dates clés : `dateDemande`, `dateApprobation`, `datePremiereEcheance`, `dateDerniereEcheance`

### Echeance
- Statuts : `A_VENIR`, `ECHEANCE_AUJOURDHUI`, `EN_RETARD`, `PAYE`, `PAYE_AVEC_RETARD`
- `numeroEcheance` : Séquentiel (1, 2, 3, ..., N)
- Montants : `montantEcheance`, `capital`, `interet`, `capitalRestant`
- `penaliteAppliquee`, `joursRetard`

### Remboursement
- Lien avec `Echeance`
- `montantPaye`, `penalites`, `dateRemboursement`
- `idCompte` : Lien avec le compte courant
- `typePaiement`, `numeroTransaction`

---

## 🚀 MÉTHODES PRINCIPALES

### PretServiceImpl
1. ✅ `simulerPret(SimulationPretDTO)` - PHASE 1
2. ✅ `creerDemandePret(PretDTO)` - PHASE 2
3. ✅ `approuverPret(Long idPret)` - PHASE 3
4. ✅ `refuserPret(Long idPret, String motif)` - PHASE 3
5. 🔒 `genererTableauAmortissementDefinitif(Pret)` - PHASE 4 (privée, auto)
6. 🔒 `verifierEligibilite(PretDTO, TypePret, Client)` - PHASE 2/3 (privée)

### EcheanceServiceImpl
1. ✅ `obtenirTableauAmortissement(Long idPret)` - Affichage
2. ✅ `enregistrerRemboursement(RemboursementDTO)` - PHASE 5
3. ✅ `verifierEcheancesMensuelles()` - PHASE 5 (scheduler quotidien)
4. 🔒 `verifierEtatPret(Pret)` - PHASE 5 (privée, auto)
5. ✅ `listerEcheancesImpayees(Long idPret)` - Liste échéances à payer
6. ✅ `listerEcheancesEnRetard()` - Liste toutes les échéances en retard

### CalculPretUtil
1. ✅ `calculerMensualite()` - Formule mathématique
2. ✅ `genererTableauAmortissement()` - Tableau complet
3. ✅ `calculerMontantTotalDu()` - Total à rembourser
4. ✅ `calculerCoutTotalCredit()` - Coût du crédit
5. ✅ `calculerTotalInterets()` - Somme des intérêts
6. ✅ `estMontantValide()` - Validation montant
7. ✅ `estDureeValide()` - Validation durée

---

## 📝 NOTES IMPORTANTES

### 🎯 Points clés implémentés
1. ✅ **Formule exacte** : `M = [C × i] / [1 - (1 + i)^-n]`
2. ✅ **Vérification 33%** : À la simulation ET à la demande
3. ✅ **Ordre chronologique** : Impossible de sauter des échéances
4. ✅ **Remboursement multiple** : Un paiement peut couvrir plusieurs échéances
5. ✅ **Pénalités automatiques** : Après 5 jours de délai de tolérance
6. ✅ **Mise à jour automatique** : Statuts prêt et échéances
7. ✅ **Génération automatique** : Tableau d'amortissement après approbation

### ⚙️ Configuration
- **Délai de tolérance** : 5 jours (constante `DELAI_TOLERANCE`)
- **Taux de pénalité** : 0.05% par jour de retard
- **Coefficient de capacité** : 1.3 × Mensualité
- **Taux d'endettement max** : 33%

### 🔄 Processus automatiques
1. Génération du numéro de prêt (`@PrePersist`)
2. Génération du tableau d'amortissement (après approbation)
3. Calcul des pénalités (lors du remboursement en retard)
4. Mise à jour des statuts (échéances et prêt)

---

## ✅ CHECKLIST DE CONFORMITÉ

- [x] **PHASE 1** : Simulation complète avec contrôles
- [x] **PHASE 2** : Demande avec validation et génération numéro unique
- [x] **PHASE 3** : Instruction avec 4 critères d'éligibilité
- [x] **PHASE 4** : Génération automatique N échéances après approbation
- [x] **PHASE 5** : Gestion complète des remboursements
- [x] Formule mathématique exacte implémentée
- [x] Vérification 33% du revenu
- [x] Remboursement automatique de plusieurs échéances
- [x] Impossible de sauter des échéances
- [x] Calcul automatique des pénalités
- [x] Mise à jour automatique des statuts
- [x] Tableau d'amortissement avec détail capital/intérêt

---

**Toutes les règles métier sont implémentées correctement, clairement et proprement!** ✅
