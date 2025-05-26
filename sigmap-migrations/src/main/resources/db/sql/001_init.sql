-- liquibase formatted sql

-- changeset db:1732196145835-1
CREATE TABLE `ac`  (
  `ac_id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NULL,
  `ancrage` varchar(100) NULL,
  `contact` varchar(255) NULL,
  `created_at` datetime(0) NULL,
  `created_by` varchar(100) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` varchar(100) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` varchar(100) NULL,
  `is_deleted` tinyint(1) NULL,
  PRIMARY KEY (`ac_id`)
);

CREATE TABLE `agpm`  (
  `agpm_id` int NOT NULL,
  `agpm_ver` int NOT NULL,
  `ref` varchar(100) NULL,
  `nom` varchar(255) NULL,
  `contenu` longtext NULL,
  `ac_id` int NULL,
  `status_id` int(0) NULL,
  `created_at` datetime(0) NULL,
  `created_by` varchar(100) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` varchar(100) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` varchar(100) NULL,
  `is_deleted` tinyint(1) NULL,
  PRIMARY KEY (`agpm_id`, `agpm_ver`)
);

CREATE TABLE `agpm_docs`  (
  `agpm_id` int NOT NULL,
  `agpm_ver` int NOT NULL,
  `doc_id` int NOT NULL,
  PRIMARY KEY (`agpm_id`, `agpm_ver`, `doc_id`)
);

CREATE TABLE `com`  (
  `com_id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NULL,
  `decret_creation` varchar(100) NULL,
  `responsable` varchar(100) NULL,
  `special` tinyint(1) NULL DEFAULT 0,
  `ancrage` varchar(100) NULL,
  `contact` varchar(255) NULL,
  `adresse` text NULL,
  `membres` varchar(100) NULL,
  `com_type_id` int(11) NULL,
  PRIMARY KEY (`com_id`)
);

CREATE TABLE `com_ac`  (
  `com_id` int NOT NULL,
  `ac_id` int NOT NULL,
  `status` set('Active','Disabled') NULL DEFAULT '',
  PRIMARY KEY (`com_id`, `ac_id`)
);

CREATE TABLE `compte_bancaire`  (
  `compte_bancaire_id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NULL,
  `agence` varchar(255) NULL,
  `rib` varchar(255) NULL,
  `pays_id` int NULL,
  `local` tinyint(1) NULL,
  `societe_id` int NULL,
  PRIMARY KEY (`compte_bancaire_id`)
);

CREATE TABLE `contact`  (
  `contact_id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NULL,
  `address` text NULL,
  `tel1` varchar(255) NULL,
  `tel2` varchar(255) NULL,
  `email` varchar(255) NULL,
  `created_at` datetime(0) NULL,
  `created_by` varchar(100) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` varchar(100) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` varchar(100) NULL,
  `is_deleted` tinyint(1) NULL,
  PRIMARY KEY (`contact_id`)
);

CREATE TABLE `dac`  (
  `dac_id` int NOT NULL AUTO_INCREMENT,
  `ppm_id` int NULL,
  PRIMARY KEY (`dac_id`)
);

CREATE TABLE `docs`  (
  `doc_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NULL,
  `alfresco_id` varchar(255) NULL,
  `created_at` datetime(0) NULL,
  `created_by` varchar(100) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` varchar(100) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` varchar(100) NULL,
  `is_deleted` tinyint(1) NULL,
  PRIMARY KEY (`doc_id`)
);

CREATE TABLE `lots`  (
  `lots_id` int NOT NULL AUTO_INCREMENT,
  `lots_ver` int NOT NULL,
  `description` varchar(100) NULL,
  PRIMARY KEY (`lots_id`, `lots_ver`)
);

CREATE TABLE `lots_realisation`  (
  `realisation_id` int NOT NULL,
  `realisation_ver` int NOT NULL,
  `lots_id` int NOT NULL,
  `lots_ver` int NOT NULL,
  PRIMARY KEY (`realisation_id`, `realisation_ver`, `lots_id`, `lots_ver`)
);

CREATE TABLE `paa`  (
  `paa_id` int NOT NULL AUTO_INCREMENT,
  `paa_ver` int NOT NULL,
  `ref` varchar(100) NULL,
  `nom` varchar(255) NULL,
  `annee` varchar(4) NULL,
  `ac_id` int NULL,
  `budget_estime` decimal(10, 2) NULL COMMENT 'Budget estimé (en MRU)',
  `created_at` datetime(0) NULL,
  `created_by` varchar(100) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` varchar(100) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` varchar(100) NULL,
  `is_deleted` tinyint(1) NULL,
  `status_id` int(0) NULL,
  PRIMARY KEY (`paa_id`, `paa_ver`)
);

CREATE TABLE `paa_docs`  (
  `paa_id` int NOT NULL,
  `paa_ver` int NOT NULL,
  `doc_id` int NOT NULL,
  PRIMARY KEY (`paa_id`, `paa_ver`, `doc_id`)
);

CREATE TABLE `paa_realisation`  (
  `paa_id` int NOT NULL,
  `paa_ver` int NOT NULL,
  `realisation_id` int NOT NULL,
  `realisation_ver` int NOT NULL,
  PRIMARY KEY (`paa_id`, `paa_ver`, `realisation_id`, `realisation_ver`)
);

CREATE TABLE `ppm`  (
  `ppm_id` int NOT NULL AUTO_INCREMENT,
  `ppm_ver` int NOT NULL,
  `ref` varchar(100) NULL,
  `nom` varchar(255) NULL,
  `annee` varchar(4) NULL,
  `ac_id` int NULL,
  `budget_estime` decimal(10, 2) NULL COMMENT 'Budget estimé (en MRU)',
  `created_at` datetime(0) NULL,
  `created_by` varchar(100) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` varchar(100) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` varchar(100) NULL,
  `is_deleted` tinyint(1) NULL,
  `status_id` int(0) NULL,
  PRIMARY KEY (`ppm_id`, `ppm_ver`)
);

CREATE TABLE `ppm_docs`  (
  `ppm_id` int NOT NULL,
  `ppm_ver` int NOT NULL,
  `doc_id` int NOT NULL,
  PRIMARY KEY (`ppm_id`, `ppm_ver`, `doc_id`)
);

CREATE TABLE `ppm_realisation`  (
  `ppm_id` int NOT NULL,
  `ppm_ver` int NOT NULL,
  `realisation_id` int NOT NULL,
  `realisation_ver` int NOT NULL,
  PRIMARY KEY (`ppm_id`, `ppm_ver`, `realisation_id`, `realisation_ver`)
);

CREATE TABLE `realisation`  (
  `realisation_id` int NOT NULL AUTO_INCREMENT,
  `realisation_ver` int NOT NULL,
  `realisation_envisagees` varchar(100) NULL,
  `service_id` int NULL,
  `direction_id` int NULL,
  `source_financement_id` int NULL,
  `monais_id` int NULL,
  `type_marcher_id` int NULL,
  `mode_selection_id` int NULL,
  `date_prev_lps` date NULL COMMENT 'Date prévue de lancement de la procédure de sélection\r\n',
  `date_prev_ac` date NULL COMMENT 'Date prévue d’attribution du contrat\r\n',
  `date_prev_dp` date NULL COMMENT 'Date prévue de démarrage des prestations',
  `date_prev_ap` date NULL COMMENT 'Date prévue d’achèvement des prestations',
  `budget_estime` decimal(10, 2) NULL COMMENT 'Budget estimé (en MRU)',
  `is_published` tinyint(1) NULL DEFAULT 0,
  `published_at` tinyint(1) NULL,
  `sort` int(5) NULL,
  `created_at` datetime(0) NULL,
  `created_by` varchar(100) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` varchar(100) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` varchar(100) NULL,
  `is_deleted` tinyint(1) NULL,
  PRIMARY KEY (`realisation_id`, `realisation_ver`)
);

CREATE TABLE `realisation_docs`  (
  `realisation_id` int NOT NULL,
  `realisation_ver` int NOT NULL,
  `doc_id` int NOT NULL,
  PRIMARY KEY (`realisation_id`, `realisation_ver`, `doc_id`)
);

CREATE TABLE `ref_param`  (
  `ref_param_id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(20) NOT NULL,
  `description` varchar(50) NOT NULL,
  `ref_type_id` int NOT NULL,
  `created_at` datetime(0) NULL,
  `created_by` varchar(100) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` varchar(100) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` varchar(100) NULL,
  `is_deleted` tinyint(1) NULL,
  PRIMARY KEY (`ref_param_id`)
);

CREATE TABLE `ref_type`  (
  `ref_type_id` int NOT NULL,
  `description` varchar(50) NOT NULL,
  `created_at` datetime(0) NULL,
  `created_by` varchar(100) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` varchar(100) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` varchar(100) NULL,
  `is_deleted` tinyint(1) NULL,
  PRIMARY KEY (`ref_type_id`)
);

CREATE TABLE `societe`  (
  `societe_id` int NOT NULL,
  `nom` varchar(255) NULL,
  `type_id` int NULL,
  `pays_id` int NULL,
  `nni` varchar(255) NULL COMMENT 'Identifiant (NNI si national ou autres pièces si étranger)',
  `categorie_soumissionnaire_id` varchar(255) NULL COMMENT 'Catégorie du soumissionnaire (par exemple PME)',
  PRIMARY KEY (`societe_id`)
);

CREATE TABLE `societe_address`  (
  `societe_address_id` int NOT NULL AUTO_INCREMENT,
  `type_id` int NULL,
  `address` text NULL,
  `societe_id` int NULL,
  PRIMARY KEY (`societe_address_id`)
);

CREATE TABLE `societe_contact`  (
  `societe_id` int NOT NULL,
  `contact_id` int NOT NULL,
  `representant_legal` tinyint(1) NULL,
  `principal` tinyint(1) NULL,
  PRIMARY KEY (`societe_id`, `contact_id`)
);

ALTER TABLE `agpm` ADD CONSTRAINT `fk_agpm_ac_id` FOREIGN KEY (`ac_id`) REFERENCES `ac` (`ac_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `agpm_docs` ADD CONSTRAINT `fk_agpm_id` FOREIGN KEY (`agpm_id`, `agpm_ver`) REFERENCES `agpm` (`agpm_id`, `agpm_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `agpm_docs` ADD CONSTRAINT `fk_agmp_docs_id` FOREIGN KEY (`doc_id`) REFERENCES `docs` (`doc_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `com` ADD CONSTRAINT `fk_com_ref_param_com_type_id` FOREIGN KEY (`com_type_id`) REFERENCES `ref_param` (`ref_param_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `com_ac` ADD CONSTRAINT `fk_com_ac_com_id` FOREIGN KEY (`com_id`) REFERENCES `com` (`com_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `com_ac` ADD CONSTRAINT `fk_com_ac_ac_id` FOREIGN KEY (`ac_id`) REFERENCES `ac` (`ac_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `compte_bancaire` ADD CONSTRAINT `fk_compte_bancaire_pays_id` FOREIGN KEY (`pays_id`) REFERENCES `ref_param` (`ref_param_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `compte_bancaire` ADD CONSTRAINT `fk_compte_bancaire_societe_id` FOREIGN KEY (`societe_id`) REFERENCES `societe` (`societe_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `dac` ADD CONSTRAINT `dac_ppm_id` FOREIGN KEY (`ppm_id`) REFERENCES `ppm_realisation` (`ppm_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `lots_realisation` ADD CONSTRAINT `fk_realisation_lots_id` FOREIGN KEY (`realisation_id`, `realisation_ver`) REFERENCES `realisation` (`realisation_id`, `realisation_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `lots_realisation` ADD CONSTRAINT `fk_lots_id` FOREIGN KEY (`lots_id`, `lots_ver`) REFERENCES `lots` (`lots_id`, `lots_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `paa` ADD CONSTRAINT `fk_paa_ac_id` FOREIGN KEY (`ac_id`) REFERENCES `ac` (`ac_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `paa_docs` ADD CONSTRAINT `fk_paa_docs_paa_id` FOREIGN KEY (`paa_id`, `paa_ver`) REFERENCES `paa` (`paa_id`, `paa_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `paa_docs` ADD CONSTRAINT `fk_paa_docs_docs_id` FOREIGN KEY (`doc_id`) REFERENCES `docs` (`doc_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `paa_realisation` ADD CONSTRAINT `fk_paa_realisation` FOREIGN KEY (`paa_id`, `paa_ver`) REFERENCES `paa` (`paa_id`, `paa_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `paa_realisation` ADD CONSTRAINT `fk_realisation_paa_id` FOREIGN KEY (`realisation_id`, `realisation_ver`) REFERENCES `realisation` (`realisation_id`, `realisation_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `ppm` ADD CONSTRAINT `fk_ac_id` FOREIGN KEY (`ac_id`) REFERENCES `ac` (`ac_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `ppm_docs` ADD CONSTRAINT `fk_docs_ppm_id` FOREIGN KEY (`ppm_id`, `ppm_ver`) REFERENCES `ppm` (`ppm_id`, `ppm_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `ppm_docs` ADD CONSTRAINT `fk_ppm_docs_id` FOREIGN KEY (`doc_id`) REFERENCES `docs` (`doc_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `ppm_realisation` ADD CONSTRAINT `fk_ppm_realisation_id` FOREIGN KEY (`ppm_id`, `ppm_ver`) REFERENCES `ppm` (`ppm_id`, `ppm_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `ppm_realisation` ADD CONSTRAINT `fk_realisation_ppm_id` FOREIGN KEY (`realisation_id`, `realisation_ver`) REFERENCES `realisation` (`realisation_id`, `realisation_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `realisation_docs` ADD CONSTRAINT `fk_realiasion_docs_id` FOREIGN KEY (`realisation_id`, `realisation_ver`) REFERENCES `realisation` (`realisation_id`, `realisation_ver`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `realisation_docs` ADD CONSTRAINT `fk_docs_realisation_id` FOREIGN KEY (`doc_id`) REFERENCES `docs` (`doc_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `ref_param` ADD CONSTRAINT `fk_ref_param_ref_type_id` FOREIGN KEY (`ref_type_id`) REFERENCES `ref_type` (`ref_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `societe_address` ADD CONSTRAINT `fk_societe_address _id` FOREIGN KEY (`societe_id`) REFERENCES `societe` (`societe_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `societe_address` ADD CONSTRAINT `fk_societe_address_type_id` FOREIGN KEY (`type_id`) REFERENCES `ref_param` (`ref_param_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `societe_contact` ADD CONSTRAINT `fk_societe_contact_societe_id` FOREIGN KEY (`societe_id`) REFERENCES `societe` (`societe_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `societe_contact` ADD CONSTRAINT `fk_societe_contact_contact_id` FOREIGN KEY (`contact_id`) REFERENCES `contact` (`contact_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
