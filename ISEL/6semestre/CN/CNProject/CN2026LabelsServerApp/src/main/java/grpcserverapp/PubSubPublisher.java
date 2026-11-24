package grpcserverapp;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

public class PubSubPublisher {

    private final String projectId;
    private final String topicId;

    public PubSubPublisher(String projectId, String topicId) {
        this.projectId = projectId;
        this.topicId = topicId;
    }

    public void publish(String jsonData) throws Exception {
        ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);

        Publisher publisher = Publisher.newBuilder(topicName).build();

        PubsubMessage message = PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(jsonData)).build();

        ApiFuture<String> future = publisher.publish(message);

        System.out.println("Published message: " + future.get());

        publisher.shutdown();
    }
}