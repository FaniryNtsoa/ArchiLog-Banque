-- Script d'initialisation pour le système d'administration
-- PostgreSQL version

-- Table direction
CREATE TABLE IF NOT EXISTS direction (
    id_direction SERIAL PRIMARY KEY,
    niveau INTEGER NOT NULL
);

-- Table utilisateur
CREATE TABLE IF NOT EXISTS utilisateur (
    id_utilisateur SERIAL PRIMARY KEY,
    login_utilisateur VARCHAR(50) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    id_direction INTEGER,
    role_utilisateur INTEGER NOT NULL,
    FOREIGN KEY (id_direction) REFERENCES direction(id_direction)
);

-- Index pour utilisateur
CREATE INDEX IF NOT EXISTS idx_utilisateur_login ON utilisateur(login_utilisateur);
CREATE INDEX IF NOT EXISTS idx_utilisateur_role ON utilisateur(role_utilisateur);

-- Table action_role
CREATE TABLE IF NOT EXISTS action_role (
    id_action_role SERIAL PRIMARY KEY,
    nom_table VARCHAR(50) NOT NULL,
    action_autorisee VARCHAR(50) NOT NULL,
    role_requis INTEGER NOT NULL
);

-- Index pour action_role
CREATE INDEX IF NOT EXISTS idx_action_role_role_table ON action_role(role_requis, nom_table);
CREATE INDEX IF NOT EXISTS idx_action_role_table_action ON action_role(nom_table, action_autorisee);

-- ============================================
-- DONNÉES DE TEST
-- ============================================

-- Insertion des directions
INSERT INTO direction (niveau) VALUES
    (1),
    (2),
    (3),
    (4)
ON CONFLICT DO NOTHING;

-- Insertion des utilisateurs
-- Rôles: 1 = Admin, 2 = Manager, 3 = Opérateur, 4 = Lecteur
INSERT INTO utilisateur (login_utilisateur, mot_de_passe, id_direction, role_utilisateur) VALUES
    ('admin', 'admin123', 1, 1),
    ('manager', 'manager123', 2, 2),
    ('operateur', 'oper123', 3, 3),
    ('lecteur', 'lecteur123', 4, 4)
ON CONFLICT (login_utilisateur) DO NOTHING;

-- Insertion des permissions
-- Actions: SELECT, INSERT, UPDATE, DELETE

-- Permissions pour l'Admin (role 1) - Tous les droits sur toutes les tables
INSERT INTO action_role (nom_table, action_autorisee, role_requis) VALUES
    ('mouvement', 'SELECT', 1),
    ('mouvement', 'INSERT', 1),
    ('mouvement', 'UPDATE', 1),
    ('mouvement', 'DELETE', 1),
    ('compte_courant', 'SELECT', 1),
    ('compte_courant', 'INSERT', 1),
    ('compte_courant', 'UPDATE', 1),
    ('compte_courant', 'DELETE', 1),
    ('client', 'SELECT', 1),
    ('client', 'INSERT', 1),
    ('client', 'UPDATE', 1),
    ('client', 'DELETE', 1)
ON CONFLICT DO NOTHING;

-- Permissions pour le Manager (role 2) - SELECT, INSERT, UPDATE (pas DELETE)
INSERT INTO action_role (nom_table, action_autorisee, role_requis) VALUES
    ('mouvement', 'SELECT', 2),
    ('mouvement', 'INSERT', 2),
    ('mouvement', 'UPDATE', 2),
    ('compte_courant', 'SELECT', 2),
    ('compte_courant', 'INSERT', 2),
    ('compte_courant', 'UPDATE', 2),
    ('client', 'SELECT', 2),
    ('client', 'INSERT', 2),
    ('client', 'UPDATE', 2)
ON CONFLICT DO NOTHING;

-- Permissions pour l'Opérateur (role 3) - SELECT et INSERT uniquement
INSERT INTO action_role (nom_table, action_autorisee, role_requis) VALUES
    ('mouvement', 'SELECT', 3),
    ('mouvement', 'INSERT', 3),
    ('compte_courant', 'SELECT', 3),
    ('client', 'SELECT', 3)
ON CONFLICT DO NOTHING;

-- Permissions pour le Lecteur (role 4) - SELECT uniquement
INSERT INTO action_role (nom_table, action_autorisee, role_requis) VALUES
    ('mouvement', 'SELECT', 4),
    ('compte_courant', 'SELECT', 4),
    ('client', 'SELECT', 4)
ON CONFLICT DO NOTHING;

-- Afficher les données insérées
SELECT 'Directions créées:' as info;
SELECT * FROM direction;

SELECT 'Utilisateurs créés:' as info;
SELECT id_utilisateur, login_utilisateur, role_utilisateur, id_direction FROM utilisateur;

SELECT 'Permissions configurées:' as info;
SELECT ar.nom_table, ar.action_autorisee, ar.role_requis, 
       CASE ar.role_requis 
           WHEN 1 THEN 'Admin'
           WHEN 2 THEN 'Manager'
           WHEN 3 THEN 'Opérateur'
           WHEN 4 THEN 'Lecteur'
       END as role_nom
FROM action_role ar
ORDER BY ar.role_requis, ar.nom_table, ar.action_autorisee;
