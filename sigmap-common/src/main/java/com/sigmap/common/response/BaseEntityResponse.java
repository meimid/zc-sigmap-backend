package com.sigmap.common.response;

import lombok.Data;

import java.util.Calendar;

/**
 * Base Entity Response.
 */
@Data
public class BaseEntityResponse {
    private Calendar createdAt;

    private String createdBy;

    private Calendar updatedAt;

    private String updatedBy;

    private Calendar deletedAt;

    private  String deletedBy;

    private Boolean isDeleted;
}
