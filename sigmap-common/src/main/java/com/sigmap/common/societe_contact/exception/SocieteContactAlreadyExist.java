package com.sigmap.common.societe_contact.exception;

import lombok.Getter;

@Getter
public class SocieteContactAlreadyExist extends Exception {
    private final Integer id;
    private final Integer contactId;
    private final Integer societeId;

    public SocieteContactAlreadyExist(Integer id, String message) {
        super(message);
        this.id = id;
        this.contactId = null;
        this.societeId = null;
    }

    public SocieteContactAlreadyExist(Integer id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
        this.contactId = null;
        this.societeId = null;
    }

    public SocieteContactAlreadyExist(String message, Integer societeId, Integer contactId) {
        super(message);
        this.id = null;
        this.contactId = contactId;
        this.societeId = societeId;
    }
}