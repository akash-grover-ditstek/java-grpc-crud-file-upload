package com.example.server.utils;

import com.example.server.entity.FileEntity;
import com.example.server.repository.FileRepository;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class DBFileStorage {

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final FileRepository fileRepository;

    public DBFileStorage(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public OutputStream getStream() {
        return byteArrayOutputStream;
    }

    public void saveToDatabase(String fileNameWithType) {
        byte[] fileData = byteArrayOutputStream.toByteArray();
        FileEntity fileEntity = new FileEntity(fileNameWithType, fileData);
        fileRepository.save(fileEntity);
    }

    public int getTotalBytes() {
        return byteArrayOutputStream.size();
    }

    public void close() {
        try {
            byteArrayOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
