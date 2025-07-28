package com.sigmap.common.ac_seuil;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class AcSeuilId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "ac_id", referencedColumnName = "ac_id")
    private Ac ac;

    @ManyToOne
    @JoinColumn(name = "type_marcher_id")
    private RefParam typeMarcher;

    @ManyToOne
    @JoinColumn(name = "mode_passation_id", referencedColumnName = "mode_passation_id")
    private ModeDePassationSelection modePassation;
}
