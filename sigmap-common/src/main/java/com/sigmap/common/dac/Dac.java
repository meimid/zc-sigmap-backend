package com.sigmap.common.dac;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dac")
public class Dac {
    @EmbeddedId
    private DacId id;


}
