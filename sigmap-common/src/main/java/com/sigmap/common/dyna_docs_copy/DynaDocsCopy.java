package com.sigmap.common.dyna_docs_copy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dyna_docs_copy")

public class DynaDocsCopy extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dyna_docs_copy_id")
    private Integer id;

    @Column(name = "dyna_docs_copy_ver")
    private Integer version;

    @Column(name = "dyna_docs_copy_name")
    private String name;

    @Column(name = "dyna_docs_copy_template_file_name")
    private String templateFileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "dyna_docs_copy_status")
    private ActiveStatus status;

    @Column(name = "dyna_docs_copy_is_aao")
    private Boolean isAAO;

    @Column(name = "is_static_dyna_docs_copy")
    private Boolean isStatic;

    @Column(name = "dyna_docs_copy_ordre")
    private Integer ordre;

    @JsonIgnore
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dac_id", referencedColumnName = "dac_id"),
            @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver")
    })
    private Dac dac;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dyna_docs_id", referencedColumnName = "dyna_docs_id"),
            @JoinColumn(name = "dyna_docs_ver", referencedColumnName = "dyna_docs_ver")
    })
    private DynaDocs dynaDocs;
}
