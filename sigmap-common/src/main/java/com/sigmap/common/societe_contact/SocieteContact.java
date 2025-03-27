package com.sigmap.common.societe_contact;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "societe_contact")
public class SocieteContact {
    @EmbeddedId
    private SocieteContactId societeContactId;
    @Column(name = "representant_legal")
    private boolean representantLegal;
    private boolean principal;
}
