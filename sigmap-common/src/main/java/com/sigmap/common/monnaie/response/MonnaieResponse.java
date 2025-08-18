package com.sigmap.common.monnaie.response;

import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

/**
 * A Response object for the {@link Monnaie} entity.
 */
@Data
public class MonnaieResponse {
    private Integer id;

    private String code;

    private String description;

    private Double taux;

    private BaseEntityResponse metadata;


}
