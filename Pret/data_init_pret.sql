-- ============================================
-- Script d'initialisation du module Prêt
-- Données de test pour le développement
-- ============================================

-- Insertion de types de prêts
INSERT INTO type_pret (code_type, libelle, taux_interet_annuel, duree_min, duree_max, montant_min, montant_max, frais_dossier, penalite_retard_taux, delai_tolerance_jours, actif) VALUES
('PERSO', 'Prêt Personnel', 0.0800, 12, 84, 100000, 5000000, 5000, 0.0200, 5, true),
('AUTO', 'Prêt Automobile', 0.0650, 12, 60, 500000, 10000000, 10000, 0.0200, 5, true),
('IMMO', 'Prêt Immobilier', 0.0400, 60, 300, 5000000, 50000000, 50000, 0.0200, 5, true),
('ETUDIANT', 'Prêt Étudiant', 0.0200, 12, 120, 50000, 2000000, 0, 0.0100, 10, true),
('CONSOMMATION', 'Crédit à la Consommation', 0.1000, 6, 48, 50000, 3000000, 2000, 0.0250, 5, true);

-- Insertion de clients de test (mot de passe: "password" hashé en SHA-256)
-- Hash SHA-256 de "password": 5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8
INSERT INTO client (numero_client, nom, prenom, date_naissance, num_cin, email, telephone, adresse, code_postal, ville, profession, revenu_mensuel, solde_initial, situation_familiale, mot_de_passe, statut) VALUES
('CLI001', 'Rakoto', 'Jean', '1985-05-15', '101234567890', 'jean.rakoto@email.com', '0340123456', 'Lot IVA 123 Bis', '101', 'Antananarivo', 'Ingénieur', 2500000, 0, 'MARIE', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'ACTIF'),
('CLI002', 'Razafy', 'Marie', '1990-08-20', '101234567891', 'marie.razafy@email.com', '0340123457', 'Lot II 456', '101', 'Antananarivo', 'Médecin', 3500000, 0, 'CELIBATAIRE', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'ACTIF'),
('CLI003', 'Randria', 'Paul', '1982-12-10', '101234567892', 'paul.randria@email.com', '0340123458', 'Lot III 789', '101', 'Antananarivo', 'Professeur', 1800000, 0, 'MARIE', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'ACTIF'),
('CLI004', 'Andrianina', 'Sophie', '1995-03-25', '101234567893', 'sophie.andrianina@email.com', '0340123459', 'Lot IV 321', '101', 'Antananarivo', 'Étudiante', 0, 0, 'CELIBATAIRE', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'ACTIF'),
('CLI005', 'Rasolofo', 'David', '1988-07-05', '101234567894', 'david.rasolofo@email.com', '0340123460', 'Lot V 654', '101', 'Antananarivo', 'Entrepreneur', 5000000, 0, 'MARIE', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'ACTIF');

-- Note: Les prêts seront créés via l'application après déploiement
-- Ceci permet de tester le workflow complet de demande et approbation
