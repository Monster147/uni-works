package grpcserverapp;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import forum.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ForumServiceImpl extends ForumGrpc.ForumImplBase {

    public ForumServiceImpl() {}

    private final ConcurrentMap<String, ConcurrentMap<String, StreamObserver<ForumMessage>>> topics =
            new ConcurrentHashMap<>();

    @Override
    public void topicSubscribe(SubscribeUnSubscribe request, StreamObserver<ForumMessage> responseObserver) {
        String user = request.getUsrName();
        String topic = request.getTopicName();
        System.out.println("User " + user + " subscribing to " + topic);
        topics.putIfAbsent(topic, new ConcurrentHashMap<>());
        topics.get(topic).put(user, responseObserver);
    }

    @Override
    public void topicUnSubscribe(SubscribeUnSubscribe request, StreamObserver<Empty> responseObserver) {
        String user = request.getUsrName();
        String topic = request.getTopicName();
        ConcurrentMap<String, StreamObserver<ForumMessage>> subscribers = topics.get(topic);
        if (subscribers != null) {
            StreamObserver<ForumMessage> observer = subscribers.remove(user);
            if (observer != null) {
                observer.onCompleted();
            } else {
                responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription("Subscription not found!")));
                return;
            }

            if (subscribers.isEmpty()) {
                topics.remove(topic);
            }
            System.out.println("User " + user + " unsubscribing to " + topic);
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription("Subscription not found!")));
        }
    }

    @Override
    public void getAllTopics(Empty request, StreamObserver<ExistingTopics> responseObserver) {
        ExistingTopics topicList = ExistingTopics.newBuilder()
                .addAllTopicName(topics.keySet())
                .build();
        responseObserver.onNext(topicList);
        responseObserver.onCompleted();
    }

    @Override
    public void publishMessage(ForumMessage request, StreamObserver<Empty> responseObserver) {
        String user = request.getFromUser();
        String topic = request.getTopicName();

        ConcurrentMap<String, StreamObserver<ForumMessage>> subscribers = topics.get(topic);

        if (subscribers == null) {
            responseObserver.onError(
                    new StatusException(Status.NOT_FOUND.withDescription("Topic does not exist"))
            );
            return;
        }

        if (!subscribers.containsKey(user)) {
            responseObserver.onError(
                    new StatusException(Status.PERMISSION_DENIED.withDescription("User not subscribed to topic"))
            );
            return;
        }

        System.out.println("User " + user + " publishing message to " + topic);
        subscribers.forEach((subscriberUser, subscriber) -> {
            if (!subscriberUser.equals(user)) {
                try {
                    subscriber.onNext(request);
                } catch (Exception e) {
                    subscribers.remove(subscriberUser);
                    e.printStackTrace();
                }
            }
        });

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
