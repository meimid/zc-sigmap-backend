package com.sigmap.common.dac;

import java.io.File;

public interface DacService {
    public File replaceBookmarksAndGenerateDOC(Integer dacId, Integer dacVersion ,String basePath, String customDirPath) ;
}
