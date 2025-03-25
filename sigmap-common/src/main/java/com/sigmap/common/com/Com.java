package com.sigmap.common.com;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_types.RefType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "com")
public class Com extends BaseEntity  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "com_id")
    private Integer id;
    private String decretCreation;
    private String responsable;
    private Boolean special=false;
    private String ancrage;
    private String contact;
    private String adresse;
    private String membres;
    @ManyToOne
    @JoinColumn(name = "com_type_id")
    private RefParam refParam;

}
