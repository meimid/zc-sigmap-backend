package com.sigmap.common.dac_docs_details.exceptions;

public class DacDocsDetailsNotFound extends RuntimeException {
  private Integer dynaFieldsDetailsId;
  private Integer rowId;
  private Integer dacId;
  private Integer dacVer;
  private Integer dynaDocsId;
  private Integer dynaDocsVer;
  private Integer dynaFieldsId;

  public DacDocsDetailsNotFound(Integer dynaFieldsDetailsId, Integer rowId, Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId) {
    super("DacDocsDetails not found with dynaFieldsDetailsId: " + dynaFieldsDetailsId + 
          ", rowId: " + rowId + ", dacId: " + dacId + ", dacVer: " + dacVer + 
          ", dynaDocsId: " + dynaDocsId + ", dynaDocsVer: " + dynaDocsVer + 
          ", dynaFieldsId: " + dynaFieldsId);
    this.dynaFieldsDetailsId = dynaFieldsDetailsId;
    this.rowId = rowId;
    this.dacId = dacId;
    this.dacVer = dacVer;
    this.dynaDocsId = dynaDocsId;
    this.dynaDocsVer = dynaDocsVer;
    this.dynaFieldsId = dynaFieldsId;
  }

  public Integer getDynaFieldsDetailsId() {
    return dynaFieldsDetailsId;
  }

  public Integer getRowId() {
    return rowId;
  }

  public Integer getDacId() {
    return dacId;
  }

  public Integer getDacVer() {
    return dacVer;
  }

  public Integer getDynaDocsId() {
    return dynaDocsId;
  }

  public Integer getDynaDocsVer() {
    return dynaDocsVer;
  }

  public Integer getDynaFieldsId() {
    return dynaFieldsId;
  }
} 