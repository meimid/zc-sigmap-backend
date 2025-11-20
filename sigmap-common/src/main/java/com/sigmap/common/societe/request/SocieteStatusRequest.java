package com.sigmap.common.societe.request;

import com.sigmap.common.entity.SocieteStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SocieteStatusRequest {
    private boolean approved;

}
