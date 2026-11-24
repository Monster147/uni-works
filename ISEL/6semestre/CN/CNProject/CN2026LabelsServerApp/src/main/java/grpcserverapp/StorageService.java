package grpcserverapp;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;

import java.nio.ByteBuffer;

public class StorageService {
    private final Storage storage;

    public StorageService(){
        this.storage= StorageOptions.getDefaultInstance().getService();
    }

    public void upload(String bucketName, String blobName, byte[] data){
        BlobId blobId = BlobId.of(bucketName, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        if(data.length>1_000_000){
            try (WriteChannel channel = storage.writer(blobInfo)){
                int offset = 0;
                int chunkSize = 1024 * 64;
                while(offset < data.length){
                    int remaining = Math.min(chunkSize, data.length-offset);
                    channel.write(ByteBuffer.wrap(data, offset, remaining));
                    offset+=remaining;
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed streaming upload", e);
            }
        }else {
            storage.create(blobInfo, data);
        }
        storage.createAcl(
                blobId,
                Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)
        );
    }

    public byte[] download(String bucketName, String blobName){
        BlobId blobId = BlobId.of(bucketName, blobName);
        Blob blob = storage.get(blobId);
        if(blob == null){
            throw new RuntimeException("Blob not found: " + bucketName + "/" + blobName);
        }
        byte[] content = blob.getContent();
        return content;
    }
}
