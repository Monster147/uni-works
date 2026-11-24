package grpcserverapp;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;

import java.io.IOException;

public class FirestoreService {
    private final Firestore db;
    private final String collectionName;

    public FirestoreService() {
        this.collectionName = ConfigManager.getCollectionName();
        try {
            GoogleCredentials credentials =
                    GoogleCredentials.getApplicationDefault();

            FirestoreOptions options = FirestoreOptions.newBuilder()
                    .setDatabaseId(ConfigManager.getDatabaseId())
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

    public void save(String requestId, ImageInfo imageInfo) throws Exception {
        if (requestId == null || requestId.isEmpty()) {
            throw new IllegalArgumentException("RequestId cannot be null or empty");
        }
        CollectionReference colRef = db.collection(collectionName);
        DocumentReference docRef = colRef.document(requestId);
        ApiFuture<WriteResult> resultFut = docRef.set(imageInfo);
        resultFut.get();
    }

    public ImageInfo getImageInfo(String requestId) throws Exception {
        if (requestId == null || requestId.isEmpty()) {
            throw new IllegalArgumentException("RequestId cannot be null or empty");
        }
        DocumentReference docRef = db.collection(collectionName).document(requestId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(ImageInfo.class);
        }
        return null;
    }

    public ApiFuture<QuerySnapshot> getAllDocuments() {
        return db.collection(collectionName).get();
    }
}
