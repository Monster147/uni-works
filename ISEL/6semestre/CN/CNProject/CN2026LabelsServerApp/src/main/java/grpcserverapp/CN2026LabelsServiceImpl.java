package grpcserverapp;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.InstanceGroupManagersClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import CN2026LabelsStubs.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class CN2026LabelsServiceImpl extends CN2026LabelsServiceGrpc.CN2026LabelsServiceImplBase {
    public CN2026LabelsServiceImpl(){}

    private final String projectId = ConfigManager.getProjectId();
    private final String zone = ConfigManager.getZone();
    private final String bucketName = ConfigManager.getBucketName();
    private final String workersGroupName = ConfigManager.getWorkersGroupName();
    private final String grpcServersGroupName = ConfigManager.getGrpcServersGroupName();

    private final StorageService storageService = new StorageService();
    private final PubSubPublisher publisher =
        new PubSubPublisher(ConfigManager.getProjectId(), ConfigManager.getPubSubTopic());
    private final FirestoreService firestoreService = new FirestoreService();

    @Override
    public StreamObserver<ImageChunk> submitImage(StreamObserver<ImageResponse> responseObserver) {
        System.out.println("submitImage called! returned a stream to receive requests");
        return new StreamObserver<ImageChunk>() {
            private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            private String filename;
            private final String id = UUID.randomUUID().toString();
            @Override
            public void onNext(ImageChunk value) {
                try{
                    filename = value.getFilename();
                    buffer.write(value.getData().toByteArray());
                }catch (Exception e){
                    responseObserver.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("client error while sending image -> " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("client completed requests -> completed responses");
                try{
                    byte[] imageBytes = buffer.toByteArray();

                    storageService.upload(bucketName, filename, imageBytes);
                    System.out.println("Image uploaded: " + filename + " with id " + id);

                    try {
                        PubSubImageMessage imageMessage = new PubSubImageMessage(id, bucketName, filename);
                        Gson gson = new Gson();
                        String jsonData = gson.toJson(imageMessage);
                        publisher.publish(jsonData);
                        System.out.println("Published to Pub/Sub for processing: " + id);
                    } catch (Exception e) {
                        System.err.println("Failed to publish to Pub/Sub: " + e.getMessage());
                    }

                    ImageResponse response =
                            ImageResponse.newBuilder()
                                    .setRequestID(id)
                                    .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }catch (Exception e){
                    System.err.println("Error in submitImage onCompleted: " + e.getMessage());
                    e.printStackTrace();
                    responseObserver.onError(e);
                }
            }
        };
    }

    @Override
    public void findImageLabelById(ImageLabelRequest request, StreamObserver<ImageLabelResponse> responseObserver) {
        try{
            ImageInfo image = firestoreService.getImageInfo(request.getRequestID());
            if(image == null){
                responseObserver.onError(new RuntimeException("Image not found"));
                return;
            }
            ImageLabelResponse.Builder builder =
                    ImageLabelResponse.newBuilder()
                            .setRequestID(image.getRequestID())
                            .setFilename(image.getFilename())
                            .setProcessedAt(image.getProcessedAt());

            image.getLabelInfo().forEach(label -> {
                builder.addLabels(
                        Label.newBuilder()
                                .setOriginalLanguage(label.getOriginal())
                                .setTranslated(label.getTranslated())
                                .setScore(label.getScore())
                                .build()
                );
            });

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }catch (Exception e){
            responseObserver.onError(e);
        }
    }

    @Override
    public void searchImage(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
        try {
            System.out.println("Searching images - From: " + request.getFromDate() + ", To: " + request.getToDate() + ", Label: " + request.getLabel());

            LocalDate fromDate = request.getFromDate().isEmpty()
                    ? LocalDate.MIN
                    : LocalDate.parse(request.getFromDate());

            LocalDate toDate = request.getToDate().isEmpty()
                    ? LocalDate.MAX
                    : LocalDate.parse(request.getToDate());

            String searchLabel = request.getLabel();
            List<String> matchingFilenames = new ArrayList<>();

            QuerySnapshot querySnapshot = firestoreService.getAllDocuments().get();

            querySnapshot.getDocuments().forEach(document -> {
                try {
                    ImageInfo imageInfo = document.toObject(ImageInfo.class);

                    if (imageInfo == null) return;

                    LocalDate processedDate =
                            OffsetDateTime.parse(imageInfo.getProcessedAt())
                                    .toLocalDate();

                    boolean withinDateRange =
                            (processedDate.isAfter(fromDate) || processedDate.isEqual(fromDate))
                                    &&
                                    (processedDate.isBefore(toDate) || processedDate.isEqual(toDate));

                    if (withinDateRange) {
                        boolean labelMatches = imageInfo.getLabelInfo().stream()
                                .anyMatch(label ->
                                        label.getTranslated().toLowerCase().contains(searchLabel.toLowerCase())
                                                || label.getOriginal().toLowerCase().contains(searchLabel.toLowerCase())
                                );

                        if (labelMatches || searchLabel.isEmpty()) {
                            matchingFilenames.add(imageInfo.getFilename());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing document: " + e.getMessage());
                }
            });
            System.out.println("Found " + matchingFilenames.size() + " matching images");
            SearchResponse response = SearchResponse.newBuilder()
                    .addAllFilenames(matchingFilenames)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void downloadImage(ImageLabelRequest request, StreamObserver<ImageChunk> responseObserver) {
        try {
            ImageInfo image = firestoreService.getImageInfo(request.getRequestID());
            if (image == null) {
                responseObserver.onError(new RuntimeException("Image not found"));
                return;
            }

            byte[] imageBytes = storageService.download(bucketName, image.getFilename());

            int chunkSize = 1024 * 256; // 256KB
            int offset = 0;

            while (offset < imageBytes.length) {
                int end = Math.min(offset + chunkSize, imageBytes.length);
                ImageChunk chunk = ImageChunk.newBuilder()
                        .setFilename(image.getFilename())
                        .setData(com.google.protobuf.ByteString.copyFrom(imageBytes, offset, end - offset))
                        .build();
                responseObserver.onNext(chunk);
                offset = end;
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void scaleGrpcServers(ScaleRequest request, StreamObserver<ScaleResponse> responseObserver) {
        try {
            int wantedInstances = request.getWantedInstances();

            if (wantedInstances <= 0) {
                responseObserver.onError(new IllegalArgumentException("Wanted instances must be greater than 0"));
                return;
            }

            System.out.println("Scaling gRPC servers to " + wantedInstances + " instances");
            resizeInstanceGroup(projectId, zone, grpcServersGroupName, wantedInstances);

            String message = "Scaling gRPC servers to " + wantedInstances + " instances completed";
            ScaleResponse response = ScaleResponse.newBuilder()
                    .setMessage(message)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Error scaling gRPC servers: " + e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void scaleWorkers(ScaleRequest request, StreamObserver<ScaleResponse> responseObserver) {
        try {
            int wantedInstances = request.getWantedInstances();

            if (wantedInstances <= 0) {
                responseObserver.onError(new IllegalArgumentException("Wanted instances must be greater than 0"));
                return;
            }

            System.out.println("Scaling workers to " + wantedInstances + " instances");
            resizeInstanceGroup(projectId, zone, workersGroupName, wantedInstances);

            String message = "Scaling workers to " + wantedInstances + " instances completed";
            ScaleResponse response = ScaleResponse.newBuilder()
                    .setMessage(message)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.err.println("Error scaling workers: " + e.getMessage());
            responseObserver.onError(e);
        }
    }

    private void resizeInstanceGroup(String project, String zone, String instanceGroupName, int newSize) throws IOException, InterruptedException, ExecutionException {
        System.out.println("================== Resizing instance group: " + instanceGroupName);
        InstanceGroupManagersClient managersClient = InstanceGroupManagersClient.create();
        OperationFuture<Operation, Operation> result = managersClient.resizeAsync(
                project,
                zone,
                instanceGroupName,
                newSize
        );
        Operation oper = result.get();
        System.out.println("Resizing with status " + oper.getStatus());
    }
}
