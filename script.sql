

-- ============================================
--   BASE 2 : Compte de Dépôt / Épargne
-- ============================================
CREATE DATABASE compte_epargne_db;
USE compte_epargne_db;

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
    charges_mensuelles DECIMAL(15,2) DEFAULT 0, -- AJOUTÉ : Pour calcul du taux d'endettement
    solde_initial DECIMAL(15,2) DEFAULT 0 NOT NULL,
    situation_familiale ENUM('CELIBATAIRE', 'MARIE', 'DIVORCE', 'VEUF'),
    mot_de_passe VARCHAR(255) NOT NULL,
    statut ENUM('ACTIF', 'SUSPENDU', 'FERME') DEFAULT 'ACTIF',
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table des types de comptes épargne
CREATE TABLE type_compte_epargne (
    id_type_compte SERIAL PRIMARY KEY,
    code_type VARCHAR(20) UNIQUE NOT NULL,
    libelle VARCHAR(100) NOT NULL,
    description TEXT,
    taux_interet_annuel DECIMAL(5,3) NOT NULL, -- Taux plus précis
    depot_initial_min DECIMAL(15,2) DEFAULT 0,
    solde_min_obligatoire DECIMAL(15,2) DEFAULT 0,
    plafond_depot DECIMAL(15,2) DEFAULT 10000000,
    retrait_max_pourcentage DECIMAL(5,2) DEFAULT 50.00, -- % du solde
    frais_tenue_compte DECIMAL(10,2) DEFAULT 0,
    periodicite_calcul_interet VARCHAR(20) DEFAULT 'MENSUEL', -- 'QUOTIDIEN', 'MENSUEL', 'TRIMESTRIEL'
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des comptes épargne
CREATE TABLE compte_epargne (
    id_compte SERIAL PRIMARY KEY,
    id_client INTEGER NOT NULL,
    id_type_compte INTEGER NOT NULL,
    numero_compte VARCHAR(30) UNIQUE NOT NULL,
    libelle_compte VARCHAR(100),
    solde DECIMAL(15,2) DEFAULT 0 NOT NULL,
    solde_disponible DECIMAL(15,2) DEFAULT 0 NOT NULL, -- Solde - retraits bloqués
    solde_min_historique DECIMAL(15,2) DEFAULT 0, -- Pour calcul des intérêts
    date_ouverture DATE NOT NULL DEFAULT CURRENT_DATE,
    date_dernier_calcul_interet DATE, -- Dernier calcul d'intérêts
    date_derniere_operation DATE, -- Dernière opération
    statut compte_statut DEFAULT 'ACTIF',
    motif_fermeture TEXT,
    date_fermeture DATE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_client) REFERENCES client(id_client),
    FOREIGN KEY (id_type_compte) REFERENCES type_compte_epargne(id_type_compte)
);

-- Enum pour le statut du compte
CREATE TYPE compte_statut AS ENUM (
    'ACTIF',
    'BLOQUE',
    'FERME',
    'SUSPENDU'
);

-- Table des opérations (dépôt, retrait)
CREATE TABLE operation_epargne (
    id_operation SERIAL PRIMARY KEY,
    id_compte INTEGER NOT NULL,
    type_operation operation_type NOT NULL,
    montant DECIMAL(15,2) NOT NULL,
    solde_avant DECIMAL(15,2) NOT NULL,
    solde_apres DECIMAL(15,2) NOT NULL,
    description VARCHAR(255),
    reference_operation VARCHAR(100),
    date_operation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_compte) REFERENCES compte_epargne(id_compte)
);

-- Enum pour le type d'opération
CREATE TYPE operation_type AS ENUM (
    'DEPOT',
    'RETRAIT',
    'VIREMENT_ENTRANT',
    'VIREMENT_SORTANT',
    'INTERETS_CAPITALISES'
);

-- Table de calcul et historique des intérêts
CREATE TABLE interet_epargne (
    id_interet SERIAL PRIMARY KEY,
    id_compte INTEGER NOT NULL,
    periode_debut DATE NOT NULL,
    periode_fin DATE NOT NULL,
    solde_moyen_periode DECIMAL(15,2) NOT NULL, -- Solde moyen pour la période
    taux_interet_applique DECIMAL(5,3) NOT NULL,
    jours_periode INTEGER NOT NULL, -- Nombre de jours dans la période
    interet_couru DECIMAL(15,2) NOT NULL,
    interet_net DECIMAL(15,2) NOT NULL, -- Après impôt si applicable
    date_calcul TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_credit_interet DATE, -- Date où les intérêts ont été crédités
    statut interet_statut DEFAULT 'CALCULE',
    
    FOREIGN KEY (id_compte) REFERENCES compte_epargne(id_compte)
);

-- Enum pour le statut des intérêts
CREATE TYPE interet_statut AS ENUM (
    'CALCULE',
    'CAPITALISE',
    'ANNULE'
);

-- Table des plafonds et restrictions
CREATE TABLE restriction_epargne (
    id_restriction SERIAL PRIMARY KEY,
    id_type_compte INTEGER NOT NULL,
    type_restriction VARCHAR(50) NOT NULL, -- 'RETRAIT_MAX', 'DEPOT_MIN', etc.
    valeur DECIMAL(15,2) NOT NULL,
    unite VARCHAR(20), -- 'EURO', 'POURCENTAGE'
    description TEXT,
    date_debut DATE NOT NULL,
    date_fin DATE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_type_compte) REFERENCES type_compte_epargne(id_type_compte)
);

-- Index pour performances
CREATE INDEX idx_compte_epargne_client ON compte_epargne(id_client);
CREATE INDEX idx_compte_epargne_type ON compte_epargne(id_type_compte);
CREATE INDEX idx_operation_compte ON operation_epargne(id_compte);
CREATE INDEX idx_operation_date ON operation_epargne(date_operation);
CREATE INDEX idx_interet_compte ON interet_epargne(id_compte);
CREATE INDEX idx_interet_periode ON interet_epargne(periode_debut, periode_fin);

