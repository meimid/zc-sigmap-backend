package com.sigmap.common.societe_contact.Request;

import com.sigmap.common.contact.request.ContactRequest;
import com.sigmap.common.societe.request.SocieteRequest;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContactAndSocieteContactRequest {
    private ContactRequest contact;
    private SocieteContactRequest societeContactRequest;
}
