package cn;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Logger;

public class Entrypoint implements BackgroundFunction<PSMessage> {
    private static Firestore db = initFirestore();
    private static Firestore initFirestore() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirestoreOptions options = FirestoreOptions.newBuilder()
                    .setDatabaseId(ConfigManager.getDatabaseId()).setCredentials(credentials).build();
            Firestore db = options.getService();
            return db;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final Logger logger = Logger.getLogger(Entrypoint.class.getName());

    @Override
    public void accept(PSMessage message, Context context) throws Exception {
        if (db == null) {
            logger.info("Error connecting to Firestore. Exiting function.");
            throw new RuntimeException("Error connecting to Firestore");
        }
        logger.info("original message " + message.data);
        String dataAsString = new String(Base64.getDecoder().decode(message.data));
        logger.info(dataAsString);
        CollectionReference colRef = db.collection(ConfigManager.getCollectionMessage());
        // O message ID vem no eventID
        DocumentReference docRef = colRef.document(""+context.eventId());
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("msg-data", message.data);
        map.put("data", dataAsString);
        if (dataAsString.compareTo("error") == 0)
            throw new Exception("error forced from data");
        map.put("ctx-messageId", context.eventId());
        map.put("ctx-pubTime", context.timestamp());
        ApiFuture<WriteResult> result = docRef.set(map);
        result.get();
        logger.info("Event was written to Firestore.");
    }

}
