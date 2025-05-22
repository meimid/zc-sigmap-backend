package com.sigmap.common.dac;

import java.io.File;

public interface DacService {
    public File replaceBookmarksAndGeneratePDF(Integer dacId, Integer dacVersion) throws Exception;
}
