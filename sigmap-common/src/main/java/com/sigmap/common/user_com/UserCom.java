package com.sigmap.common.user_com;


import com.sigmap.common.ac.Ac;
import com.sigmap.common.com.Com;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.user_ac.UserAcId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "user_com")
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(UserComId.class)
@EntityListeners(AuditingEntityListener.class)

public class UserCom extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "com_id")
    private Integer comId;

    @ManyToOne
    @JoinColumn(name = "com_id", insertable = false, updatable = false)
    private Com com;

    @Enumerated(EnumType.STRING)
    private ActiveStatus status;

    private boolean responsable;



}
