package com.sigmap.common.contact.request;
import lombok.Data;

@Data
public class ContactRequest {
    private Integer id;
    private String nom;
    private String address;
    private String tel1;
    private String tel2;
    private String email;
}
