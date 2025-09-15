package com.sigmap.common.parametrage_process_dac;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "paramertage_process_dac")
public class ParametrageProcessDac {
    @EmbeddedId
    private ParametrageProcessDacId id;

    @Column(name = "process", length = 255, nullable = false)
    private String process;
}
