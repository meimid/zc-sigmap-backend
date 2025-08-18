package com.sigmap.common.delais_reglementaires;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ref_params.RefParam;
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
public class DelaisReglementairesId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "type_marche_id")
    private RefParam typeMarcher;

    @ManyToOne
    @JoinColumn(name = "mode_passation_id", referencedColumnName = "mode_passation_id")
    private ModeDePassationSelection modePassation;
}
