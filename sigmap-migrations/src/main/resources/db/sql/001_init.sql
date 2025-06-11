-- liquibase formatted sql

-- changeset sigmap:1749671959050-1
CREATE SEQUENCE agpm_id_seq INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775806 START WITH 1;

-- changeset sigmap:1749671959050-2
CREATE SEQUENCE dac_id_seq INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775806 START WITH 1;

-- changeset sigmap:1749671959050-3
CREATE SEQUENCE dyna_docs_id_seq INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775806 START WITH 1;

-- changeset sigmap:1749671959050-4
CREATE SEQUENCE dyna_fields_details_id_seq INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775806 START WITH 1;

-- changeset sigmap:1749671959050-5
CREATE SEQUENCE dyna_fields_id_seq INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775806 START WITH 1;

-- changeset sigmap:1749671959050-6
CREATE SEQUENCE lot_id_seq INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775806 START WITH 1;

-- changeset sigmap:1749671959050-7
CREATE SEQUENCE paa_id_seq INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775806 START WITH 1;

-- changeset sigmap:1749671959050-8
CREATE SEQUENCE ppm_id_seq INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775806 START WITH 1;

-- changeset sigmap:1749671959050-9
CREATE SEQUENCE realisation_id_seq INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775806 START WITH 1;

-- changeset sigmap:1749671959050-10
CREATE TABLE ac (ac_id INT AUTO_INCREMENT NOT NULL, nom VARCHAR(255) NULL, ancrage VARCHAR(100) NULL, contact VARCHAR(255) NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, tutel_id INT DEFAULT NULL NULL, CONSTRAINT PK_AC PRIMARY KEY (ac_id));

-- changeset sigmap:1749671959050-11
CREATE TABLE agpm (agpm_id INT AUTO_INCREMENT NOT NULL, agpm_ver INT NOT NULL, ref VARCHAR(100) NULL, nom VARCHAR(255) NULL, contenu LONGTEXT NULL, ac_id INT DEFAULT NULL NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, remarque VARCHAR(255) NULL, process_instance_id VARCHAR(255) NULL, status ENUM('BROUILLON', 'EN_TRAITEMENT', 'PUBLIE', 'VALIDE') DEFAULT 'BROUILLON' NOT NULL, CONSTRAINT PK_AGPM PRIMARY KEY (agpm_id, agpm_ver));

-- changeset sigmap:1749671959050-12
CREATE TABLE agpm_docs (agpm_id INT NOT NULL, agpm_ver INT NOT NULL, doc_id INT NOT NULL, CONSTRAINT PK_AGPM_DOCS PRIMARY KEY (agpm_id, agpm_ver, doc_id));

-- changeset sigmap:1749671959050-13
CREATE TABLE authority (authority_code VARCHAR(50) NOT NULL, CONSTRAINT PK_AUTHORITY PRIMARY KEY (authority_code));

-- changeset sigmap:1749671959050-14
CREATE TABLE com (com_id INT AUTO_INCREMENT NOT NULL, nom VARCHAR(100) NULL, decret_creation VARCHAR(100) NULL, responsable VARCHAR(100) NULL, special TINYINT(1) DEFAULT 0 NULL, ancrage VARCHAR(100) NULL, contact VARCHAR(255) NULL, adresse TEXT NULL, com_type_id INT DEFAULT NULL NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_COM PRIMARY KEY (com_id));

-- changeset sigmap:1749671959050-15
CREATE TABLE compte_bancaire (compte_bancaire_id INT AUTO_INCREMENT NOT NULL, nom VARCHAR(255) NULL, agence VARCHAR(255) NULL, rib VARCHAR(255) NULL, pays_id INT DEFAULT NULL NULL, local TINYINT(1) DEFAULT NULL NULL, societe_id INT DEFAULT NULL NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_COMPTE_BANCAIRE PRIMARY KEY (compte_bancaire_id));

-- changeset sigmap:1749671959050-16
CREATE TABLE contact (contact_id INT AUTO_INCREMENT NOT NULL, nom VARCHAR(100) NULL, address TEXT NULL, tel1 VARCHAR(255) NULL, tel2 VARCHAR(255) NULL, email VARCHAR(255) NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_CONTACT PRIMARY KEY (contact_id));

-- changeset sigmap:1749671959050-17
CREATE TABLE dac (dac_id INT AUTO_INCREMENT NOT NULL, dac_ver INT NOT NULL, ppm_id INT DEFAULT NULL NULL, ppm_ver INT DEFAULT NULL NULL, category_id INT DEFAULT NULL NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, status ENUM('BROUILLON', 'EN_TRAITEMENT', 'PUBLIE', 'VALIDE') DEFAULT 'BROUILLON' NOT NULL, CONSTRAINT PK_DAC PRIMARY KEY (dac_id, dac_ver));

-- changeset sigmap:1749671959050-18
CREATE TABLE dac_docs (dac_id INT NOT NULL, dac_ver INT NOT NULL, dyna_docs_id INT NOT NULL, dyna_docs_ver INT NOT NULL, dyna_fields_id INT NOT NULL, source_id VARCHAR(255) NULL, field_value VARCHAR(255) NULL, CONSTRAINT PK_DAC_DOCS PRIMARY KEY (dac_id, dac_ver, dyna_docs_id, dyna_docs_ver, dyna_fields_id));

-- changeset sigmap:1749671959050-19
CREATE TABLE dac_docs_details (dac_id INT NOT NULL, dac_ver INT NOT NULL, dyna_docs_id INT NOT NULL, dyna_docs_ver INT NOT NULL, dyna_fields_id INT NOT NULL, dyna_fields_details_id INT NOT NULL, row_id INT NOT NULL, source_id VARCHAR(255) NULL, field_value VARCHAR(255) NULL, CONSTRAINT PK_DAC_DOCS_DETAILS PRIMARY KEY (dac_id, dac_ver, dyna_docs_id, dyna_docs_ver, dyna_fields_id, dyna_fields_details_id, row_id));

-- changeset sigmap:1749671959050-20
CREATE TABLE docs (doc_id INT AUTO_INCREMENT NOT NULL, name VARCHAR(255) NULL, alfresco_id VARCHAR(255) NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, file_name_alfresco VARCHAR(255) NULL, path_folder TINYTEXT NULL, file_size VARCHAR(255) NULL, content_type VARCHAR(255) NULL, file_extension VARCHAR(255) NULL, CONSTRAINT PK_DOCS PRIMARY KEY (doc_id));

-- changeset sigmap:1749671959050-21
CREATE TABLE dyna_docs (dyna_docs_id INT AUTO_INCREMENT NOT NULL, dyna_docs_ver INT NOT NULL, name VARCHAR(100) NULL, template_file_name VARCHAR(100) NULL, guide_file_name VARCHAR(100) NULL, category_id INT DEFAULT NULL NULL, dyna_doc_order INT DEFAULT NULL NULL, status ENUM('ACTIF', 'NON_ACTIF') DEFAULT 'ACTIF' NOT NULL, CONSTRAINT PK_DYNA_DOCS PRIMARY KEY (dyna_docs_id, dyna_docs_ver));

-- changeset sigmap:1749671959050-22
CREATE TABLE dyna_fields (dyna_fields_id INT AUTO_INCREMENT NOT NULL, dyna_docs_id INT NOT NULL, dyna_docs_ver INT NOT NULL, name VARCHAR(100) NULL, ref VARCHAR(100) NULL, label VARCHAR(100) NULL, `description` VARCHAR(255) NULL, source VARCHAR(255) NULL, source_field VARCHAR(255) NULL, dyna_fields_order INT DEFAULT NULL NULL, status ENUM('ACTIF', 'NON_ACTIF') DEFAULT 'ACTIF' NOT NULL, field_type ENUM('INTEGER', 'TEXT', 'DECIMAL', 'DATE', 'LIST', 'TABLE', 'SYSTEM') NOT NULL, CONSTRAINT PK_DYNA_FIELDS PRIMARY KEY (dyna_fields_id, dyna_docs_id, dyna_docs_ver));

-- changeset sigmap:1749671959050-23
CREATE TABLE dyna_fields_details (dyna_fields_details_id INT AUTO_INCREMENT NOT NULL, column_name VARCHAR(100) NULL, column_pos INT NOT NULL, source VARCHAR(255) NULL, source_field VARCHAR(255) NULL, dyna_fields_id INT NOT NULL, dyna_docs_id INT NOT NULL, dyna_docs_ver INT NOT NULL, field_type ENUM('INTEGER', 'TEXT', 'DECIMAL', 'DATE', 'LIST', 'TABLE', 'SYSTEM') NOT NULL, CONSTRAINT PK_DYNA_FIELDS_DETAILS PRIMARY KEY (dyna_fields_details_id, dyna_fields_id, dyna_docs_id, dyna_docs_ver));

-- changeset sigmap:1749671959050-24
CREATE TABLE lots (lots_id INT AUTO_INCREMENT NOT NULL, lots_ver INT NOT NULL, `description` VARCHAR(100) NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_LOTS PRIMARY KEY (lots_id, lots_ver));

-- changeset sigmap:1749671959050-25
CREATE TABLE lots_realisation (realisation_id INT NOT NULL, realisation_ver INT NOT NULL, lots_id INT NOT NULL, lots_ver INT NOT NULL, CONSTRAINT PK_LOTS_REALISATION PRIMARY KEY (realisation_id, realisation_ver, lots_id, lots_ver));

-- changeset sigmap:1749671959050-26
CREATE TABLE menu_item (id INT AUTO_INCREMENT NOT NULL, label VARCHAR(100) NOT NULL, icon VARCHAR(50) NULL, route VARCHAR(200) NULL, order_index INT DEFAULT NULL NULL, parent_id INT DEFAULT NULL NULL, authority_code VARCHAR(36) NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_MENU_ITEM PRIMARY KEY (id));

-- changeset sigmap:1749671959050-27
CREATE TABLE paa (paa_id INT AUTO_INCREMENT NOT NULL, paa_ver INT NOT NULL, ref VARCHAR(100) NULL, nom VARCHAR(255) NULL, annee VARCHAR(4) NULL, ac_id INT DEFAULT NULL NULL, budget_estime DECIMAL(10, 2) DEFAULT NULL NULL COMMENT 'Budget estimé (en MRU)', created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, process_instance_id VARCHAR(255) NULL, mode_passation_id INT DEFAULT NULL NULL, remarque VARCHAR(255) NULL, status ENUM('BROUILLON', 'EN_TRAITEMENT', 'PUBLIE', 'VALIDE') DEFAULT 'BROUILLON' NOT NULL, CONSTRAINT PK_PAA PRIMARY KEY (paa_id, paa_ver));

-- changeset sigmap:1749671959050-28
CREATE TABLE paa_docs (paa_id INT NOT NULL, paa_ver INT NOT NULL, doc_id INT NOT NULL, CONSTRAINT PK_PAA_DOCS PRIMARY KEY (paa_id, paa_ver, doc_id));

-- changeset sigmap:1749671959050-29
CREATE TABLE paa_realisation (paa_id INT NOT NULL, paa_ver INT NOT NULL, realisation_id INT NOT NULL, realisation_ver INT NOT NULL, CONSTRAINT PK_PAA_REALISATION PRIMARY KEY (paa_id, paa_ver, realisation_id, realisation_ver));

-- changeset sigmap:1749671959050-30
CREATE TABLE ppm (ppm_id INT AUTO_INCREMENT NOT NULL, ppm_ver INT NOT NULL, ref VARCHAR(100) NULL, nom VARCHAR(255) NULL, annee VARCHAR(4) NULL, ac_id INT DEFAULT NULL NULL, budget_estime DECIMAL(10, 2) DEFAULT NULL NULL COMMENT 'Budget estimé (en MRU)', created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, process_instance_id VARCHAR(255) NULL, mode_passation_id INT DEFAULT NULL NULL, remarque VARCHAR(255) NULL, status ENUM('BROUILLON', 'EN_TRAITEMENT', 'PUBLIE', 'VALIDE') DEFAULT 'BROUILLON' NOT NULL, CONSTRAINT PK_PPM PRIMARY KEY (ppm_id, ppm_ver));

-- changeset sigmap:1749671959050-31
CREATE TABLE ppm_com (com_id INT NOT NULL, ppm_id INT NOT NULL, ppm_ver INT NOT NULL, CONSTRAINT PK_PPM_COM PRIMARY KEY (com_id, ppm_id, ppm_ver));

-- changeset sigmap:1749671959050-32
CREATE TABLE ppm_docs (ppm_id INT NOT NULL, ppm_ver INT NOT NULL, doc_id INT NOT NULL, CONSTRAINT PK_PPM_DOCS PRIMARY KEY (ppm_id, ppm_ver, doc_id));

-- changeset sigmap:1749671959050-33
CREATE TABLE ppm_realisation (ppm_id INT NOT NULL, ppm_ver INT NOT NULL, realisation_id INT NOT NULL, realisation_ver INT NOT NULL, CONSTRAINT PK_PPM_REALISATION PRIMARY KEY (ppm_id, ppm_ver, realisation_id, realisation_ver));

-- changeset sigmap:1749671959050-34
CREATE TABLE realisation (realisation_id INT AUTO_INCREMENT NOT NULL, realisation_ver INT NOT NULL, realisation_envisagees VARCHAR(100) NULL, service_id INT DEFAULT NULL NULL, direction_id INT DEFAULT NULL NULL, source_financement_id INT DEFAULT NULL NULL, monais_id INT DEFAULT NULL NULL, type_marcher_id INT DEFAULT NULL NULL, mode_selection_id INT DEFAULT NULL NULL, date_prev_lps date DEFAULT NULL NULL COMMENT 'Date prévue de lancement de la procédure de sélection', date_prev_ac date DEFAULT NULL NULL COMMENT 'Date prévue d’attribution du contrat', date_prev_dp date DEFAULT NULL NULL COMMENT 'Date prévue de démarrage des prestations', date_prev_ap date DEFAULT NULL NULL COMMENT 'Date prévue d’achèvement des prestations', budget_estime DECIMAL(10, 2) DEFAULT NULL NULL COMMENT 'Budget estimé (en MRU)', is_published TINYINT(1) DEFAULT 0 NULL, published_at TINYINT(1) DEFAULT NULL NULL, sort INT DEFAULT NULL NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_REALISATION PRIMARY KEY (realisation_id, realisation_ver));

-- changeset sigmap:1749671959050-35
CREATE TABLE realisation_docs (realisation_id INT NOT NULL, realisation_ver INT NOT NULL, doc_id INT NOT NULL, CONSTRAINT PK_REALISATION_DOCS PRIMARY KEY (realisation_id, realisation_ver, doc_id));

-- changeset sigmap:1749671959050-36
CREATE TABLE ref_param (ref_param_id INT AUTO_INCREMENT NOT NULL, code VARCHAR(20) NOT NULL, `description` VARCHAR(50) NOT NULL, ref_type_id INT NOT NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, code_color VARCHAR(255) NULL, CONSTRAINT PK_REF_PARAM PRIMARY KEY (ref_param_id));

-- changeset sigmap:1749671959050-37
CREATE TABLE ref_type (ref_type_id INT AUTO_INCREMENT NOT NULL, `description` VARCHAR(50) NOT NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, ref_code VARCHAR(255) NOT NULL, system_type TINYINT(1) NOT NULL, CONSTRAINT PK_REF_TYPE PRIMARY KEY (ref_type_id), UNIQUE (ref_code));

-- changeset sigmap:1749671959050-38
CREATE TABLE societe (societe_id INT AUTO_INCREMENT NOT NULL, nom VARCHAR(255) NULL, type_id INT DEFAULT NULL NULL, pays_id INT DEFAULT NULL NULL, nni VARCHAR(255) NULL COMMENT 'Identifiant (NNI si national ou autres pièces si étranger)', categorie_soumissionnaire_id INT DEFAULT NULL NULL COMMENT 'Catégorie du soumissionnaire (par exemple PME)', created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_SOCIETE PRIMARY KEY (societe_id));

-- changeset sigmap:1749671959050-39
CREATE TABLE societe_address (societe_address_id INT AUTO_INCREMENT NOT NULL, type_id INT DEFAULT NULL NULL, address TEXT NULL, societe_id INT DEFAULT NULL NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_SOCIETE_ADDRESS PRIMARY KEY (societe_address_id));

-- changeset sigmap:1749671959050-40
CREATE TABLE societe_contact (societe_id INT NOT NULL, contact_id INT NOT NULL, representant_legal TINYINT(1) DEFAULT NULL NULL, principal TINYINT(1) DEFAULT NULL NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, CONSTRAINT PK_SOCIETE_CONTACT PRIMARY KEY (societe_id, contact_id));

-- changeset sigmap:1749671959050-41
CREATE TABLE user_ac (user_id VARCHAR(100) NOT NULL, ac_id INT NOT NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NULL, ac_pilote TINYINT(1) DEFAULT 0 NOT NULL, status ENUM('ACTIF', 'NON_ACTIF') DEFAULT 'ACTIF' NOT NULL, CONSTRAINT PK_USER_AC PRIMARY KEY (user_id, ac_id));

-- changeset sigmap:1749671959050-42
CREATE TABLE user_com (user_id VARCHAR(100) NOT NULL, com_id INT NOT NULL, created_at datetime DEFAULT NULL NULL, created_by VARCHAR(100) NULL, updated_at datetime DEFAULT NULL NULL, updated_by VARCHAR(100) NULL, deleted_at datetime DEFAULT NULL NULL, deleted_by VARCHAR(100) NULL, is_deleted TINYINT(1) DEFAULT 0 NULL, status ENUM('ACTIF', 'NON_ACTIF') DEFAULT 'ACTIF' NOT NULL, CONSTRAINT PK_USER_COM PRIMARY KEY (user_id, com_id));

-- changeset sigmap:1749671959050-43
CREATE INDEX FK_DAC_DOCS ON dac_docs_details(dyna_docs_id, dyna_docs_ver, dyna_fields_id);

-- changeset sigmap:1749671959050-44
CREATE INDEX FK_DAC_PPM ON dac(ppm_id, ppm_ver);

-- changeset sigmap:1749671959050-45
CREATE INDEX FK_DAC_REF_PARAM ON dac(category_id);

-- changeset sigmap:1749671959050-46
CREATE INDEX FK_DYNA_DOCS_REF_PARAM ON dyna_docs(category_id);

-- changeset sigmap:1749671959050-47
CREATE INDEX FK_DYNA_DOC_FIELDS_ID ON dac_docs(dyna_docs_id, dyna_docs_ver, dyna_fields_id);

-- changeset sigmap:1749671959050-48
CREATE INDEX FK_DYNA_FIELDS_DETAILS_DYNA_FIELDS ON dyna_fields_details(dyna_fields_id);

-- changeset sigmap:1749671959050-49
CREATE INDEX FK_DYNA_FIELDS_DYNA_DOCS ON dyna_fields(dyna_docs_id, dyna_docs_ver);

-- changeset sigmap:1749671959050-50
CREATE INDEX authority_code ON menu_item(authority_code);

-- changeset sigmap:1749671959050-51
CREATE INDEX fk_ac_id ON ppm(ac_id);

-- changeset sigmap:1749671959050-52
CREATE INDEX fk_agmp_docs_id ON agpm_docs(doc_id);

-- changeset sigmap:1749671959050-53
CREATE INDEX fk_agpm_ac_id ON agpm(ac_id);

-- changeset sigmap:1749671959050-54
CREATE INDEX fk_categorie_soumissionnaire_id ON societe(categorie_soumissionnaire_id);

-- changeset sigmap:1749671959050-55
CREATE INDEX fk_com_ref_param_com_type_id ON com(com_type_id);

-- changeset sigmap:1749671959050-56
CREATE INDEX fk_compte_bancaire_pays_id ON compte_bancaire(pays_id);

-- changeset sigmap:1749671959050-57
CREATE INDEX fk_compte_bancaire_societe_id ON compte_bancaire(societe_id);

-- changeset sigmap:1749671959050-58
CREATE INDEX fk_docs_realisation_id ON realisation_docs(doc_id);

-- changeset sigmap:1749671959050-59
CREATE INDEX fk_lots_id ON lots_realisation(lots_id, lots_ver);

-- changeset sigmap:1749671959050-60
CREATE INDEX fk_paa_ac_id ON paa(ac_id);

-- changeset sigmap:1749671959050-61
CREATE INDEX fk_paa_docs_docs_id ON paa_docs(doc_id);

-- changeset sigmap:1749671959050-62
CREATE INDEX fk_ppm_com_ppm_id ON ppm_com(ppm_id, ppm_ver);

-- changeset sigmap:1749671959050-63
CREATE INDEX fk_ppm_docs_id ON ppm_docs(doc_id);

-- changeset sigmap:1749671959050-64
CREATE INDEX fk_realisation_direction_id ON realisation(direction_id);

-- changeset sigmap:1749671959050-65
CREATE INDEX fk_realisation_mode_selection_id ON realisation(mode_selection_id);

-- changeset sigmap:1749671959050-66
CREATE INDEX fk_realisation_monais_id ON realisation(monais_id);

-- changeset sigmap:1749671959050-67
CREATE INDEX fk_realisation_paa_id ON paa_realisation(realisation_id, realisation_ver);

-- changeset sigmap:1749671959050-68
CREATE INDEX fk_realisation_ppm_id ON ppm_realisation(realisation_id, realisation_ver);

-- changeset sigmap:1749671959050-69
CREATE INDEX fk_realisation_service_id ON realisation(service_id);

-- changeset sigmap:1749671959050-70
CREATE INDEX fk_realisation_source_financement_id ON realisation(source_financement_id);

-- changeset sigmap:1749671959050-71
CREATE INDEX fk_realisation_type_marcher_id ON realisation(type_marcher_id);

-- changeset sigmap:1749671959050-72
CREATE INDEX fk_ref_param_ref_type_id ON ref_param(ref_type_id);

-- changeset sigmap:1749671959050-73
CREATE INDEX fk_societe_address_id ON societe_address(societe_id);

-- changeset sigmap:1749671959050-74
CREATE INDEX fk_societe_address_type_id ON societe_address(type_id);

-- changeset sigmap:1749671959050-75
CREATE INDEX fk_societe_contact_contact_id ON societe_contact(contact_id);

-- changeset sigmap:1749671959050-76
CREATE INDEX fk_user_ac_ac_id ON user_ac(ac_id);

-- changeset sigmap:1749671959050-77
CREATE INDEX fk_user_com_com_id ON user_com(com_id);

-- changeset sigmap:1749671959050-78
CREATE INDEX parent_id ON menu_item(parent_id);

-- changeset sigmap:1749671959050-79
ALTER TABLE dac_docs_details ADD CONSTRAINT FK_DAC_DOCS FOREIGN KEY (dyna_docs_id, dyna_docs_ver, dyna_fields_id) REFERENCES dyna_fields (dyna_docs_id, dyna_docs_ver, dyna_fields_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-80
ALTER TABLE dac ADD CONSTRAINT FK_DAC_PPM FOREIGN KEY (ppm_id, ppm_ver) REFERENCES ppm (ppm_id, ppm_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-81
ALTER TABLE dac ADD CONSTRAINT FK_DAC_REF_PARAM FOREIGN KEY (category_id) REFERENCES ref_param (ref_param_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-82
ALTER TABLE dac_docs ADD CONSTRAINT FK_DAC_VER_ID FOREIGN KEY (dac_id, dac_ver) REFERENCES dac (dac_id, dac_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-83
ALTER TABLE dyna_docs ADD CONSTRAINT FK_DYNA_DOCS_REF_PARAM FOREIGN KEY (category_id) REFERENCES ref_param (ref_param_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-84
ALTER TABLE dac_docs ADD CONSTRAINT FK_DYNA_DOC_FIELDS_ID FOREIGN KEY (dyna_docs_id, dyna_docs_ver, dyna_fields_id) REFERENCES dyna_fields (dyna_docs_id, dyna_docs_ver, dyna_fields_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-85
ALTER TABLE dyna_fields_details ADD CONSTRAINT FK_DYNA_FIELDS_DETAILS_DYNA_FIELDS FOREIGN KEY (dyna_fields_id) REFERENCES dyna_fields (dyna_fields_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-86
ALTER TABLE dyna_fields ADD CONSTRAINT FK_DYNA_FIELDS_DYNA_DOCS FOREIGN KEY (dyna_docs_id, dyna_docs_ver) REFERENCES dyna_docs (dyna_docs_id, dyna_docs_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-87
ALTER TABLE ppm ADD CONSTRAINT fk_ac_id FOREIGN KEY (ac_id) REFERENCES ac (ac_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-88
ALTER TABLE agpm_docs ADD CONSTRAINT fk_agmp_docs_id FOREIGN KEY (doc_id) REFERENCES docs (doc_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-89
ALTER TABLE agpm ADD CONSTRAINT fk_agpm_ac_id FOREIGN KEY (ac_id) REFERENCES ac (ac_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-90
ALTER TABLE agpm_docs ADD CONSTRAINT fk_agpm_id FOREIGN KEY (agpm_id, agpm_ver) REFERENCES agpm (agpm_id, agpm_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-91
ALTER TABLE societe ADD CONSTRAINT fk_categorie_soumissionnaire_id FOREIGN KEY (categorie_soumissionnaire_id) REFERENCES ref_param (ref_param_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- changeset sigmap:1749671959050-92
ALTER TABLE com ADD CONSTRAINT fk_com_ref_param_com_type_id FOREIGN KEY (com_type_id) REFERENCES ref_param (ref_param_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-93
ALTER TABLE compte_bancaire ADD CONSTRAINT fk_compte_bancaire_pays_id FOREIGN KEY (pays_id) REFERENCES ref_param (ref_param_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-94
ALTER TABLE compte_bancaire ADD CONSTRAINT fk_compte_bancaire_societe_id FOREIGN KEY (societe_id) REFERENCES societe (societe_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-95
ALTER TABLE ppm_docs ADD CONSTRAINT fk_docs_ppm_id FOREIGN KEY (ppm_id, ppm_ver) REFERENCES ppm (ppm_id, ppm_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-96
ALTER TABLE realisation_docs ADD CONSTRAINT fk_docs_realisation_id FOREIGN KEY (doc_id) REFERENCES docs (doc_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-97
ALTER TABLE lots_realisation ADD CONSTRAINT fk_lots_id FOREIGN KEY (lots_id, lots_ver) REFERENCES lots (lots_id, lots_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-98
ALTER TABLE paa ADD CONSTRAINT fk_paa_ac_id FOREIGN KEY (ac_id) REFERENCES ac (ac_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-99
ALTER TABLE paa_docs ADD CONSTRAINT fk_paa_docs_docs_id FOREIGN KEY (doc_id) REFERENCES docs (doc_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-100
ALTER TABLE paa_docs ADD CONSTRAINT fk_paa_docs_paa_id FOREIGN KEY (paa_id, paa_ver) REFERENCES paa (paa_id, paa_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-101
ALTER TABLE paa_realisation ADD CONSTRAINT fk_paa_realisation FOREIGN KEY (paa_id, paa_ver) REFERENCES paa (paa_id, paa_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-102
ALTER TABLE ppm_com ADD CONSTRAINT fk_ppm_com_com_id FOREIGN KEY (com_id) REFERENCES com (com_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-103
ALTER TABLE ppm_com ADD CONSTRAINT fk_ppm_com_ppm_id FOREIGN KEY (ppm_id, ppm_ver) REFERENCES ppm (ppm_id, ppm_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-104
ALTER TABLE ppm_docs ADD CONSTRAINT fk_ppm_docs_id FOREIGN KEY (doc_id) REFERENCES docs (doc_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-105
ALTER TABLE ppm_realisation ADD CONSTRAINT fk_ppm_realisation_id FOREIGN KEY (ppm_id, ppm_ver) REFERENCES ppm (ppm_id, ppm_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-106
ALTER TABLE realisation_docs ADD CONSTRAINT fk_realiasion_docs_id FOREIGN KEY (realisation_id, realisation_ver) REFERENCES realisation (realisation_id, realisation_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-107
ALTER TABLE realisation ADD CONSTRAINT fk_realisation_direction_id FOREIGN KEY (direction_id) REFERENCES ref_param (ref_param_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- changeset sigmap:1749671959050-108
ALTER TABLE lots_realisation ADD CONSTRAINT fk_realisation_lots_id FOREIGN KEY (realisation_id, realisation_ver) REFERENCES realisation (realisation_id, realisation_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-109
ALTER TABLE realisation ADD CONSTRAINT fk_realisation_mode_selection_id FOREIGN KEY (mode_selection_id) REFERENCES ref_param (ref_param_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- changeset sigmap:1749671959050-110
ALTER TABLE realisation ADD CONSTRAINT fk_realisation_monais_id FOREIGN KEY (monais_id) REFERENCES ref_param (ref_param_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- changeset sigmap:1749671959050-111
ALTER TABLE paa_realisation ADD CONSTRAINT fk_realisation_paa_id FOREIGN KEY (realisation_id, realisation_ver) REFERENCES realisation (realisation_id, realisation_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-112
ALTER TABLE ppm_realisation ADD CONSTRAINT fk_realisation_ppm_id FOREIGN KEY (realisation_id, realisation_ver) REFERENCES realisation (realisation_id, realisation_ver) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-113
ALTER TABLE realisation ADD CONSTRAINT fk_realisation_service_id FOREIGN KEY (service_id) REFERENCES ref_param (ref_param_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- changeset sigmap:1749671959050-114
ALTER TABLE realisation ADD CONSTRAINT fk_realisation_source_financement_id FOREIGN KEY (source_financement_id) REFERENCES ref_param (ref_param_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- changeset sigmap:1749671959050-115
ALTER TABLE realisation ADD CONSTRAINT fk_realisation_type_marcher_id FOREIGN KEY (type_marcher_id) REFERENCES ref_param (ref_param_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- changeset sigmap:1749671959050-116
ALTER TABLE ref_param ADD CONSTRAINT fk_ref_param_ref_type_id FOREIGN KEY (ref_type_id) REFERENCES ref_type (ref_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;

-- changeset sigmap:1749671959050-117
ALTER TABLE societe_address ADD CONSTRAINT fk_societe_address_id FOREIGN KEY (societe_id) REFERENCES societe (societe_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-118
ALTER TABLE societe_address ADD CONSTRAINT fk_societe_address_type_id FOREIGN KEY (type_id) REFERENCES ref_param (ref_param_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-119
ALTER TABLE societe_contact ADD CONSTRAINT fk_societe_contact_contact_id FOREIGN KEY (contact_id) REFERENCES contact (contact_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-120
ALTER TABLE societe_contact ADD CONSTRAINT fk_societe_contact_societe_id FOREIGN KEY (societe_id) REFERENCES societe (societe_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-121
ALTER TABLE user_ac ADD CONSTRAINT fk_user_ac_ac_id FOREIGN KEY (ac_id) REFERENCES ac (ac_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-122
ALTER TABLE user_com ADD CONSTRAINT fk_user_com_com_id FOREIGN KEY (com_id) REFERENCES com (com_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-123
ALTER TABLE menu_item ADD CONSTRAINT menu_item_ibfk_1 FOREIGN KEY (authority_code) REFERENCES authority (authority_code) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset sigmap:1749671959050-124
ALTER TABLE menu_item ADD CONSTRAINT menu_item_ibfk_2 FOREIGN KEY (parent_id) REFERENCES menu_item (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

