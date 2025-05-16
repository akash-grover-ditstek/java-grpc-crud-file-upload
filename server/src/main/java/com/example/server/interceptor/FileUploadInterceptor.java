package com.example.server.interceptor;

import com.example.grpc.fileupload.FileMetadata;
import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import shared.Constant;

@GrpcGlobalServerInterceptor
public class FileUploadInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        FileMetadata fileMetadata = null;
        if (metadata.containsKey(Constant.fileMetadataKey)) {
            byte[] fileMetadataBytes = metadata.get(Constant.fileMetadataKey);
            try {
                fileMetadata = FileMetadata.parseFrom(fileMetadataBytes);
            } catch (Exception e) {
                Status status = Status.INTERNAL.withDescription("unable to create file metadata");
                serverCall.close(status, metadata);
            }
            Context context = Context.current().withValue(
                    Constant.fileMetaContext,
                    fileMetadata
            );
            return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
        }
        return new ServerCall.Listener<>() {
        };
    }
}
