package com.sigmap.common.societe_address;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "societe_address")
public class SocieteAddress extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "societe_address_id")
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "type_id")
    private RefParam refParam;
    private String address;
    @ManyToOne
    @JoinColumn(name = "societe_id")
    private Societe societe;




}
