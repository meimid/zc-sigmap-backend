package com.sigmap.common.societe_dac;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.MethodePaiement;
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
@Table(name = "societe_dac")
public class SocieteDac extends BaseEntity {

    @EmbeddedId
    private SocieteDacID id;

    @Column(name = "methode_paiement")
    @Enumerated(EnumType.STRING)
    private MethodePaiement methodePaiement;

    @Column(name = "justif_paiement_id")
    private Integer justifPaiementId;

    @Column(name = "status_paiement")
    private boolean statusPaiement;
}
