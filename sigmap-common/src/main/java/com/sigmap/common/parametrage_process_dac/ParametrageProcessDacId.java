package com.sigmap.common.parametrage_process_dac;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParametrageProcessDacId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "type_marcher_id")
    private RefParam typeMarcher;

    @ManyToOne
    @JoinColumn(name = "mode_passation_id", referencedColumnName = "mode_passation_id")
    private ModeDePassationSelection modePassation;
}
