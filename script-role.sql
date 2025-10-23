CREATE TABLE role (
    id_role SERIAL PRIMARY KEY,
    nom_role VARCHAR(50) NOT NULL,
    description TEXT
);

INSERT INTO role (nom_role, description) VALUES
('Admin', 'Has full access to all system operations and configuration.'),
('Manager', 'Can approve, modify, and oversee virements and mouvements.'),
('Clerk', 'Can create and view virements and mouvements but cannot delete or approve them.'),
('Auditor', 'Has read-only access and can export data for audit purposes.');


ALTER TABLE utilisateur
ADD CONSTRAINT fk_utilisateur_role
FOREIGN KEY (role_utilisateur)
REFERENCES role (id_role)
ON UPDATE CASCADE
ON DELETE RESTRICT;

INSERT INTO utilisateur (login_utilisateur, mot_de_passe, role_utilisateur, id_direction) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 1, NULL), -- password: admin123
('manager', '6f04f55125f8dca03d91e39c4b9c3d4d5c98c77e8e3b8cb7ed23bdbfea27e3c2', 2, NULL), -- password: manager123
('clerk', '2c583c2a17650e9f2d739ba65c5a0c08db6e932f287efdeba94f91f18f62f7e0', 3, NULL),  -- password: clerk123
('auditor', '28d609f61ef278d1fc1781e4a93cf1fa4ffb1d80d9204de43a0f2a4a9011c63e', 4, NULL); -- password: auditor123



INSERT INTO public.action_role (action_autorisee, nom_table, role_requis) VALUES
('CREATE', 'virement', 3),  -- Clerk can create
('READ',   'virement', 3),  -- Clerk can read
('UPDATE', 'virement', 2),  -- Manager can update
('DELETE', 'virement', 1),  -- Admin can delete
('CREATE', 'mouvement', 3),  -- Clerk can create
('READ',   'mouvement', 3),  -- Clerk can read
('UPDATE', 'mouvement', 2),  -- Manager can update
('DELETE', 'mouvement', 1);  -- Admin can delete
