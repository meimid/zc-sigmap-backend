package com.sigmap.common.ac;

import com.sigmap.common.com.Com;
import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ac")
public class Ac extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ac_id")
    private Integer id;
    private String nom;
    private String ancrage;
    private String contact;

    @ManyToMany
    @JoinTable(name="com_ac",
            joinColumns = @JoinColumn(name = "ac_id"),
            inverseJoinColumns = @JoinColumn(name = "com_id"))
    private List<Com> coms= new ArrayList<>();
}
