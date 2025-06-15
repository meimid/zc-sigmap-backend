package com.sigmap.common.ModeDePassationSelection;

import com.sigmap.common.ref_params.exceptions.RefParamNotFound;


import java.util.List;

public interface ModeDePassationSelectionService {
    List<ModeDePassationSelection> findByCategorieId(Integer categorieId) throws RefParamNotFound;

}
