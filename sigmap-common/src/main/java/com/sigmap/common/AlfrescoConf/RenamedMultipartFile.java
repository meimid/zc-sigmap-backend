package com.sigmap.common.AlfrescoConf;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

// inner class to wrap MultipartFile with a custom file name
public   class RenamedMultipartFile implements MultipartFile {
    private final MultipartFile originalFile;
    private final String newFileName;
    private final byte[] content;

    public RenamedMultipartFile(MultipartFile originalFile, String newFileName) throws IOException {
        this.originalFile = originalFile;
        this.newFileName = newFileName;
        this.content = originalFile.getBytes(); // Cache the content
    }

    @Override
    public String getName() {
        return originalFile.getName();
    }

    @Override
    public String getOriginalFilename() {
        return newFileName; // Use the new file name
    }

    @Override
    public String getContentType() {
        return originalFile.getContentType();
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        originalFile.transferTo(dest);
    }
}