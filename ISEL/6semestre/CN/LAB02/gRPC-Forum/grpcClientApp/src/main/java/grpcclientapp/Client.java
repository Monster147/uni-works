package grpcclientapp;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import forum.*;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Client {
    // generic ClientApp for Calling a grpc Service
    private static String svcIP = "localhost";
    private static int svcPort = 8000;
    private static ForumGrpc.ForumBlockingStub blockingStub;
    private static ForumGrpc.ForumStub noBlockStub;

    private static final Map<String, StreamObserver<ForumMessage>> activeSubscriptions =
            new ConcurrentHashMap<>();

    private static String username;

    static void main(String[] args) {
        try {
            if (args.length == 2) {
                svcIP = args[0]; svcPort = Integer.parseInt(args[1]);
            }
            System.out.println("connect to " + svcIP + ":" + svcPort);
            // Channels are secure by default (via SSL/TLS).
            // For the example we disable TLS to avoid
            // needing certificates.
            ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
                    // Channels are secure by default (via SSL/TLS).
                    // For the example we disable TLS to avoid
                    // needing certificates.
                    .usePlaintext()
                    .build();
            blockingStub = ForumGrpc.newBlockingStub(channel);
            noBlockStub = ForumGrpc.newStub(channel);

            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter username: ");
            username = scanner.nextLine();

            // Call service operations for example ping server
            while (true) {
                try {
                    int option = Menu(scanner);
                    switch (option) {
                        case 1 -> subscribe(scanner);
                        case 2 -> unsubscribe(scanner);
                        case 3 -> publish(scanner);
                        case 4 -> listTopics();
                        case 99 -> {
                            System.out.println("Exiting...");
                            channel.shutdown();
                            return;
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Execution call Error  !");
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            System.out.println("Unhandled exception");
            ex.printStackTrace();
        }
    }

    private static void subscribe(Scanner scanner) {
        System.out.print("Topic to subscribe: ");
        String topic = scanner.nextLine();

        if (activeSubscriptions.containsKey(topic)) {
            System.out.println("Already subscribed to " + topic);
            return;
        }

        SubscribeUnSubscribe request = SubscribeUnSubscribe.newBuilder()
                .setUsrName(username)
                .setTopicName(topic)
                .build();

        StreamObserver<ForumMessage> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ForumMessage msg) {
                String raw = msg.getTxtMsg();

                String[] parts = raw.split(";", 3);

                String text = parts[0];

                System.out.println("[" + msg.getTopicName() + "] "
                        + msg.getFromUser() + ": " + text);

                boolean hasFile = parts.length == 3
                        && !parts[1].isBlank()
                        && !parts[2].isBlank();

                if(hasFile){
                    String bucket = parts[1];
                    String blob = parts[2];
                    System.out.println("Downloading file from gs://" + bucket + "/" + blob);
                    try {
                        downloadBlob(bucket, blob, scanner);
                    } catch (Exception e) {
                        System.out.println("Download failed: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Subscription error: " + throwable.getMessage());
                activeSubscriptions.remove(topic);
            }

            @Override
            public void onCompleted() {
                System.out.println("Unsubscribed from " + topic);
                activeSubscriptions.remove(topic);
            }
        };

        noBlockStub.topicSubscribe(request, responseObserver);

        activeSubscriptions.put(topic, responseObserver);

        System.out.println("Subscribed to " + topic);
    }

    private static void downloadBlob(String bucketname, String blobname, Scanner scanner) throws Exception{
        Storage storage = StorageOptions.getDefaultInstance().getService();

        Blob blob = storage.get(bucketname, blobname);

        if(blob == null){
            throw new Exception("Blob not found: " + bucketname + "/" + blobname);
        }

        Path target = Paths.get("downloads", blobname);
        Files.createDirectories(target.getParent());

        blob.downloadTo(target);

        System.out.println("Downloaded to " + target.toAbsolutePath());
    }

    private static void unsubscribe(Scanner scanner) {
        System.out.print("Topic to unsubscribe: ");
        String topic = scanner.nextLine();

        if (!activeSubscriptions.containsKey(topic)) {
            System.out.println("Not subscribed to " + topic);
            return;
        }

        SubscribeUnSubscribe request = SubscribeUnSubscribe.newBuilder()
                .setUsrName(username)
                .setTopicName(topic)
                .build();

        try {
            blockingStub.topicUnSubscribe(request);
            activeSubscriptions.remove(topic);
            System.out.println("Unsubscribed from " + topic);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void publish(Scanner scanner) {
        System.out.print("Topic: ");
        String topic = scanner.nextLine();

        System.out.print("Message: ");
        String msg = scanner.nextLine();

        System.out.print("Attach file? (y/n): ");
        String opt = scanner.nextLine();

        if(opt.equalsIgnoreCase("y")){
            try {
                System.out.print("Bucket name: ");
                String bucket = scanner.nextLine();

                System.out.println("Blob name: ");
                String blobName = scanner.nextLine();

                System.out.print("File path: ");
                String path = scanner.nextLine();

                uploadPublicBlob(bucket, blobName, path);
                msg = msg + ";" + bucket + ";" + blobName;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        ForumMessage request = ForumMessage.newBuilder()
                .setFromUser(username)
                .setTopicName(topic)
                .setTxtMsg(msg)
                .build();

        try {
            blockingStub.publishMessage(request);
        } catch (Exception e) {
            System.out.println("Publish error: " + e.getMessage());
        }
    }

    private static void uploadPublicBlob(String bucketName, String blobName, String absFileName) throws Exception{
        Storage storage = StorageOptions.getDefaultInstance().getService();

        Path uploadFrom = Paths.get(absFileName);
        String contentType = Files.probeContentType(uploadFrom);

        BlobId blobId = BlobId.of(bucketName, blobName);

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
        if (Files.size(uploadFrom) > 1_000_000) {
            // When content is not available or large (1MB or more) it is recommended
            // to write it in chunks via the blob's channel writer.
            try (WriteChannel channel = storage.writer(blobInfo)) {
                byte[] buffer = new byte[1024];
                try (InputStream input = Files.newInputStream(uploadFrom)) {
                    int limit;
                    while ((limit = input.read(buffer)) >= 0) {
                        try {
                            channel.write(ByteBuffer.wrap(buffer, 0, limit));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } else {
            byte[] bytes = Files.readAllBytes(uploadFrom);
            // create the blob in one request.
            storage.create(blobInfo, bytes);
        }
        storage.createAcl(
                blobId,
                Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)
        );

        System.out.println("Public Blob created!");
        System.out.println("https://storage.googleapis.com/" + bucketName + "/" + blobName);
    }

    private static void listTopics() {
        try {
            ExistingTopics topics = blockingStub.getAllTopics(Empty.newBuilder().build());

            System.out.println("Topics:");
            for (String t : topics.getTopicNameList()) {
                System.out.println(" - " + t);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private static int Menu(Scanner scanner) {
        int op;
        do {
            System.out.println();
            System.out.println("===== FORUM MENU =====");
            System.out.println("1 - Subscribe to topic");
            System.out.println("2 - Unsubscribe from topic");
            System.out.println("3 - Publish message");
            System.out.println("4 - List topics");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an Option?");

            op = Integer.parseInt(scanner.nextLine());
        } while (!((op >= 1 && op <= 4) || op == 99));
        return op;
    }
}
