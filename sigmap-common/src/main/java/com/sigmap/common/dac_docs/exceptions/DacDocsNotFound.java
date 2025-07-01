package com.sigmap.common.dac_docs.exceptions;

public class DacDocsNotFound extends RuntimeException {
  private Integer dacId;
  private Integer dacVer;
  private Integer dynaDocsId;
  private Integer dynaDocsVer;
  private Integer dynaFieldsId;

  public DacDocsNotFound(Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId) {
    super("DacDocs not found with dacId: " + dacId + ", dacVer: " + dacVer + 
          ", dynaDocsId: " + dynaDocsId + ", dynaDocsVer: " + dynaDocsVer + 
          ", dynaFieldsId: " + dynaFieldsId);
    this.dacId = dacId;
    this.dacVer = dacVer;
    this.dynaDocsId = dynaDocsId;
    this.dynaDocsVer = dynaDocsVer;
    this.dynaFieldsId = dynaFieldsId;
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