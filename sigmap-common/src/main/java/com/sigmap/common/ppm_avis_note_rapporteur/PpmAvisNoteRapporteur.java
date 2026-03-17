package com.sigmap.common.ppm_avis_note_rapporteur;

import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ppm_avis_note_rapporteur")
@Getter
@Setter
public class PpmAvisNoteRapporteur extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "process_instance_id", nullable = false)
    private String processInstanceId;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "ppm_id", nullable = false)
    private Integer ppmId;

    @Column(name = "ppm_ver", nullable = false)
    private Integer ppmVer;

    @Column(name = "rapporteur_user_id", nullable = false)
    private String rapporteurUserId;

    @Column(name = "avis", columnDefinition = "TEXT")
    private String avis;

    @Column(name = "doc_id_note")
    private Integer docIdNote;
}