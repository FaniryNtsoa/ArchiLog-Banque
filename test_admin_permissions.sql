-- Script de test pour insérer des données de démonstration
-- À exécuter APRÈS avoir démarré l'application pour avoir des comptes et types d'opération

-- ============================================
-- VÉRIFICATION DES DONNÉES
-- ============================================

-- Vérifier les directions
SELECT 'Directions disponibles:' as info;
SELECT * FROM direction;

-- Vérifier les utilisateurs
SELECT 'Utilisateurs créés:' as info;
SELECT u.id_utilisateur, u.login_utilisateur, u.role_utilisateur, 
       d.libelle_direction
FROM utilisateur u
LEFT JOIN direction d ON u.id_direction = d.id_direction;

-- Vérifier les permissions par rôle
SELECT 'Permissions par rôle:' as info;
SELECT 
    CASE ar.role_requis 
        WHEN 1 THEN 'Admin'
        WHEN 2 THEN 'Manager'
        WHEN 3 THEN 'Opérateur'
        WHEN 4 THEN 'Lecteur'
    END as role_nom,
    ar.nom_table,
    STRING_AGG(ar.action_autorisee, ', ' ORDER BY ar.action_autorisee) as actions
FROM action_role ar
GROUP BY ar.role_requis, ar.nom_table
ORDER BY ar.role_requis, ar.nom_table;

-- ============================================
-- COMMANDES DE TEST
-- ============================================

-- Pour tester les connexions:
-- 1. Admin (tous les droits):
--    Login: admin / Password: admin123
--    URL: http://localhost:8080/centralisateur/admin/login

-- 2. Manager (lecture + écriture, pas de suppression):
--    Login: manager / Password: manager123

-- 3. Opérateur (lecture + insertion):
--    Login: operateur / Password: oper123

-- 4. Lecteur (lecture seule):
--    Login: lecteur / Password: lecteur123

-- ============================================
-- AJOUT DE PERMISSIONS SUPPLÉMENTAIRES (optionnel)
-- ============================================

-- Exemple: Donner au Manager le droit de DELETE sur les mouvements
-- INSERT INTO action_role (nom_table, action_autorisee, role_requis) 
-- VALUES ('mouvement', 'DELETE', 2);

-- Exemple: Donner à l'Opérateur le droit UPDATE sur les mouvements
-- INSERT INTO action_role (nom_table, action_autorisee, role_requis) 
-- VALUES ('mouvement', 'UPDATE', 3);

-- ============================================
-- VÉRIFICATION DES COMPTES ET TYPES D'OPÉRATION
-- ============================================

-- Vérifier les comptes disponibles (pour créer des mouvements)
SELECT 'Comptes disponibles:' as info;
SELECT id_compte, numero_compte, solde 
FROM compte_courant 
LIMIT 5;

-- Vérifier les types d'opération (pour créer des mouvements)
SELECT 'Types d''opération disponibles:' as info;
SELECT id_type_operation, libelle_type_operation, nature_operation
FROM type_operation;

-- ============================================
-- REQUÊTES UTILES POUR LE DÉBOGAGE
-- ============================================

-- Voir toutes les permissions d'un utilisateur spécifique
-- Remplacer 'admin' par le login de l'utilisateur
SELECT 
    u.login_utilisateur,
    u.role_utilisateur,
    ar.nom_table,
    ar.action_autorisee
FROM utilisateur u
JOIN action_role ar ON u.role_utilisateur = ar.role_requis
WHERE u.login_utilisateur = 'admin'
ORDER BY ar.nom_table, ar.action_autorisee;

-- Compter les permissions par rôle
SELECT 
    role_requis,
    COUNT(*) as nombre_permissions
FROM action_role
GROUP BY role_requis
ORDER BY role_requis;

-- Vérifier les mouvements récents
SELECT 
    m.id_mouvement,
    cc.numero_compte,
    to2.libelle_type_operation,
    m.montant,
    m.date_operation,
    m.libelle_operation
FROM mouvement m
JOIN compte_courant cc ON m.id_compte = cc.id_compte
JOIN type_operation to2 ON m.id_type_operation = to2.id_type_operation
ORDER BY m.date_operation DESC
LIMIT 10;
