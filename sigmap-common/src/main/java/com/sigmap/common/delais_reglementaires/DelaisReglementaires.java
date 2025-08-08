package com.sigmap.common.delais_reglementaires;


import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "delais_reglementaires")
public class DelaisReglementaires extends BaseEntity {

    @EmbeddedId
    private DelaisReglementairesId id;

    @Column(name = "delais_publication_lancement")
    private Integer delaiPublicationLancement;

    @Column(name = "delais_lancement_attribution")
    private Integer delaiLancementAttribution;

    @Column(name = "delais_attribution_demarrage")
    private Integer delaiAttributionDemarrage;
}
