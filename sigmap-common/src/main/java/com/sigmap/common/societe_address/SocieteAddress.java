package com.sigmap.common.societe_address;

import com.sigmap.common.Participant.ParticipantId;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "societe_address")
@IdClass(SocieteAddressId.class)
public class SocieteAddress {

    @Id
    @Column(name = "societe_id")
    private Integer societeId;

    @Id
    @Column(name = "adresse_id")
    private Integer addressId;


    @ManyToOne
    @JoinColumn(name = "type_id")
    private RefParam type;

    @ManyToOne
    @JoinColumn(name = "adresse_id", insertable = false, updatable = false)

    Adresse address;
    @ManyToOne
    @JoinColumn(name = "societe_id", insertable = false, updatable = false)

    Societe societe;

}
