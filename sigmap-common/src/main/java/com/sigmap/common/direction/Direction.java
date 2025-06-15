package com.sigmap.common.direction;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.response.BaseEntityResponse;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "direction")
public class Direction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "direction_id")
    private Integer id;
    private String libelle;
/*    @ManyToMany
    @JoinTable(
            name = "ac_direction",
            joinColumns = @JoinColumn(name = "direction_id", referencedColumnName = "direction_id"),
            inverseJoinColumns = @JoinColumn(name = "ac_id")
    )*/
    @ManyToMany(mappedBy = "directions")
    private List<Ac> acs;


}
