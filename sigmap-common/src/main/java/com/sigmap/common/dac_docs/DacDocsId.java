package com.sigmap.common.dac_docs;

import com.sigmap.common.dac.Dac;
import jakarta.persistence.*;
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
public class DacDocsId implements Serializable {

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dac_id", referencedColumnName = "dac_id"),
            @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver")
    })
    private Dac dac;

}
