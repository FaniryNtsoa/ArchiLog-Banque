-- situation bancaire
-- Insertion des taux d'intérêt découvert
INSERT INTO taux_interet_decouvert (id_taux, taux_annuel, description, date_debut, date_fin, date_creation) VALUES
(1, 0.1200, 'Taux standard pour découvert autorisé', '2024-01-01', NULL, '2024-01-01 10:00:00'),
(2, 0.0850, 'Taux préférentiel pour comptes premium', '2024-01-01', NULL, '2024-01-01 10:15:00'),
(3, 0.1500, 'Taux étudiant avec conditions avantageuses', '2024-01-01', NULL, '2024-01-01 10:30:00'),
(4, 0.0950, 'Taux business pour professionnels', '2024-01-01', NULL, '2024-01-01 10:45:00'),
(5, 0.1800, 'Taux historique ancienne période', '2023-01-01', '2023-12-31', '2023-01-01 09:00:00');

-- Insertion des paramètres de compte
INSERT INTO parametres_compte (id_parametre, id_taux_decouvert, plafond_retrait_journalier, plafond_virement_journalier, montant_decouvert_autorise, frais_tenue_compte, frais_tenue_compte_periodicite, date_debut, date_fin, date_creation) VALUES
(1, 1, 1000.00, 5000.00, 500.00, 5.00, 'MENSUEL', '2024-01-01', NULL, '2024-01-01 11:00:00'),
(2, 2, 3000.00, 20000.00, 2000.00, 15.00, 'MENSUEL', '2024-01-01', NULL, '2024-01-01 11:15:00'),
(3, 3, 500.00, 2000.00, 100.00, 0.00, 'MENSUEL', '2024-01-01', NULL, '2024-01-01 11:30:00'),
(4, 4, 5000.00, 50000.00, 5000.00, 25.00, 'MENSUEL', '2024-01-01', NULL, '2024-01-01 11:45:00'),
(5, 1, 800.00, 3000.00, 300.00, 2.50, 'TRIMESTRIEL', '2024-01-01', NULL, '2024-01-01 12:00:00'),
(6, 2, 2000.00, 10000.00, 1000.00, 8.00, 'MENSUEL', '2024-01-01', '2024-06-30', '2024-01-01 12:15:00');

-- Insertion des types de compte
INSERT INTO type_compte (id_type_compte, id_parametre_actuel, code_type, libelle, description) VALUES
(1, 1, 'STANDARD', 'Compte Standard', 'Compte courant standard avec conditions classiques'),
(2, 2, 'PREMIUM', 'Compte Premium', 'Compte premium avec avantages exclusifs et conditions privilegiees'),
(3, 3, 'ETUDIANT', 'Compte Etudiant', 'Compte specialement concu pour les etudiants avec frais reduits'),
(4, 4, 'BUSINESS', 'Compte Business', 'Compte professionnel pour les entreprises et independants'),
(5, 5, 'ECO', 'Compte Economique', 'Compte economique avec frais reduits'),
(6, 6, 'SILVER', 'Compte Silver', 'Ancienne offre silver remplacee par le compte premium');