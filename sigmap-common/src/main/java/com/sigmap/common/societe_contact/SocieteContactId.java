package com.sigmap.common.societe_contact;

import com.sigmap.common.contact.Contact;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Embeddable
@Data
public class SocieteContactId {
    @ManyToOne
    @JoinColumn(name = "societe_id", nullable = false)
    private Societe societe;

    @ManyToOne
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;
}
