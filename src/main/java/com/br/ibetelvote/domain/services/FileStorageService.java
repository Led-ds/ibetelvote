package com.br.ibetelvote.domain.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String storeFile(MultipartFile file, String directory) throws IOException;
    void deleteFile(String fileName);
    boolean fileExists(String fileName);
    byte[] loadFile(String fileName) throws IOException;
    String getFileUrl(String fileName);
}
