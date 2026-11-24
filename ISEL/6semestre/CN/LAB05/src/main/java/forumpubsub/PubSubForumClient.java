package forumpubsub;


import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;

import java.io.IOException;
import java.util.Scanner;

public class PubSubForumClient {
    static String projectId;
    static Firestore firestore;

    private static String read(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }

    public static void listTopics() throws IOException {
        TopicAdminClient topicAdmin = TopicAdminClient.create();
        try {
            TopicAdminClient.ListTopicsPagedResponse res = topicAdmin.listTopics(ProjectName.of(projectId));
            for (Topic top : res.iterateAll()) {
                System.out.println("Topic Name=" + top.getName());
            }
        } finally {
            topicAdmin.close();
        }
    }

    public static void publishMessage(String topicID, String jsonData) throws Exception {
        TopicName topicName = TopicName.ofProjectTopicName(projectId, topicID);
        Publisher publisher = Publisher.newBuilder(topicName).build();
        ByteString msgData = ByteString.copyFromUtf8(jsonData);
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
            .setData(msgData)
            .build();
        ApiFuture<String> future = publisher.publish(pubsubMessage);
        String msgID = future.get();
        System.out.println("Message Published with ID=" + msgID);
        publisher.shutdown();
        publisher.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES);
    }

    public static void createSubscription(String topicID, String subscriptionID) throws IOException {
        TopicName topicName = TopicName.of(projectId, topicID);
        SubscriptionName subscriptionName = SubscriptionName.of(projectId, subscriptionID);
        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create();
        try {
            PushConfig pConfig = PushConfig.getDefaultInstance();
            subscriptionAdminClient.createSubscription(subscriptionName, topicName, pConfig, 0);
        } finally {
            subscriptionAdminClient.close();
        }
    }

    public static void deleteSubscription(String subscriptionID) throws IOException {
        SubscriptionName subscriptionName = SubscriptionName.of(projectId, subscriptionID);
        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create();
        try {
            subscriptionAdminClient.deleteSubscription(subscriptionName);
        } finally {
            subscriptionAdminClient.close();
        }
    }

    private static void initializeFirestore() throws Exception {
        GoogleCredentials credentials =
                GoogleCredentials.getApplicationDefault();

        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId("cn-lab05-test1").setCredentials(credentials)
                .build();
        firestore = options.getService();
    }

    static void main(String[] args) throws Exception {
        // - Set environment variable
        //     GOOGLE_APPLICATION_CREDENTIALS="pathname to AccountServiceKEY.json"
        projectId = "cn2526-t1-g07";

        // Initialize Firestore
        initializeFirestore();

        Scanner scanInput = new Scanner(System.in);

        String sender = read("Your name (sender)?", scanInput);

        while (true) {
            System.out.println("\n=== Forum Pub/Sub Client ===");
            System.out.println("1. Publish Message");
            System.out.println("2. Create Subscription");
            System.out.println("3. Pull Messages");
            System.out.println("4. Delete Subscription");
            System.out.println("5. List Topics");
            System.out.println("6. Exit");
            String option = read("Choose an option:", scanInput);

            try {
                switch (option) {
                    case "1":
                        String pubTopicName = read("Topic ID to Publish?", scanInput);
                        String msg = read("Message to Publish?", scanInput);
                        Gson gsonMsg = new Gson();
                        String jsonMsg = gsonMsg.toJson(new ForumMessage(sender, msg));
                        publishMessage(pubTopicName, jsonMsg);
                        break;
                    case "2":
                        String topicID = read("Topic ID to create Subscription?", scanInput);
                        String subsID = read("Subscription ID?", scanInput);
                        createSubscription(topicID, subsID);
                        System.out.println("Subscription created successfully!");
                        break;
                    case "3":
                        String subIdToPull = read("Subscription ID to pull from?", scanInput);
                        String topicNameForPull = read("Topic name associated with this subscription?", scanInput);
                        MessageReceiveHandler.pullMessages(projectId, subIdToPull, topicNameForPull, firestore);
                        break;
                    case "4":
                        String subsIDDelete = read("Subscription ID to delete?", scanInput);
                        deleteSubscription(subsIDDelete);
                        System.out.println("Subscription deleted successfully!");
                        break;
                    case "5":
                        listTopics();
                        break;
                    case "6":
                        firestore.close();
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option!");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                System.err.println(e.getClass().getName() + ": " + e);
            }
        }
    }
}
