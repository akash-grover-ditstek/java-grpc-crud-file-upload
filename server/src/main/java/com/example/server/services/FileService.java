package com.example.server.services;

import com.example.grpc.*;
import com.example.server.entity.FileEntity;
import com.example.server.repository.FileRepository;
import com.example.server.utils.DBFileStorage;
import com.google.protobuf.ByteString;
import com.shared.proto.Constant;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Optional;

@GrpcService
@Slf4j
public class FileService extends FileUploadServiceGrpc.FileUploadServiceImplBase {

    @Autowired
    private FileRepository fileRepository;

    @Override
    public StreamObserver<FileUploadRequest> uploadFile(StreamObserver<FileUploadResponse> responseObserver) {
        FileMetadata fileMetadata = Constant.fileMetaContext.get();
        DBFileStorage dbFileStorage = new DBFileStorage(fileRepository);

        return new StreamObserver<FileUploadRequest>() {
            @Override
            public void onNext(FileUploadRequest fileUploadRequest) {
                log.info("Received {} bytes of data", fileUploadRequest.getFile().getContent().size());
                try {
                    fileUploadRequest.getFile().getContent().writeTo(dbFileStorage.getStream());
                } catch (Exception e) {
                    responseObserver.onError(
                            Status.INTERNAL.withDescription("Error writing data: " + e.getMessage())
                                    .withCause(e)
                                    .asRuntimeException()
                    );
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Upload failed: {}", throwable.getMessage(), throwable);
                dbFileStorage.close();
            }

            @Override
            public void onCompleted() {
                try {
                    int totalBytesReceived = dbFileStorage.getTotalBytes();
                    if (totalBytesReceived != fileMetadata.getContentLength()) {
                        responseObserver.onError(
                                Status.FAILED_PRECONDITION
                                        .withDescription(String.format("Expected %d bytes but received %d bytes",
                                                fileMetadata.getContentLength(), totalBytesReceived))
                                        .asRuntimeException()
                        );
                        return;
                    }

                    // Save to DB
                    dbFileStorage.saveToDatabase(fileMetadata.getFileNameWithType());

                    //Close the stream
                    dbFileStorage.close();

                    // Send response
                    responseObserver.onNext(
                            FileUploadResponse.newBuilder()
                                    .setFileName(fileMetadata.getFileNameWithType())
                                    .setUploadStatus(UploadStatus.SUCCESS)
                                    .build()
                    );
                    responseObserver.onCompleted();
                    log.info("File upload completed successfully for: {}", fileMetadata.getFileNameWithType());
                } catch (Exception e) {
                    responseObserver.onError(
                            Status.INTERNAL.withDescription("Failed to save data: " + e.getMessage())
                                    .withCause(e)
                                    .asRuntimeException()
                    );
                }
            }
        };

    }

    @Override
    public void downloadFile(FileDownloadRequest request, StreamObserver<FileDownloadResponse> responseObserver) {
        String fileName = request.getFileName();

        Optional<FileEntity> file = fileRepository.findByFileName(fileName);

        if (file.isEmpty()) {
            responseObserver.onError(new RuntimeException("File not found in database: " + fileName));
            return;
        }

        byte[] fileData = file.get().getFileData();

        int chunkSize = 64 * 1024; // 64 KB
        int offset = 0;

        while (offset < fileData.length) {
            int end = Math.min(offset + chunkSize, fileData.length);
            byte[] chunk = Arrays.copyOfRange(fileData, offset, end);

            FileDownloadResponse response = FileDownloadResponse.newBuilder()
                    .setContent(ByteString.copyFrom(chunk))
                    .build();

            responseObserver.onNext(response);
            offset = end;
        }

        responseObserver.onCompleted();
    }
}
