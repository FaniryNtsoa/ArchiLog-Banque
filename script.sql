

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
CREATE TABLE type_compte (
    id_type INT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    taux_interet DECIMAL(5,2) NOT NULL,
    retrait_max_pourcentage DECIMAL(5,2) DEFAULT 50.00,
    calcul_interet ENUM('MENSUEL', 'TRIMESTRIEL') DEFAULT 'MENSUEL'
);

-- Table des comptes épargne
CREATE TABLE compte_epargne (
    id_compte INT AUTO_INCREMENT PRIMARY KEY,
    id_client INT NOT NULL,
    id_type INT NOT NULL,
    numero_compte VARCHAR(30) UNIQUE NOT NULL,
    solde DECIMAL(15,2) DEFAULT 0,
    plafond_depot DECIMAL(15,2) DEFAULT 10000000,
    date_ouverture DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_client) REFERENCES client(id_client),
    FOREIGN KEY (id_type) REFERENCES type_compte(id_type)
);

-- Table des opérations (dépôt, retrait)
CREATE TABLE operation_epargne (
    id_operation INT AUTO_INCREMENT PRIMARY KEY,
    id_compte INT NOT NULL,
    type_operation ENUM('DEPOT', 'RETRAIT') NOT NULL,
    montant DECIMAL(15,2) NOT NULL,
    date_operation DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_compte) REFERENCES compte_epargne(id_compte)
);

-- Table de calcul et historique des intérêts
CREATE TABLE interet_calcul (
    id_interet INT AUTO_INCREMENT PRIMARY KEY,
    id_compte INT NOT NULL,
    montant_interet DECIMAL(15,2) NOT NULL,
    periode_debut DATE,
    periode_fin DATE,
    date_calcul DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_compte) REFERENCES compte_epargne(id_compte)
);

