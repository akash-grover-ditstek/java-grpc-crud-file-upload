package com.example.client.service.impl;

import com.example.client.service.IFileUpload;
import com.example.grpc.*;
import com.google.protobuf.ByteString;
import com.shared.proto.Constant;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class FileUploadImpl implements IFileUpload {

    @GrpcClient("grpc-client")
    private FileUploadServiceGrpc.FileUploadServiceStub fileUploadServiceStub;


    @Override
    public String uploadFile(MultipartFile multipartFile) {
        String fileName;
        int fileSize;
        InputStream inputStream;
        fileName = multipartFile.getOriginalFilename();

        try {
            fileSize = multipartFile.getBytes().length;
            inputStream = multipartFile.getInputStream();
        } catch (IOException e) {
            return "unable to extract file info";
        }

        StringBuilder response = new StringBuilder();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Metadata metadata = new Metadata();
        metadata.put(Constant.fileMetadataKey,
                FileMetadata.newBuilder()
                        .setFileNameWithType(fileName)
                        .setContentLength(fileSize)
                        .build()
                        .toByteArray());

        StreamObserver<FileUploadRequest> fileUploadRequestStreamObserver =
                fileUploadServiceStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata)).uploadFile(new StreamObserver<>() {
                    @Override
                    public void onNext(FileUploadResponse fileUploadResponse) {
                        response.append(fileUploadResponse.getUploadStatus());
                    }

                    @Override
                    public void onError(Throwable t) {
                        response.append(UploadStatus.FAILED);
                        t.printStackTrace();
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        countDownLatch.countDown();
                    }
                });
        byte[] fiveKb = new byte[64 * 1024];

        int length;

        try {
            while ((length = inputStream.read(fiveKb)) > 0) {
                log.info(String.format("sending %d length of data", length));
                var request = FileUploadRequest
                        .newBuilder()
                        .setFile(File.newBuilder()
                                .setContent(ByteString.copyFrom(fiveKb, 0, length))
                                .build()
                        ).build();
                //sending the request that contains the chunked data of file
                fileUploadRequestStreamObserver.onNext(request);
            }
            inputStream.close();
            fileUploadRequestStreamObserver.onCompleted();
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
            response.append(UploadStatus.FAILED);
        }
        return response.toString();
    }

    @Override
    public String downloadFile(String fileName) {

        String downloadDir = "C:\\projects\\Downloads";
        java.io.File directory = new java.io.File(downloadDir);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to create download directory: " + downloadDir);
        }

        String destinationPath = new java.io.File(directory, fileName).getAbsolutePath();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        FileDownloadRequest request = FileDownloadRequest.newBuilder()
                .setFileName(fileName)
                .build();

        try (FileOutputStream outputStream = new FileOutputStream(destinationPath)) {
            StreamObserver<FileDownloadResponse> responseObserver = new StreamObserver<>() {
                @Override
                public void onNext(FileDownloadResponse response) {
                    try {
                        outputStream.write(response.getContent().toByteArray());
                    } catch (IOException e) {
                        onError(e);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    error.set(t);
                    latch.countDown();
                }

                @Override
                public void onCompleted() {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        onError(e);
                    }
                    latch.countDown();
                }
            };

            fileUploadServiceStub.downloadFile(request, responseObserver);

            //latch.await();

            if (error.get() != null) {
                throw new RuntimeException("Download failed", error.get());
            }

            return destinationPath;

        } catch (IOException e) {
            throw new RuntimeException("Download failed: " + e.getMessage());
        }
    }

}
