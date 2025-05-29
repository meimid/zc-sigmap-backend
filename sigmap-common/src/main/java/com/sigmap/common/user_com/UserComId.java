package com.sigmap.common.user_com;

import com.sigmap.common.com.Com;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserComId implements Serializable {
    @Column(name = "user_id")
    private String userId;

    @Column(name = "com_id")
    private Integer comId;
}