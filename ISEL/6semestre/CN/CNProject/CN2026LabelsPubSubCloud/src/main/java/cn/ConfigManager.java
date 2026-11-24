package cn;

public class ConfigManager {
    private static final String GCP_PROJECT_ID_DEFAULT = "cn2526-t1-g07";
    private static final String GCP_ZONE_DEFAULT = "europe-west1-b";
    private static final String BUCKET_NAME_DEFAULT = "cn2026labels-g07-bucket";
    private static final String COLLECTION_NAME_DEFAULT = "cn2026labels-g07-collection";
    private static final String COLLECTION_MESSAGE_DEFAULT = "CFPubSubMessages";
    private static final String DATABASE_ID_DEFAULT = "cn2026labels-g07-firestore";
    private static final String PUBSUB_TOPIC_DEFAULT = "labels-topic";
    private static final String GRPC_SERVER_NAME_DEFAULT = "cn2026-labels-server-group";
    private static final String WORKERS_GROUP_NAME_DEFAULT = "cn2026-labels-worker-group";
    public ConfigManager() {}
    public static String getProjectId() {
        return getEnvOrDefault("GCP_PROJECT_ID", GCP_PROJECT_ID_DEFAULT);
    }
    public static String getZone() {
        return getEnvOrDefault("GCP_ZONE", GCP_ZONE_DEFAULT);
    }
    public static String getBucketName() {
        return getEnvOrDefault("GCP_BUCKET_NAME", BUCKET_NAME_DEFAULT);
    }
    public static String  getCollectionName() {return getEnvOrDefault("FIRESTORE_COLLECTION", COLLECTION_NAME_DEFAULT);}
    public static String getDatabaseId() {return getEnvOrDefault("FIRESTORE_DATABASE_ID", DATABASE_ID_DEFAULT);}
    public static String getPubSubTopic() {
        return getEnvOrDefault("PUBSUB_TOPIC", PUBSUB_TOPIC_DEFAULT);
    }
    public static String getGrpcServersGroupName() {return getEnvOrDefault("GRPC_SERVERS_GROUP", GRPC_SERVER_NAME_DEFAULT);}
    public static String getWorkersGroupName() {
        return getEnvOrDefault("WORKERS_GROUP", WORKERS_GROUP_NAME_DEFAULT);
    }
    public static String getCollectionMessage() { return getEnvOrDefault("FIRESTORE_COLLECTION_MESSAGE", COLLECTION_MESSAGE_DEFAULT); }
    private static String getEnvOrDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        return value != null && !value.isEmpty() ? value : defaultValue;
    }
}

