package com.sigmap.common.response;

import jakarta.persistence.Column;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Calendar;

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
