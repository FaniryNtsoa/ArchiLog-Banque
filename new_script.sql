
-- Table direction
CREATE TABLE direction (
    id_direction INT AUTO_INCREMENT PRIMARY KEY,
    niveau INT NOT NULL
);

-- Table utilisateur
CREATE TABLE utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    login_utilisateur VARCHAR(50) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    id_direction INT,
    role_utilisateur INT NOT NULL,
    FOREIGN KEY (id_direction) REFERENCES direction(id_direction)
);

-- Table action_role
CREATE TABLE action_role (
    id_action_role INT AUTO_INCREMENT PRIMARY KEY,
    nom_table VARCHAR(50) NOT NULL,
    action_autorisee VARCHAR(50) NOT NULL,
    role_requis INT NOT NULL
);
