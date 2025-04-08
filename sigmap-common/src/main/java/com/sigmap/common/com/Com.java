package com.sigmap.common.com;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "com")
public class Com extends BaseEntity  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "com_id")
    private Integer id;
    private String nom;
    @Column(name = "decret_creation")
    private String decretCreation;
    private String responsable;
    private Boolean special;
    private String ancrage;
    private String contact;
    private String adresse;
    private String membres;
    @ManyToOne
    @JoinColumn(name = "com_type_id")
    private RefParam comType;
    @ManyToMany
    @JoinTable(name="com_ac",
            joinColumns = @JoinColumn(name = "com_id"),
            inverseJoinColumns = @JoinColumn(name = "ac_id"))
    private List<Ac> acs=new ArrayList<>();

}
