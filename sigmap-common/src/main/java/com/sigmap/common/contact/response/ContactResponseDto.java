package com.sigmap.common.contact.response;

import lombok.Data;

@Data
public class ContactResponseDto {
    private Integer id;
    private String nom;
    private String address;
    private String tel1;
    private String tel2;
    private String email;
}
