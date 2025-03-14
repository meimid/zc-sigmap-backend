-- liquibase formatted sql

-- changeset db:1732196145835-2
CREATE TABLE IF NOT EXISTS `ac`  (
  `ac_id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NULL,
  `ancrage` varchar(100) NULL,
  `contact` varchar(255) NULL,
  PRIMARY KEY (`ac_id`)
);

CREATE TABLE IF NOT EXISTS `agpm`  (
  `agpm_id` int NOT NULL,
  `agpm_ver` int NOT NULL,
  `ref` varchar(100) NULL,
  `nom` varchar(255) NULL,
  `contenu` longtext NULL,
  `ac_id` int NULL,
  `created_at` datetime(0) NULL,
  `created_by` int(0) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` int(0) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` int(0) NULL,
  `is_deleted` tinyint(1) NULL,
  `status_id` int(0) NULL,
  PRIMARY KEY (`agpm_id`, `agpm_ver`)
);

CREATE TABLE IF NOT EXISTS `agpm_docs`  (
  `agpm_id` int NOT NULL,
  `agpm_ver` int NOT NULL,
  `doc_id` int NOT NULL,
  PRIMARY KEY (`agpm_id`, `agpm_ver`, `doc_id`)
);

CREATE TABLE IF NOT EXISTS `com`  (
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

CREATE TABLE IF NOT EXISTS `com_ac`  (
  `com_id` int NOT NULL,
  `ac_id` int NOT NULL,
  `status` set('Active','Disabled') NULL DEFAULT '',
  PRIMARY KEY (`com_id`, `ac_id`)
);

CREATE TABLE IF NOT EXISTS `compte_bancaire`  (
  `compte_bancaire_id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NULL,
  `agence` varchar(255) NULL,
  `rib` varchar(255) NULL,
  `pays_id` int NULL,
  `local` tinyint(1) NULL,
  `societe_id` int NULL,
  PRIMARY KEY (`compte_bancaire_id`)
);

CREATE TABLE IF NOT EXISTS `contact`  (
  `contact_id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NULL,
  `address` text NULL,
  `tel1` varchar(255) NULL,
  `tel2` varchar(255) NULL,
  `email` varchar(255) NULL,
  PRIMARY KEY (`contact_id`)
);

CREATE TABLE IF NOT EXISTS `dac`  (
  `dac_id` int NOT NULL AUTO_INCREMENT,
  `ppm_id` int NULL,
  PRIMARY KEY (`dac_id`)
);

CREATE TABLE IF NOT EXISTS `docs`  (
  `doc_id` int NOT NULL,
  `name` varchar(255) NULL,
  `alfresco_id` varchar(255) NULL,
  PRIMARY KEY (`doc_id`)
);

CREATE TABLE IF NOT EXISTS  `lots`  (
  `lots_id` int NOT NULL AUTO_INCREMENT,
  `lots_ver` int NOT NULL,
  `description` varchar(100) NULL,
  PRIMARY KEY (`lots_id`, `lots_ver`)
);

CREATE TABLE IF NOT EXISTS `lots_realisation`  (
  `lots_id` int NOT NULL,
  `lots_ver` int NOT NULL,
  `realisation_id` int NOT NULL,
  `realisation_ver` int NOT NULL,
  PRIMARY KEY (`lots_id`, `lots_ver`, `realisation_id`, `realisation_ver`)
);

CREATE TABLE IF NOT EXISTS `paa`  (
  `paa_id` int NOT NULL,
  `paa_ver` int NOT NULL,
  `ref` varchar(100) NULL,
  `nom` varchar(255) NULL,
  `annee` varchar(4) NULL,
  `ac_id` int NULL,
  `budget_estime` decimal(10, 2) NULL,
  `created_at` datetime(0) NULL,
  `created_by` int(0) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` int(0) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` int(0) NULL,
  `is_deleted` tinyint(1) NULL,
  `status_id` int(0) NULL,
  PRIMARY KEY (`paa_id`, `paa_ver`)
);

CREATE TABLE IF NOT EXISTS `paa_docs`  (
  `paa_id` int NOT NULL,
  `paa_ver` int NOT NULL,
  `doc_id` int NOT NULL,
  PRIMARY KEY (`paa_id`, `paa_ver`, `doc_id`)
);

CREATE TABLE IF NOT EXISTS `paa_realisation`  (
  `paa_id` int NOT NULL,
  `paa_ver` int NOT NULL,
  `realisation_id` int NOT NULL,
  `realisation_ver` int NOT NULL,
  PRIMARY KEY (`paa_id`, `paa_ver`, `realisation_id`, `realisation_ver`)
);

CREATE TABLE IF NOT EXISTS `paa_realisation_docs`  (
  `paa_realisation_id` int NOT NULL,
  `doc_id` int NOT NULL,
  PRIMARY KEY (`paa_realisation_id`, `doc_id`)
);

CREATE TABLE IF NOT EXISTS `ppm`  (
  `ppm_id` int NOT NULL,
  `ppm_ver` int NOT NULL,
  `ref` varchar(100) NULL,
  `nom` varchar(255) NULL,
  `annee` varchar(4) NULL,
  `ac_id` int NULL,
  `budget_estime` decimal(10, 2) NULL COMMENT 'Budget estimé (en MRU)',
  `created_at` datetime(0) NULL,
  `created_by` int(0) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` int(0) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` int(0) NULL,
  `is_deleted` tinyint(1) NULL,
  `status_id` int(0) NULL,
  PRIMARY KEY (`ppm_id`, `ppm_ver`)
);

CREATE TABLE IF NOT EXISTS `ppm_docs`  (
  `ppm_id` int NOT NULL,
  `ppm_ver` int NOT NULL,
  `doc_id` int NOT NULL,
  PRIMARY KEY (`ppm_id`, `ppm_ver`, `doc_id`)
);

CREATE TABLE IF NOT EXISTS `ppm_lots`  (
  `ppm_id` int NOT NULL,
  `ppm_ver` int NOT NULL,
  `lots_id` int NOT NULL,
  `lots_ver` int NOT NULL,
  PRIMARY KEY (`ppm_id`, `ppm_ver`, `lots_id`, `lots_ver`)
);

CREATE TABLE IF NOT EXISTS `ppm_realisation`  (
  `ppm_id` int NOT NULL AUTO_INCREMENT,
  `ref` varchar(100) NOT NULL,
  `realisation_envisagees` varchar(100) NULL,
  `service_id` int NULL,
  `direction_id` int NULL,
  `source_financement_id` int NULL,
  `type_marcher_id` int NULL,
  `mode_selection_id` int NULL,
  `date_prev_lps` date NULL COMMENT 'Date prévue de lancement de la procédure de sélection\r\n',
  `date_prev_ac` date NULL COMMENT 'Date prévue d’attribution du contrat\r\n',
  `date_prev_dp` date NULL COMMENT 'Date prévue de démarrage des prestations',
  `date_prev_ap` date NULL COMMENT 'Date prévue d’achèvement des prestations',
  `budget_estime` decimal(10, 2) NULL COMMENT 'Budget estimé (en MRU)',
  `is_published` tinyint(1) NULL DEFAULT 0,
  `published_at` tinyint(1) NULL,
  `created_at` datetime NULL,
  `created_by` int NULL,
  `updated_at` datetime NULL,
  `updated_by` int NULL,
  `deleted_at` datetime NULL,
  `deleted_by` int NULL,
  `is_deleted` tinyint(1) NULL,
  `status_id` int NULL,
  PRIMARY KEY (`ppm_id`),
  UNIQUE INDEX `ppm_ref_idx`(`ref`)
);

CREATE TABLE IF NOT EXISTS `realisation`  (
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
  `created_at` datetime NULL,
  `created_by` int NULL,
  `updated_at` datetime NULL,
  `updated_by` int NULL,
  `deleted_at` datetime NULL,
  `deleted_by` int NULL,
  `is_deleted` tinyint(1) NULL,
  `sort` int(5) NULL,
  PRIMARY KEY (`realisation_id`, `realisation_ver`)
);

CREATE TABLE IF NOT EXISTS `ref_param`  (
  `ref_param_id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(20) NOT NULL,
  `description` varchar(50) NOT NULL,
  `ref_type_id` int NOT NULL,
  `created_at` datetime(0) NULL,
  `created_by` int(0) NULL,
  `updated_at` datetime(0) NULL,
  `updated_by` int(0) NULL,
  `deleted_at` datetime(0) NULL,
  `deleted_by` int(0) NULL,
  `is_deleted` tinyint(1) NULL,
  PRIMARY KEY (`ref_param_id`),
  UNIQUE INDEX `ref_param_code_idx`(`code`)
);

CREATE TABLE IF NOT EXISTS `ref_type`  (
  `ref_type_id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(50) NOT NULL,
  PRIMARY KEY (`ref_type_id`)
);

CREATE TABLE IF NOT EXISTS `societe`  (
  `societe_id` int NOT NULL,
  `nom` varchar(255) NULL,
  `type_id` int NULL,
  `pays_id` int NULL,
  `nni` varchar(255) NULL,
  `categorie_soumissionnaire_id` varchar(255) NULL,
  PRIMARY KEY (`societe_id`)
);

CREATE TABLE IF NOT EXISTS `societe_address`  (
  `societe_address_id` int NOT NULL AUTO_INCREMENT,
  `type_id` int NULL,
  `address` text NULL,
  `societe_id` int NULL,
  PRIMARY KEY (`societe_address_id`)
);

CREATE TABLE IF NOT EXISTS `societe_contact`  (
  `societe_id` int NOT NULL,
  `contact_id` int NOT NULL,
  `representant_legal` tinyint(1) NULL,
  `principal` tinyint(1) NULL,
  PRIMARY KEY (`societe_id`, `contact_id`)
);