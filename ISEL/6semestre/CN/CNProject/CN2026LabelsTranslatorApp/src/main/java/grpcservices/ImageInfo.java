package grpcservices;

import java.util.List;

public class ImageInfo {
    private String requestID;
    private String filename;
    private String processedAt;
    private List<LabelInfo> labelInfo;
    private String status;
    public ImageInfo(){}

    public ImageInfo(String requestID, String filename, String processedAt, List<LabelInfo> labelInfo, String status) {
        this.requestID = requestID;
        this.filename = filename;
        this.processedAt = processedAt;
        this.labelInfo = labelInfo;
        this.status = status;
    }

    public String getRequestID(){
        return requestID;
    }
    public String getFilename(){
        return filename;
    }
    public String getProcessedAt(){
        return processedAt;
    }
    public List<LabelInfo> getLabelInfo(){
        return labelInfo;
    }
    public String getStatus() { return status; }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }
    public void setLabelInfo(List<LabelInfo> labelInfo) {
        this.labelInfo = labelInfo;
    }
    public void setStatus(String status) { this.status = status; }
}
