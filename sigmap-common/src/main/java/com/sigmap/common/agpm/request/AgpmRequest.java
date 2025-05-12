package com.sigmap.common.agpm.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Request DTO for creating or updating an Agpm entity.
 */
@Data
public class AgpmRequest {
    private Integer id;
    private Integer version = 1;
    private String ref;
    private String nom;
    private String contenu;
    private List<Integer> docIds;
    private String remarque;
    private List<MultipartFile> files;
}