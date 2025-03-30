package com.sigmap.common.lots.response;

import lombok.Data;

import java.util.Date;

@Data
public class LotResponse {
    private Integer id;
    private Integer version;
    private String description;
    private Metadata metadata;

    @Data
    public static class Metadata {
        private Date createdAt;
        private String createdBy;
        private Date updatedAt;
        private String updatedBy;
        private Boolean isDeleted;
    }
}