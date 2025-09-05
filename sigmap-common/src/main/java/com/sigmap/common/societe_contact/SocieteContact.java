package com.sigmap.common.societe_contact;

import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "societe_contact")
public class SocieteContact  extends BaseEntity {
    @EmbeddedId
    private SocieteContactId societeContactId;
    @Column(name = "representant_legal")
    private boolean representantLegal;
    private boolean principal;
}
