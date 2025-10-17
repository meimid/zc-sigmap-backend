package com.sigmap.common.ModeDePassationSelection.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModeDePassationSelectionResponseDto {
    private Integer id;
    private String libelle;
    private boolean isRestraint;

}
