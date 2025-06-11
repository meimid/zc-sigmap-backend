package com.sigmap.common.user_ac;


import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "user_ac")
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(UserAcId.class)
@EntityListeners(AuditingEntityListener.class)

public class UserAc extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "ac_id")
    private Integer acId;

    @ManyToOne
    @JoinColumn(name = "ac_id", insertable = false, updatable = false)
    private Ac ac;

    @Column(name = "ac_pilote")
    private Boolean acPilote = false;


    @Enumerated(EnumType.STRING)
    private ActiveStatus status;



}
