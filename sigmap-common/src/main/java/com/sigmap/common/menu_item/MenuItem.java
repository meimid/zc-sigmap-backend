package com.sigmap.common.menu_item;

import com.sigmap.common.authority.Authority;
import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "menu_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    private String label;
    private String icon;
    private String route;
    @Column(name = "order_index")
    private Integer orderIndex;
    // Self-referencing parent-child relationship
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private MenuItem parentId;

    @ManyToOne
    @JoinColumn(name = "authority_code", referencedColumnName = "authority_code")
    private Authority authority;
}
