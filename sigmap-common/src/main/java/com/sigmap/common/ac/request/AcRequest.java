package com.sigmap.common.ac.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcRequest {
    private String nom;
    private String ancrage;
    private String contact;
   private List<Integer> comIds;
   private List<String> userIds;
}
