package com.sigmap.common.config;

import java.time.LocalDate;

public class Constants {
  // public static final Integer TYPE_SOCIETE = 10;
  // public static final Integer PAYS = 11;
  // public static final Integer CATEGORIE_SOMISSIONAIRE = 12;
  // public static final Integer TYPE_COMITE = 13;
  // public static final Integer TYPE_ADDRESS = 14;
    
  // public static final Integer STATUS = 15;
  // public static final Integer DRAFT_STATUS = 32;
  // public static final Integer TRAITMENT_STATUS = 33;
  // public static final Integer VALIDE_STATUS = 34;
  // public static final Integer PUBLIE_STATUS = 35;
  public static final String PATH_ENTREPOT_GED =
      "/SIGMAP-DEV/ARCHIVE/AGPM/" + LocalDate.now().getYear();
  public static final String PATH_ENTREPOT_GED_PAA =
      "/SIGMAP-DEV/ARCHIVE/PAA/" + LocalDate.now().getYear();
  public static final String PATH_ENTREPOT_GED_REALISATION =
      "/SIGMAP-DEV/ARCHIVE/REALISATION/" + LocalDate.now().getYear();
  public static final String PATH_ENTREPOT_GED_PPM =
      "/SIGMAP-DEV/ARCHIVE/PPM/" + LocalDate.now().getYear();

  // public static final Integer SERVICE = 16;
  // public static final Integer TYPE_MARCHER = 17;
  // public static final Integer SOURCE_FINANCEMENT = 18;
  // public static final Integer MONAIS = 19;
  // public static final Integer MODE_SELECTION = 20;
  // public static final Integer DIRECTION = 21;
  // public static final Integer MODE_PASSATION = 22;

  // public static final Integer AC_COM_STATUS = 23;

  // public static final Integer TUTEL = 24;
    
  // public static final Integer ACTIF_STATUS = 36;
  //
  // public static final Integer NON_ACTIF_STATUS = 37;

  // DAC category
  // public static final Integer CATEGORIE_DAC = 50;
  // DAC dynamic fields type
  // public static final Integer TYPE_FIELD_DAC = 51;

  public static final String AGPM_ID_SEQ = "agpm_id_seq";
  public static final String PAA_ID_SEQ = "paa_id_seq";
  public static final String PPM_ID_SEQ = "ppm_id_seq";
  public static final String REALISATION_ID_SEQ = "realisation_id_seq";
  public static final String LOT_ID_SEQ = "lot_id_seq";
  public static final String DYNA_DOCS_SEQ = "dyna_docs_id_seq";
  public static final String DIRECTION_SEQ = "direction_id_seq";


  private Constants() {}
}
