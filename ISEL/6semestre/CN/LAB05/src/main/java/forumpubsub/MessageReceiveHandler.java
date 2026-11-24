package forumpubsub;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MessageReceiveHandler implements MessageReceiver {
    private final Firestore firestore;
    private final String topicName;
    private final String subscriptionName;
    private final Gson gson = new Gson();

    public MessageReceiveHandler(Firestore firestore, String topicName, String subscriptionName) {
        this.firestore = firestore;
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
    }

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            String msgData = message.getData().toStringUtf8();
            ForumMessage forumMsg = gson.fromJson(msgData, ForumMessage.class);

            System.out.println("Received message from " + forumMsg.getSender() + ": " + forumMsg.getMessage());
            saveToFirestore(forumMsg);

            consumer.ack();
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            System.err.println(e.getClass().getName() + ": " + e);
            consumer.nack();
        }
    }

    private void saveToFirestore(ForumMessage forumMsg) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("sender", forumMsg.getSender());
            data.put("message", forumMsg.getMessage());
            data.put("topic", topicName);
            data.put("subscription", subscriptionName);
            data.put("receivedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            firestore.collection("messages").document().set(data).get();
            System.out.println("Message saved to Firestore");
        } catch (Exception e) {
            System.err.println("Error saving to Firestore: " + e.getMessage());
            System.err.println(e.getClass().getName() + ": " + e);
        }
    }

    public static void pullMessages(String projectId, String subscriptionId, String topicName, Firestore firestore) {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
        MessageReceiveHandler handler = new MessageReceiveHandler(firestore, topicName, subscriptionId);

        Subscriber subscriber = Subscriber.newBuilder(subscriptionName, handler)
            .setParallelPullCount(1)
            .build();
        subscriber.startAsync().awaitRunning();

        System.out.println("Listening for messages on subscription: " + subscriptionId);
        System.out.println("Press Enter to stop listening...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        System.out.println("Stopping subscriber...");
        subscriber.stopAsync();
    }
}
