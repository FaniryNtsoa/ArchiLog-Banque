-- ============================================
--   BASE 3 : Prêts
-- ============================================
CREATE DATABASE pret_db;
USE pret_db;

-- Table des clients (avec login)
CREATE TABLE client (
    id_client INT AUTO_INCREMENT PRIMARY KEY,
    numero_client VARCHAR(20) UNIQUE NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    date_naissance DATE NOT NULL,
    num_cin VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    telephone VARCHAR(20),
    adresse VARCHAR(255),
    code_postal VARCHAR(10),
    ville VARCHAR(100),
    profession VARCHAR(100),
    revenu_mensuel DECIMAL(15,2),
    solde_initial DECIMAL(15,2) DEFAULT 0 NOT NULL,
    situation_familiale ENUM('CELIBATAIRE', 'MARIE', 'DIVORCE', 'VEUF'),
    mot_de_passe VARCHAR(255) NOT NULL,
    statut ENUM('ACTIF', 'SUSPENDU', 'FERME') DEFAULT 'ACTIF',
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table des types de prêts
CREATE TABLE type_pret (
    id_type_pret SERIAL PRIMARY KEY,
    code_type VARCHAR(20) UNIQUE NOT NULL,
    libelle VARCHAR(100) NOT NULL,
    taux_interet_annuel DECIMAL(6,4) NOT NULL,
    duree_min INTEGER NOT NULL,
    duree_max INTEGER NOT NULL,
    montant_min DECIMAL(15,2) NOT NULL,
    montant_max DECIMAL(15,2) NOT NULL,
    frais_dossier DECIMAL(10,2) DEFAULT 0,
    
    -- Champs prévus pour les pénalités (à implémenter plus tard)
    penalite_retard_taux DECIMAL(5,4) DEFAULT 0, -- Taux de pénalité pour retard
    delai_tolerance_jours INTEGER DEFAULT 5, -- Délai de grâce avant pénalités
    
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table principale des prêts
CREATE TABLE pret (
    id_pret SERIAL PRIMARY KEY,
    id_client INTEGER NOT NULL,
    id_type_pret INTEGER NOT NULL,
    numero_pret VARCHAR(30) UNIQUE NOT NULL,
    montant_demande DECIMAL(15,2) NOT NULL,
    montant_accordé DECIMAL(15,2) NOT NULL,
    duree_mois INTEGER NOT NULL,
    taux_interet_annuel DECIMAL(6,4) NOT NULL,
    montant_total_du DECIMAL(15,2) NOT NULL,
    mensualite DECIMAL(15,2) NOT NULL,
    
    -- Champs pour suivi global des pénalités
    total_penalites DECIMAL(10,2) DEFAULT 0, -- Somme des pénalités appliquées
    
    date_demande DATE NOT NULL DEFAULT CURRENT_DATE,
    date_approbation DATE,
    date_premiere_echeance DATE NOT NULL,
    date_derniere_echeance DATE NOT NULL,
    statut pret_statut DEFAULT 'EN_ATTENTE',
    motif_refus TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_client) REFERENCES client(id_client),
    FOREIGN KEY (id_type_pret) REFERENCES type_pret(id_type_pret)
);

-- Enum pour le statut du prêt
CREATE TYPE pret_statut AS ENUM (
    'EN_ATTENTE', 
    'APPROUVE', 
    'REFUSE', 
    'EN_COURS', 
    'EN_RETARD', 
    'TERMINE'
);

-- Table des échéances (tableau d'amortissement)
CREATE TABLE echeance (
    id_echeance SERIAL PRIMARY KEY,
    id_pret INTEGER NOT NULL,
    numero_echeance INTEGER NOT NULL,
    montant_echeance DECIMAL(15,2) NOT NULL,
    capital DECIMAL(15,2) NOT NULL,
    interet DECIMAL(15,2) NOT NULL,
    capital_restant DECIMAL(15,2) NOT NULL,
    date_echeance DATE NOT NULL,
    date_paiement DATE,
    statut echeance_statut DEFAULT 'A_VENIR',
    
    -- Champs réservés pour les pénalités (à utiliser plus tard)
    penalite_appliquee DECIMAL(10,2) DEFAULT 0,
    jours_retard INTEGER DEFAULT 0,
    date_calcul_penalite DATE, -- Date à laquelle les pénalités ont été calculées
    
    FOREIGN KEY (id_pret) REFERENCES pret(id_pret),
    UNIQUE (id_pret, numero_echeance)
);

-- Enum pour le statut de l'échéance
CREATE TYPE echeance_statut AS ENUM (
    'A_VENIR',
    'ECHEANCE_AUJOURDHUI',
    'EN_RETARD',
    'PAYE',
    'PAYE_AVEC_RETARD' -- Statut prévu pour pénalités futures
);

-- Table des remboursements effectifs
CREATE TABLE remboursement (
    id_remboursement SERIAL PRIMARY KEY,
    id_echeance INTEGER NOT NULL,
    id_compte INTEGER,
    montant DECIMAL(15,2) NOT NULL,
    
    -- Détail du montant (pour séparer pénalités plus tard)
    montant_echeance DECIMAL(15,2) NOT NULL, -- Partie normale de l'échéance
    montant_penalite DECIMAL(10,2) DEFAULT 0, -- Partie pénalité (0 pour l'instant)
    
    date_paiement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type_paiement paiement_type DEFAULT 'VIREMENT',
    
    FOREIGN KEY (id_echeance) REFERENCES echeance(id_echeance),
    FOREIGN KEY (id_compte) REFERENCES compte_courant(id_compte)
);

-- Enum pour le type de paiement
CREATE TYPE paiement_type AS ENUM (
    'ESPECES',
    'VIREMENT',
    'PRELEVEMENT',
    'CARTE'
);

-- Table de configuration des pénalités (pour extension future)
CREATE TABLE parametres_penalites (
    id_parametre SERIAL PRIMARY KEY,
    type_penalite VARCHAR(50) NOT NULL, -- 'RETARD', 'DEFICIT', etc.
    taux_penalite DECIMAL(5,4) NOT NULL,
    delai_tolerance_jours INTEGER NOT NULL,
    montant_min_penalite DECIMAL(10,2),
    montant_max_penalite DECIMAL(10,2),
    actif BOOLEAN DEFAULT FALSE, -- Désactivé pour l'instant
    date_debut DATE,
    date_fin DATE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour améliorer les performances
CREATE INDEX idx_pret_client ON pret(id_client);
CREATE INDEX idx_pret_statut ON pret(statut);
CREATE INDEX idx_echeance_pret ON echeance(id_pret);
CREATE INDEX idx_echeance_date ON echeance(date_echeance);
CREATE INDEX idx_echeance_statut ON echeance(statut);
CREATE INDEX idx_remboursement_echeance ON remboursement(id_echeance);