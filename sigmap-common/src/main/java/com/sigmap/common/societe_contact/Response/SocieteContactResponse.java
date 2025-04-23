package com.sigmap.common.societe_contact.Response;

import com.sigmap.common.contact.response.ContactResponse;
import com.sigmap.common.contact.response.ContactResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import com.sigmap.common.societe.response.SocieteResponse;
import com.sigmap.common.societe.response.SocieteResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SocieteContactResponse {
    private SocieteResponseDto societe;
    private ContactResponseDto contact;
    private boolean representantLegal;
    private boolean principal;
    private BaseEntityResponse metadata;
}
