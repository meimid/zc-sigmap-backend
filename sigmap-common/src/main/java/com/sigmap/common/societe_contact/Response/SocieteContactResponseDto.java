package com.sigmap.common.societe_contact.Response;

import com.sigmap.common.contact.response.ContactResponseDto;
import com.sigmap.common.societe_contact.Request.SocieteContactRequest;
import lombok.Data;

@Data
public class SocieteContactResponseDto {
    private ContactResponseDto contact;
    private SocieteContactRequest societeContact;

}
