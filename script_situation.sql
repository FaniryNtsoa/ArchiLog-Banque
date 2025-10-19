-- ============================================
--   BASE 1 : Situation Bancaire (Compte Courant)
-- ============================================

CREATE DATABASE situation_bancaire_db;
USE situation_bancaire_db;

-- Table des clients
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

-- Table des taux d'intérêt annuels pour les découverts
CREATE TABLE taux_interet_decouvert (
    id_taux INT AUTO_INCREMENT PRIMARY KEY,
    taux_annuel DECIMAL(5,4) NOT NULL,
    description VARCHAR(255),
    date_debut DATE NOT NULL,
    date_fin DATE NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Table des paramètres de compte (avec historique)
CREATE TABLE parametres_compte (
    id_parametre INT AUTO_INCREMENT PRIMARY KEY,
    id_taux_decouvert INT NOT NULL,
    plafond_retrait_journalier DECIMAL(15,2) NOT NULL,
    plafond_virement_journalier DECIMAL(15,2) NOT NULL,
    montant_decouvert_autorise DECIMAL(15,2) DEFAULT 0,
    frais_tenue_compte DECIMAL(10,2) NOT NULL,
    frais_tenue_compte_periodicite ENUM('MENSUEL', 'TRIMESTRIEL', 'ANNUEL') DEFAULT 'MENSUEL',
    date_debut DATE NOT NULL,
    date_fin DATE NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_taux_decouvert) REFERENCES taux_interet_decouvert(id_taux)
);

-- Table des types de compte (étudiant, business, standard, etc.)
CREATE TABLE type_compte (
    id_type_compte INT AUTO_INCREMENT PRIMARY KEY,
    id_parametre_actuel INT NOT NULL, -- Paramètre actuel pour ce type
    code_type VARCHAR(20) UNIQUE NOT NULL,
    libelle VARCHAR(100) NOT NULL,
    description TEXT,
    FOREIGN KEY (id_parametre_actuel) REFERENCES parametres_compte(id_parametre)
);

-- Table principale des comptes courants
CREATE TABLE compte_courant (
    id_compte INT AUTO_INCREMENT PRIMARY KEY,
    id_client INT NOT NULL,
    id_type_compte INT NOT NULL,
    numero_compte VARCHAR(30) UNIQUE NOT NULL,
    libelle_compte VARCHAR(100),
    devise VARCHAR(3) DEFAULT 'XOF',
    statut ENUM('OUVERT', 'BLOQUE', 'FERME', 'SUSPENDU') DEFAULT 'OUVERT',
    date_ouverture DATE NOT NULL,
    date_fermeture DATE NULL,
    motif_fermeture VARCHAR(255),
    FOREIGN KEY (id_client) REFERENCES client(id_client),
    FOREIGN KEY (id_type_compte) REFERENCES type_compte(id_type_compte)
);

-- Table des types d'opérations
CREATE TABLE type_operation (
    id_type_operation INT AUTO_INCREMENT PRIMARY KEY,
    code_operation VARCHAR(10) UNIQUE NOT NULL,
    libelle_operation VARCHAR(100) NOT NULL,
    categorie ENUM('DEBIT', 'CREDIT') NOT NULL,
    nature ENUM('RETRAIT', 'DEPOT', 'VIREMENT', 'PRELEVEMENT', 'FRAIS', 'INTERET')
);

-- Table des mouvements bancaires
CREATE TABLE mouvement (
    id_mouvement INT AUTO_INCREMENT PRIMARY KEY,
    id_compte INT NOT NULL,
    id_type_operation INT NOT NULL,
    montant DECIMAL(15,2) NOT NULL,
    solde_avant_operation DECIMAL(15,2) NOT NULL,
    solde_apres_operation DECIMAL(15,2) NOT NULL,
    date_operation DATETIME DEFAULT CURRENT_TIMESTAMP,
    reference VARCHAR(100),
    libelle_operation VARCHAR(255),
    FOREIGN KEY (id_compte) REFERENCES compte_courant(id_compte),
    FOREIGN KEY (id_type_operation) REFERENCES type_operation(id_type_operation)
);

-- Table des virements entre comptes
CREATE TABLE virement (
    id_virement INT AUTO_INCREMENT PRIMARY KEY,
    id_mouvement_debit INT NOT NULL,
    id_mouvement_credit INT NOT NULL,
    montant DECIMAL(15,2) NOT NULL,
    date_virement DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_mouvement_debit) REFERENCES mouvement(id_mouvement),
    FOREIGN KEY (id_mouvement_credit) REFERENCES mouvement(id_mouvement)
);


-- Table des découverts avec gestion des intérêts
CREATE TABLE decouvert (
    id_decouvert INT AUTO_INCREMENT PRIMARY KEY,
    id_compte INT NOT NULL,
    montant_decouvert DECIMAL(15,2) NOT NULL,
    montant_autorise DECIMAL(15,2) NOT NULL,
    statut ENUM('ACTIF', 'REMBOURSE', 'FERME', 'SUSPENDU') DEFAULT 'ACTIF',
    date_debut DATE NOT NULL,
    date_fin DATE NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    motif_fermeture VARCHAR(255),
    FOREIGN KEY (id_compte) REFERENCES compte_courant(id_compte)
);

-- conserver le détail du calcul des intérêts
CREATE TABLE interet_journalier (
    id_interet INT AUTO_INCREMENT PRIMARY KEY,
    id_compte INT NOT NULL,
    id_decouvert INT NULL,
    date_jour DATE NOT NULL,
    montant_base DECIMAL(15,2) NOT NULL,
    taux_applique DECIMAL(5,4) NOT NULL,
    interet_jour DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (id_compte) REFERENCES compte_courant(id_compte),
    FOREIGN KEY (id_decouvert) REFERENCES decouvert(id_decouvert)
);

-- Pour garder une trace des frais de tenue de compte prélevés
CREATE TABLE frais_tenue_historique (
    id_frais INT AUTO_INCREMENT PRIMARY KEY,
    id_compte INT NOT NULL,
    montant_frais DECIMAL(15,2) NOT NULL,
    periode_debut DATE NOT NULL,
    periode_fin DATE NOT NULL,
    date_prelevement DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_compte) REFERENCES compte_courant(id_compte)
);

    -- Insertion des taux d'intérêt
INSERT INTO taux_interet_decouvert (taux_annuel, description, date_debut) VALUES
(0.05, 'Taux d''intérêt standard pour découvert autorisé', '2024-01-01'),
(0.08, 'Taux d''intérêt pour découvert non autorisé', '2024-01-01'),
(0.03, 'Taux préférentiel pour comptes premium', '2024-01-01');

-- Insertion des paramètres
INSERT INTO parametres_compte (id_taux_decouvert, plafond_retrait_journalier, plafond_virement_journalier, autorisation_decouvert, montant_decouvert_autorise, frais_tenue_compte, frais_tenue_compte_periodicite, date_debut) VALUES
(2, 500000, 2000000, 0, 0, 'MENSUEL', '2024-01-01'),  -- Paramètres étudiant
(1, 2000000, 5000000,  100000, 5000, 'MENSUEL', '2024-01-01'),  -- Paramètres standard
(1, 5000000, 20000000,  500000, 10000, 'MENSUEL', '2024-01-01'),  -- Paramètres business
(3, 10000000, 50000000,  1000000, 0, 'MENSUEL', '2024-01-01');  -- Paramètres premium

-- Insertion des types de compte
INSERT INTO type_compte (id_parametre_actuel, code_type, libelle, description) VALUES
(1, 'ETUDIANT', 'Compte Étudiant', 'Compte destiné aux étudiants avec conditions avantageuses'),
(2, 'STANDARD', 'Compte Standard', 'Compte courant classique'),
(3, 'BUSINESS', 'Compte Business', 'Compte professionnel avec plafonds élevés'),
(4, 'PREMIUM', 'Compte Premium', 'Compte haut de gamme avec services premium');

-- Insertion des types d'opérations
INSERT INTO type_operation (code_operation, libelle_operation, categorie, nature) VALUES
('DEP', 'Dépôt espèces', 'CREDIT', 'DEPOT'),
('RET', 'Retrait espèces', 'DEBIT', 'RETRAIT'),
('VIR_IN', 'Virement reçu', 'CREDIT', 'VIREMENT'),
('VIR_OUT', 'Virement émis', 'DEBIT', 'VIREMENT'),
('PRELEV', 'Prélèvement', 'DEBIT', 'PRELEVEMENT'),
('FRAIS', 'Frais bancaires', 'DEBIT', 'FRAIS'),
('INT_DB', 'Intérêts débiteurs', 'DEBIT', 'INTERET'),
('INT_CR', 'Intérêts créditeurs', 'CREDIT', 'INTERET');

-- Création des index
CREATE INDEX idx_mouvement_compte_date ON mouvement(id_compte, date_operation);
CREATE INDEX idx_mouvement_reference ON mouvement(reference);
CREATE INDEX idx_compte_courant_numero ON compte_courant(numero_compte);
CREATE INDEX idx_client_numero ON client(numero_client);
CREATE INDEX idx_parametres_dates ON parametres_compte(date_debut, date_fin);
CREATE INDEX idx_taux_dates ON taux_interet_decouvert(date_debut, date_fin);

-- Vue pour obtenir le solde actuel d'un compte
-- CREATE VIEW vue_solde_comptes AS
-- SELECT 
--     c.id_compte,
--     c.numero_compte,
--     c.id_client,
--     m.solde_apres_operation as solde_actuel,
--     m.date_operation as derniere_operation
-- FROM compte_courant c
-- INNER JOIN mouvement m ON m.id_mouvement = (
--     SELECT id_mouvement 
--     FROM mouvement m2 
--     WHERE m2.id_compte = c.id_compte 
--     ORDER BY m2.date_operation DESC, m2.id_mouvement DESC 
--     LIMIT 1
-- );