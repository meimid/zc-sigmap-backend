package com.sigmap.common.dyna_form_fields;

import com.sigmap.common.dyna_form.DynaForm;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dyna_form_fields")
@IdClass(DynaFormFieldId.class)
public class DynaFormField extends BaseEntity {

    @Id
    @Column(name = "dyna_form_id", nullable = false)
    private Integer dynaFormId;

    @Id
    @Column(name = "dyna_form_ver", nullable = false)
    private Integer dynaFormVer;

    @Id
    @Column(name = "dyna_form_fields_id", nullable = false)
    private Integer dynaFormFieldsId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "dyna_form_field_order")
    private Integer order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private ActiveStatus status = ActiveStatus.ACTIF;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "dyna_form_id",  referencedColumnName = "dyna_form_id", insertable = false, updatable = false),
            @JoinColumn(name = "dyna_form_ver", referencedColumnName = "dyna_form_ver", insertable = false, updatable = false)
    })
    private DynaForm form;
}
