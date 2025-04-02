package com.sigmap.common.contact.response;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

@Data
public class ContactResponse {
    private Integer id;
    private String nom;
    private String address;
    private String tel1;
    private String tel2;
    private String email;
    private BaseEntityResponse metadata;
}
