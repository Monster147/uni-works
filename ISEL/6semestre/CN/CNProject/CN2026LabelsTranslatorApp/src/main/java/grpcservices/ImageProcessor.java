package grpcservices;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageProcessor {

    private final Firestore db;
    private final String collectionName = "cn2026labels-g07-collection";

    public ImageProcessor() {
        try {
            GoogleCredentials credentials =
                    GoogleCredentials.getApplicationDefault();

            FirestoreOptions options = FirestoreOptions.newBuilder()
                    .setDatabaseId("cn2026labels-g07-firestore")
                    .setCredentials(credentials)
                    .build();

            db = options.getService();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to get Google credentials. " +
                            "Make sure GOOGLE_APPLICATION_CREDENTIALS " +
                            "is set correctly.",
                    e
            );
        }
    }

    public void process(String requestId, String bucket, String filename) throws Exception {
        Map<String, Double> labelsWithScores = AppMain.detectLabels("gs://" + bucket + "/" + filename);

        List<String> labels =
                new ArrayList<>(labelsWithScores.keySet());

        List<String> translatedLabels =
                AppMain.TranslateLabels(labels);

        List<LabelInfo> labelInfoList = new ArrayList<>();

        for (int i = 0; i < labels.size(); i++) {
            String originalLabel = labels.get(i);
            String translatedLabel = translatedLabels.get(i);
            double score =
                    labelsWithScores.get(originalLabel);

            labelInfoList.add(
                    new LabelInfo(
                            originalLabel,
                            translatedLabel,
                            score
                    )
            );
        }

        ImageInfo imageInfo = new ImageInfo(requestId, filename, Instant.now().toString(), labelInfoList, "COMPLETED");

        save(requestId, imageInfo);

        System.out.println(
                "Image processed: " + requestId
        );
    }

    public void save(String requestId, ImageInfo imageInfo) throws Exception {
        if (requestId == null || requestId.isEmpty()) {
            throw new IllegalArgumentException("RequestId cannot be null or empty");
        }
        CollectionReference colRef = db.collection(collectionName);
        DocumentReference docRef = colRef.document(requestId);
        ApiFuture<WriteResult> resultFut = docRef.set(imageInfo);
        resultFut.get();
    }
}
