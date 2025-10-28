package com.sigmap.common.societe_dac;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
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
public class SocieteDacID implements Serializable {

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dac_id", referencedColumnName = "dac_id"),
            @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver")
    })
    private Dac dac;

    @ManyToOne
    @JoinColumn(name = "societe_id", referencedColumnName = "societe_id")
    private Societe societe;

}
