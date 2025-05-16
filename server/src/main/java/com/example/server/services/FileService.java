package com.example.server.services;

import com.example.grpc.fileupload.*;
import com.example.server.repository.FileRepository;
import com.example.server.utils.DBFileStorage;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.Constant;


@GrpcService
public class FileService extends FileUploadServiceGrpc.FileUploadServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

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
}
