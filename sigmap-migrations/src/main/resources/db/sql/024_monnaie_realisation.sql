-- liquibase formatted sql

-- changeset sigmap:1749671959050-62
ALTER TABLE realisation
    ADD COLUMN monnaie_id INT DEFAULT NULL;

-- changeset sigmap:1749671959050-63
CREATE INDEX fk_realisation_monnaie_id ON realisation(monnaie_id);

-- changeset sigmap:1749671959050-64
ALTER TABLE realisation
    ADD CONSTRAINT fk_realisation_monnaie_id
        FOREIGN KEY (monnaie_id) REFERENCES monnaie(monnaie_id) ON UPDATE CASCADE ON DELETE SET NULL;
