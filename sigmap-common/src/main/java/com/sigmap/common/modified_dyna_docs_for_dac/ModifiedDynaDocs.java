package com.sigmap.common.modified_dyna_docs_for_dac;

import com.sigmap.common.dac.Dac;
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
@Table(name = "modified_dyna_docs")
public class ModifiedDynaDocs extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "modif_dyna_docs_id")
    private Integer id;

    @Column(name = "modif_dyna_docs_ver")
    private Integer version;

    @Column(name = "name")
    private String name;

    @Column(name = "template_file_name")
    private String templateFileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ActiveStatus status;

    @Column(name = "is_aao")
    private Boolean isAAO;

    @Column(name = "ordre")
    private Integer ordre;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dac_id", referencedColumnName = "dac_id"),
            @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver")
    })
    private Dac dac;

}
