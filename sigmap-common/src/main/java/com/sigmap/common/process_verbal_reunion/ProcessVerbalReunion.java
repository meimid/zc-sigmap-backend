package com.sigmap.common.process_verbal_reunion;

import com.sigmap.common.entity.BaseEntity;

import com.sigmap.common.process_verbal_reunion_doc.ProcessVerbalReunionDoc;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtrait;
import lombok.*;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "process_verbal_reunion")
public class ProcessVerbalReunion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pv_id")
    private Integer id;


    @Column(name = "observation")
    private String observation;

    @OneToMany(mappedBy = "processVerbal", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    private List<ProcessVerbalReunionDoc> documents = new ArrayList<>();

   /* @OneToMany(mappedBy = "processVerbal", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    private List<ProcessVerbalReunionExtrait> extraits = new ArrayList<>();*/
}
