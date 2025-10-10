

-- ============================================
--   BASE 2 : Compte de Dépôt / Épargne
-- ============================================
CREATE DATABASE compte_epargne_db;
USE compte_epargne_db;

-- Table des clients (avec login)
CREATE TABLE client (
    id_client INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    telephone VARCHAR(20),
    adresse VARCHAR(255),
    mot_de_passe VARCHAR(255) NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
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

-- ============================================
--   BASE 3 : Prêts
-- ============================================
CREATE DATABASE pret_db;
USE pret_db;

-- Table des clients (avec login)
CREATE TABLE client (
    id_client INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    telephone VARCHAR(20),
    adresse VARCHAR(255),
    mot_de_passe VARCHAR(255) NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Table des types de prêts
CREATE TABLE type_pret (
    id_type INT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    taux_interet DECIMAL(5,2) NOT NULL,
    duree_max INT NOT NULL, -- en mois
    montant_max DECIMAL(15,2) NOT NULL
);

-- Table des prêts
CREATE TABLE pret (
    id_pret INT AUTO_INCREMENT PRIMARY KEY,
    id_client INT NOT NULL,
    id_type INT NOT NULL,
    montant DECIMAL(15,2) NOT NULL,
    duree INT NOT NULL,
    mensualite DECIMAL(15,2),
    statut ENUM('EN_ATTENTE', 'APPROUVE', 'REFUSE', 'EN_COURS', 'TERMINE') DEFAULT 'EN_ATTENTE',
    date_demande DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_approbation DATETIME,
    FOREIGN KEY (id_client) REFERENCES client(id_client),
    FOREIGN KEY (id_type) REFERENCES type_pret(id_type)
);

-- Table des pièces justificatives
CREATE TABLE piece_justificative (
    id_piece INT AUTO_INCREMENT PRIMARY KEY,
    id_pret INT NOT NULL,
    type_piece VARCHAR(100) NOT NULL,
    chemin_fichier VARCHAR(255),
    date_upload DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_pret) REFERENCES pret(id_pret)
);

-- Table de remboursement
CREATE TABLE remboursement (
    id_remboursement INT AUTO_INCREMENT PRIMARY KEY,
    id_pret INT NOT NULL,
    montant DECIMAL(15,2) NOT NULL,
    date_paiement DATETIME DEFAULT CURRENT_TIMESTAMP,
    type_paiement ENUM('ESPECES', 'VIREMENT', 'CARTE') DEFAULT 'VIREMENT',
    FOREIGN KEY (id_pret) REFERENCES pret(id_pret)
);

-- Table des échéances
CREATE TABLE echeance (
    id_echeance INT AUTO_INCREMENT PRIMARY KEY,
    id_pret INT NOT NULL,
    numero_echeance INT NOT NULL,
    montant_echeance DECIMAL(15,2) NOT NULL,
    date_echeance DATE NOT NULL,
    statut ENUM('PAYE', 'NON_PAYE') DEFAULT 'NON_PAYE',
    FOREIGN KEY (id_pret) REFERENCES pret(id_pret)
);
