package grpcservices;

public class PubSubImageMessage {
    public String requestId;
    public String bucket;
    public String filename;

    public PubSubImageMessage(String requestId, String bucket, String filename) {
        this.requestId = requestId;
        this.bucket = bucket;
        this.filename = filename;
    }
}