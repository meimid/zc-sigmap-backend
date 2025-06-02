package com.sigmap.common.dac_docs;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dac_docs")
public class DacDocs {

    @EmbeddedId
    private DacDocsId id;
    @Column(name = "source_id", length = 100)
    private String sourceId;
    @Column(name = "field_value", length = 255)
    private String fieldValue;

}
