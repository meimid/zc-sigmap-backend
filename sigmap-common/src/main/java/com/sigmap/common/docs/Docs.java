package com.sigmap.common.docs;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sigmap.common.entity.BaseEntity;

import com.sigmap.common.paas.Paa;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.realisations.Realisation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "docs")
@Where(clause = "is_deleted = false")
public class Docs extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Integer id;
    private String name;
    private String alfresco_id;


    @ManyToMany(mappedBy = "docs")
    @JsonBackReference
    private List<Realisation> realisations;

    @ManyToMany(mappedBy = "docs")
    @JsonBackReference
    private List<Ppm> ppms;

    @ManyToMany(mappedBy = "docs")
    @JsonBackReference
    private List<Paa> paas;

    @Lob
    @Column(name = "path_folder")
    private String pathFolder;
    @Column(name = "file_size")
    private String fileSize;
    @Column(name = "file_name_alfresco")
    private String AlfrescoName;

    @Column(name = "content_type")
    private String contentType;
    @Column(name = "file_extension")
    private String fileExtension;

}
