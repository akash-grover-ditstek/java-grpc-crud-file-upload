package shared;

import com.example.grpc.fileupload.FileMetadata;
import io.grpc.Context;
import io.grpc.Metadata;

public class Constant {
    public static final Metadata.Key<byte[]> fileMetadataKey = Metadata.Key.of("file-meta-bin", Metadata.BINARY_BYTE_MARSHALLER); //gRPC provides BINARY_BYTE_MARSHALLER to safely serialize complex data like Protobuf messages or file details into binary form
    public static final Context.Key<FileMetadata> fileMetaContext = Context.key("file-meta");
}
