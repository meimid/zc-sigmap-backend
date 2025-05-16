package com.sigmap.common.docs.file_check;


import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileSizeConversion {

    public long getFileSize(MultipartFile file) {

        if ( file !=null) {
            return file.getSize(); // Returns size in bytes
        }
        return 0;
    }

    public double getFileSizeInMB(MultipartFile file) {
        long sizeInBytes = getFileSize(file);
        // return the file size en MB
        return sizeInBytes / (1024.0 * 1024.0);

    }

    public double getAllFileSizeInMB(Long fileSizeBytes) {
        // return the file size en MB
        return fileSizeBytes / (1024.0 * 1024.0);

    }

}
