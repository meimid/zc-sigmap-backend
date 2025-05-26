package com.sigmap.common.dac;

import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dac")
public class Dac extends BaseEntity {
    @EmbeddedId
    private DacId id;


}
