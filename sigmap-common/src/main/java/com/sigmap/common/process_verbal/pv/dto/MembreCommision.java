package com.sigmap.common.process_verbal.pv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MembreCommision {
    private Integer ppmId;
    private Integer ppmVersion;
    private Integer processVerbalId;
    private Integer pvMembrePresentId;
    private String nom;
    private String prenom;
    private Integer fonctionId;
    private String fonction;
    private boolean present;
    private String presentStr;

    public MembreCommision(MembreCommisionDTO dto) {
        this.ppmId = dto.getPpmId();
        this.ppmVersion = dto.getPpmVersion();
        this.processVerbalId = dto.getProcessVerbalId();
        this.pvMembrePresentId = dto.getPvMembrePresentId();
        this.nom = dto.getNom()+" "+dto.getPrenom();
        this.prenom = dto.getNom()+" "+dto.getPrenom();
        this.fonctionId = dto.getFonctionId();
        this.fonction = dto.getFonction();
        this.present = dto.getPresent();
        this.presentStr = this.present ? "présent" : "absent";
    }
}
