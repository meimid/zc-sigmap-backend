package com.sigmap.common.dyna_docs;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DynaDocID implements Serializable {
    @Column(name = "dyna_docs_id")
    private Integer dynaDocsId;

    @Column(name = "dyna_docs_ver")
    private Integer dynaDocsVer;
}
