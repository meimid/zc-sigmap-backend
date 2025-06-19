package com.sigmap.common.direction;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;



@Entity
@Table(name = "direction")
@IdClass(DirectionId.class)
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
@Data
public class Direction extends BaseEntity {

    @Id
    @Column(name = "ac_id")
    private Integer acId;
    @Id
    @Column(name = "direction_id")
    private Integer id;
    private String libelle;

}
