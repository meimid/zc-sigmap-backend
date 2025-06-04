package com.sigmap.common.docs.Request;

import com.sigmap.common.docs.Docs;

import java.util.List;

public interface DocumentHolder {
    Integer getId();
    Integer getVersion();
    List<Docs> getDocs();
    void setDocs(List<Docs> docs);
}
