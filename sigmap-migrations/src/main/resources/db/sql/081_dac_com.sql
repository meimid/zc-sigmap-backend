-- changeset sigmap:1757950282-1
CREATE TABLE dac_com (
                         com_id INT NOT NULL,
                         dac_id INT NOT NULL,
                         dac_ver INT NOT NULL,
                         CONSTRAINT PK_DAC_COM PRIMARY KEY (com_id, dac_id, dac_ver));