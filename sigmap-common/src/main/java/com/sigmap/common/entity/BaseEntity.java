package com.sigmap.common.entity;

import com.sigmap.common.security.Authentication;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Calendar;


/**
 * Base Entity.
 *
 * <p>createdAt,updatedAt,createdBy,updatedBy populated by {@link AuditingEntityListener}</p>
 * <p>createdBy,updatedBy requires {@link org.springframework.data.domain.AuditorAware} to be defined </p>
 * <p>deletedBy,deletedAt,isDeleted populated by {@link com.sigmap.common.repository.SoftDeleteRepository#softDelete}</p>
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at",updatable = false)
    private Calendar createdAt;

    @CreatedBy
    @Column(name = "created_by",updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Calendar updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at")
    private Calendar deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "is_deleted")
    private Boolean isDeleted;


}
