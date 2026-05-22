-- ========================================
-- Schema Base de Donnees - Gestion des Notes
-- ========================================

CREATE DATABASE IF NOT EXISTS gestion_ecole 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE gestion_ecole;

-- Table: utilisateur (base)
CREATE TABLE IF NOT EXISTS utilisateur (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role ENUM('RESPONSABLE_PLANNING', 'RESPONSABLE_FILIERE', 'ENSEIGNANT') NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_login (login),
    INDEX idx_role (role)
) ENGINE=InnoDB;

-- Table: enseignant
CREATE TABLE IF NOT EXISTS enseignant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL,
    matricule VARCHAR(20) NOT NULL UNIQUE,
    specialite VARCHAR(100),
    grade ENUM('ASSISTANT', 'MAITRE_ASSISTANT', 'PROFESSEUR'),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_matricule (matricule)
) ENGINE=InnoDB;

-- Table: responsable_planning
CREATE TABLE IF NOT EXISTS responsable_planning (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL,
    matricule VARCHAR(20) NOT NULL UNIQUE,
    departement VARCHAR(100),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table: responsable_filiere
CREATE TABLE IF NOT EXISTS responsable_filiere (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL,
    matricule VARCHAR(20) NOT NULL UNIQUE,
    filiere_id BIGINT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table: filiere
CREATE TABLE IF NOT EXISTS filiere (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    intitule VARCHAR(200) NOT NULL,
    domaine VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Table: promotion
CREATE TABLE IF NOT EXISTS promotion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    intitule VARCHAR(100) NOT NULL,
    annee INT NOT NULL,
    filiere_id BIGINT NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (filiere_id) REFERENCES filiere(id) ON DELETE RESTRICT,
    INDEX idx_annee (annee),
    INDEX idx_filiere (filiere_id)
) ENGINE=InnoDB;

-- Table: etudiant
CREATE TABLE IF NOT EXISTS etudiant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cne VARCHAR(20) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    date_naissance DATE,
    email VARCHAR(100),
    telephone VARCHAR(20),
    statut ENUM('ACTIF', 'ARCHIVE', 'SUSPENDU') DEFAULT 'ACTIF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cne (cne),
    INDEX idx_statut (statut)
) ENGINE=InnoDB;

-- Table: inscription (lien etudiant-promotion)
CREATE TABLE IF NOT EXISTS inscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    etudiant_id BIGINT NOT NULL,
    promotion_id BIGINT NOT NULL,
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_inscription (etudiant_id, promotion_id),
    FOREIGN KEY (etudiant_id) REFERENCES etudiant(id) ON DELETE CASCADE,
    FOREIGN KEY (promotion_id) REFERENCES promotion(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Table: module
CREATE TABLE IF NOT EXISTS module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    intitule VARCHAR(200) NOT NULL,
    coefficient DOUBLE DEFAULT 1.0,
    promotion_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (promotion_id) REFERENCES promotion(id) ON DELETE CASCADE,
    UNIQUE KEY unique_module_promotion (code, promotion_id)
) ENGINE=InnoDB;

-- Table: sous_module
CREATE TABLE IF NOT EXISTS sous_module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    intitule VARCHAR(200) NOT NULL,
    coefficient DOUBLE DEFAULT 1.0,
    module_id BIGINT NOT NULL,
    enseignant_id BIGINT,
    FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE CASCADE,
    FOREIGN KEY (enseignant_id) REFERENCES enseignant(id) ON DELETE SET NULL,
    UNIQUE KEY unique_sous_module (code, module_id)
) ENGINE=InnoDB;

-- Table: note
CREATE TABLE IF NOT EXISTS note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    valeur DOUBLE NOT NULL CHECK (valeur BETWEEN 0 AND 20),
    type_note ENUM('EXAMEN', 'TP', 'CONTROLE_CONTINU', 'PROJET') DEFAULT 'EXAMEN',
    etudiant_id BIGINT NOT NULL,
    sous_module_id BIGINT NOT NULL,
    saisi_par BIGINT,
    date_saisie TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    validee BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (etudiant_id) REFERENCES etudiant(id) ON DELETE CASCADE,
    FOREIGN KEY (sous_module_id) REFERENCES sous_module(id) ON DELETE CASCADE,
    FOREIGN KEY (saisi_par) REFERENCES enseignant(id) ON DELETE SET NULL,
    UNIQUE KEY unique_note (etudiant_id, sous_module_id, type_note)
) ENGINE=InnoDB;

-- ========================================
-- Vues pour les statistiques
-- ========================================

CREATE OR REPLACE VIEW v_moyenne_sous_module AS
SELECT 
    n.etudiant_id,
    sm.id as sous_module_id,
    sm.intitule as sous_module_intitule,
    m.id as module_id,
    m.intitule as module_intitule,
    AVG(n.valeur) as moyenne_sous_module,
    sm.coefficient
FROM note n
JOIN sous_module sm ON n.sous_module_id = sm.id
JOIN module m ON sm.module_id = m.id
WHERE n.validee = TRUE
GROUP BY n.etudiant_id, sm.id, m.id;

CREATE OR REPLACE VIEW v_moyenne_module AS
SELECT 
    v.etudiant_id,
    v.module_id,
    v.module_intitule,
    SUM(v.moyenne_sous_module * v.coefficient) / SUM(v.coefficient) as moyenne_module,
    m.coefficient as module_coefficient
FROM v_moyenne_sous_module v
JOIN module m ON v.module_id = m.id
GROUP BY v.etudiant_id, v.module_id;

CREATE OR REPLACE VIEW v_moyenne_generale AS
SELECT 
    v.etudiant_id,
    e.cne,
    CONCAT(e.nom, ' ', e.prenom) as nom_complet,
    SUM(v.moyenne_module * v.module_coefficient) / SUM(v.module_coefficient) as moyenne_generale
FROM v_moyenne_module v
JOIN etudiant e ON v.etudiant_id = e.id
GROUP BY v.etudiant_id, e.cne, e.nom, e.prenom;

-- ========================================
-- Donnees de test (optionnel)
-- ========================================

-- Utilisateur admin (mot de passe: admin123 - hash avec BCrypt)
-- INSERT INTO utilisateur (login, mot_de_passe, nom, prenom, email, role, actif) 
-- VALUES ('admin', '$2a$12$...', 'Admin', 'System', 'admin@ecole.ma', 'RESPONSABLE_PLANNING', TRUE);
