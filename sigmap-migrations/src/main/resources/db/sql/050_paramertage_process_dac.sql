
-- liquibase formatted sql

-- changeset sigmap:1749671959051-1


CREATE TABLE paramertage_process_dac ( type_marcher_id INT NOT NULL,
                                       mode_passation_id INT NOT NULL,
                                       process VARCHAR(255)Not NULL);

-- changeset sigmap:1749671959051-2
ALTER TABLE paramertage_process_dac ADD CONSTRAINT fk_param_process_dac_type_marcher
    FOREIGN KEY (type_marcher_id) REFERENCES ref_param (ref_param_id);

-- changeset sigmap:1749671959051-3
ALTER TABLE paramertage_process_dac ADD CONSTRAINT fk_param_process_dac_mode_passation
    FOREIGN KEY (mode_passation_id) REFERENCES mode_passation (mode_passation_id);






