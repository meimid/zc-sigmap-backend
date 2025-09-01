package com.sigmap.common.docs.file_check;

import com.sigmap.common.agpm.exceptions.MaxUploadSizeFile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class FileCheckServices {

    private final FileSizeConversion fileSizeConversion;

    public void checkFileUpload(List<MultipartFile> files , long maxFileUploadSize ) throws MaxUploadSizeFile {
        if(files!=null && !files.isEmpty()) {

        long totalSizeInBytes = 0;

        for (MultipartFile file : files) {
            long sizeInBytes = fileSizeConversion.getFileSize(file);
            double sizeInMB = fileSizeConversion.getFileSizeInMB(file);
            totalSizeInBytes += sizeInBytes;
            log.debug("File with name : {} have size : {} MB", file.getOriginalFilename() ,sizeInMB);

            if (sizeInBytes > maxFileUploadSize) {
                throw new MaxUploadSizeFile("",maxFileUploadSize);
            }
        }

        double totalFilesSizeInMB = fileSizeConversion.getAllFileSizeInMB(totalSizeInBytes);

        log.debug("All File sizes is .... : {} MB", totalFilesSizeInMB);

        if (totalSizeInBytes > maxFileUploadSize) {
            throw new MaxUploadSizeFile("",maxFileUploadSize);
        }
    }
}
}
