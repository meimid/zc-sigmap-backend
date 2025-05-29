package com.sigmap.common.user_ac;

import com.sigmap.common.ac.Ac;
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
public class UserAcId implements Serializable {
    @Column(name = "user_id")
    private String userId;

    @Column(name = "ac_id")
    private Integer acId;
}