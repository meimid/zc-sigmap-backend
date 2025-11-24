package com.sigmap.common.devise_history;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Calendar;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "devise_historique")
public class DeviseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "devise_id")
    private Integer deviseId;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "taux")
    private Double taux;

    @Column(name = "date_debut")
    private Calendar dateDebut;

    @Column(name="date_fin")
    private Calendar dateFin;

    @Column(name = "action")
    private String action;

    @Column(name = "changed_by")
    private String changedBy;

}
