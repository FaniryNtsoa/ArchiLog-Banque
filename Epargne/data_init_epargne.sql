-- Script d'initialisation de la base de données Épargne
-- PostgreSQL

-- Créer la base de données si elle n'existe pas
-- Exécuter cette commande en étant connecté à PostgreSQL en tant qu'administrateur
-- CREATE DATABASE compte_epargne_db;

-- Se connecter à la base de données
\c compte_epargne_db;

-- Note: Les tables seront créées automatiquement par Entity Framework Core Migrations
-- Ce script peut être utilisé pour ajouter des données de test

-- Insertion de données de test (optionnel)

-- Exemple de client
-- Le mot de passe 'password123' est haché en SHA-256
INSERT INTO client (numero_client, nom, prenom, date_naissance, num_cin, email, telephone, adresse, code_postal, ville, profession, revenu_mensuel, solde_initial, situation_familiale, mot_de_passe, statut, date_creation, date_modification)
VALUES 
('CLI17293847560001', 'Dupont', 'Jean', '1990-01-01', 'CIN123456789', 'jean.dupont@email.com', '0612345678', '123 Rue Example', '75001', 'Paris', 'Ingénieur', 3000.00, 0.00, 'CELIBATAIRE', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ACTIF', NOW(), NOW()),
('CLI17293847560002', 'Martin', 'Sophie', '1985-05-15', 'CIN987654321', 'sophie.martin@email.com', '0687654321', '456 Avenue Test', '69001', 'Lyon', 'Professeur', 2500.00, 0.00, 'MARIE', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ACTIF', NOW(), NOW()),
('CLI17293847560003', 'Bernard', 'Pierre', '1978-12-20', 'CIN456789123', 'pierre.bernard@email.com', '0698765432', '789 Boulevard Demo', '33000', 'Bordeaux', 'Médecin', 5000.00, 0.00, 'DIVORCE', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ACTIF', NOW(), NOW());

-- Note: Les types de comptes épargne sont déjà insérés automatiquement par le seed data dans le DbContext

-- Afficher les tables créées
\dt

-- Afficher les données
SELECT * FROM client;
SELECT * FROM type_compte_epargne;

COMMIT;
