package com.sigmap.common.user_com.response;

import com.sigmap.common.com.Com;
import com.sigmap.common.com.response.ComResponseDto;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.response.BaseEntityResponse;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class UserComResponse {
    private String userId;
    private Integer comId;
    private ComResponseDto com;
    private ActiveStatus status;
    private boolean responsable;
    private BaseEntityResponse metadata;

}
