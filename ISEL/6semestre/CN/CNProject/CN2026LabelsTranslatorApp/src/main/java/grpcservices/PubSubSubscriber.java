package grpcservices;

import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.gson.Gson;
import com.google.pubsub.v1.ProjectSubscriptionName;

public class PubSubSubscriber {

    String projectId = "cn2526-t1-g07";
    String subscriptionId = "labels-topic-sub";
    private final Gson gson = new Gson();

    public void work() {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
        MessageReceiver messageReceiver = (message, consumer) -> {
            try {
                String msgData = message.getData().toStringUtf8();
                PubSubImageMessage data = gson.fromJson(msgData, PubSubImageMessage.class);

                ImageProcessor processor =
                        new ImageProcessor();

                processor.process(
                        data.requestId,
                        data.bucket,
                        data.filename
                );

                consumer.ack();
            } catch (Exception e) {
                System.err.println("Error processing message: " + e.getMessage());
                System.err.println(e.getClass().getName() + ": " + e);
                consumer.nack();
            }
        };

        Subscriber subscriber = Subscriber.newBuilder(subscriptionName, messageReceiver).build();

        System.out.println("Worker started");

        subscriber.startAsync().awaitTerminated();
    }
}
