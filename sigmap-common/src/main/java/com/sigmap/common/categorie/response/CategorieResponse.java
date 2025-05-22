package com.sigmap.common.categorie.response;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

@Data
public class CategorieResponse {
    private Integer id;
    private String label;
    private BaseEntityResponse metadata;
}
