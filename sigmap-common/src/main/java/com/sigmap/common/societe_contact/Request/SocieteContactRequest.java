package com.sigmap.common.societe_contact.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SocieteContactRequest {
    private Integer societeId;
    private Integer contactId;
    private boolean representantLegal;
    private boolean principal;

}
