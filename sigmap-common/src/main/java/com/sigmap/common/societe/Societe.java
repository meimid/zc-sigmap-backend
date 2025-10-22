package com.sigmap.common.societe;

import com.sigmap.common.activite.Activite;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.SocieteStatus;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.societe_address.SocieteAddress;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.request.UserSocieteRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "societe")
public class Societe extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "societe_id")
    private Integer id;
    private String nom;
    @ManyToOne
    @JoinColumn(name = "type_id")
    private RefParam type;
    @ManyToOne
    @JoinColumn(name = "categorie_soumissionnaire_id")
    private RefParam categorieSoumissionnaire;
    private String nif;
    @Column(name = "date_nif")
    private Date dateNif;
    @Column(name = "num_cnss")
    private Integer numCnss;
    @Enumerated(EnumType.STRING)
    private SocieteStatus status;

    @ManyToMany
    @JoinTable(
            name = "societe_activite",
            joinColumns = @JoinColumn(name = "societe_id"),
            inverseJoinColumns = @JoinColumn(name = "activite_id")
    )
    private List<Activite> activites = new ArrayList<Activite>();

    @OneToMany(mappedBy = "societe")
    private List<SocieteAddress> societeAddresses = new ArrayList<>();
    @OneToMany(mappedBy = "societe")
    private List<UserSociete> usersSociete = new ArrayList<>();
}
