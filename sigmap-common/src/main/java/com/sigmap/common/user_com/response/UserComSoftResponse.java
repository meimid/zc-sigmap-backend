package com.sigmap.common.user_com.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserComSoftResponse {
    private String id;
    private String lastName;
    private String firstName;
    private String email;
    private String tel;
    private String adresse;
}
