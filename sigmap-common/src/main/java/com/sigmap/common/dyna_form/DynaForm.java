package com.sigmap.common.dyna_form;

import com.sigmap.common.dyna_form_fields.DynaFormField;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.TypeDossier;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dyna_form")
@IdClass(CompositeId.class)
public class DynaForm extends BaseEntity {

    @Id
    @Column(name = "dyna_form_id", nullable = false)
    private Integer id;

    @Id
    @Column(name = "dyna_form_ver", nullable = false)
    private Integer version;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "dyna_form_order")
    private Integer order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private ActiveStatus status = ActiveStatus.ACTIF;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_dossier", length = 32, nullable = false)
    private TypeDossier typeDossier;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<DynaFormField> fields = new ArrayList<>();
}
