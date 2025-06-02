package com.sigmap.common.dyna_fields_details;

import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dyna_fields_details")
@Entity
public class DynaFieldsDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dyna_fields_details_id")
    private Integer dynaFieldsDetailsId;

    @Column(name = "column_name", length = 100)
    private String columnName;

    @Column(name = "column_pos")
    private Integer columnPos;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private RefParam typeId;

    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "source_field", length = 100)
    private String source_field;

    @ManyToOne
    @JoinColumn(name = "dyna_fields_id")
    private DynaFields dynaFieldsId;
    // dyna fields fiha akther men pk w hnaa 3ana fk wa7ed ??

}
