package com.sigmap.common.dac_dyna_form;

import com.sigmap.common.dac_dyna_form.DacDynaFormId;
import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "dac_dyna_form")
@IdClass(DacDynaFormId.class)
public class DacDynaForm extends BaseEntity {

    @Id
    @Column(name = "dac_id")
    private Integer dacId;

    @Id
    @Column(name = "dac_ver")
    private Integer dacVer;

    @Id
    @Column(name = "dyna_form_id")
    private Integer dynaFormId;

    @Id
    @Column(name = "dyna_form_ver")
    private Integer dynaFormVer;

    @Id
    @Column(name = "dyna_form_fields_id")
    private Integer dynaFormFieldsId;

    @Column(name = "display")
    private Boolean display;
}