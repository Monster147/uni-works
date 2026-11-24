package grpcclientapp;

import CN2026LabelsStubs.*;
import CN2026LabelsStubs.CN2026LabelsServiceGrpc;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Client {
    private static final String LIST_VM_URL = "https://europe-west1-cn2526-t1-g07.cloudfunctions.net/funcHttpLookup";
    private static String svcIP = "localhost";
    private static int svcPort = 7500;
    private static ManagedChannel channel;
    private static CN2026LabelsServiceGrpc.CN2026LabelsServiceBlockingStub blockingStub;
    private static CN2026LabelsServiceGrpc.CN2026LabelsServiceStub noBlockStub;

    static void main(String[] args) {
        try {
            if (args.length == 2) {
                svcIP = args[0]; svcPort = Integer.parseInt(args[1]);
            }
            System.out.println("connect to " + svcIP + ":" + svcPort);
            channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
                    .usePlaintext()
                    .build();
            blockingStub = CN2026LabelsServiceGrpc.newBlockingStub(channel);
            noBlockStub = CN2026LabelsServiceGrpc.newStub(channel);

            while (true) {
                try {
                    int option = Menu();
                    switch (option) {
                        case 1:
                            submitImage();
                            break;
                        case 2:
                            findImageLabelById();
                            break;
                        case 3:
                            searchImage();
                            break;
                        case 4:
                            downloadImage();
                            break;
                        case 5:
                            scaleGrpcServers();
                            break;
                        case 6:
                            scaleWorkers();
                            break;
                        case 7:
                            choosegRPCServers();
                            break;
                        case 99:  System.exit(0);
                    }
                } catch (Exception ex) {
                    System.out.println("Execution call Error!");
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            System.out.println("Unhandled exception");
            ex.printStackTrace();
        }
    }

    static void choosegRPCServers(){
        try{
            HttpClient client = HttpClient.newBuilder().build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LIST_VM_URL))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200) {
                JsonObject json =
                        JsonParser.parseString(response.body()).getAsJsonObject();

                JsonArray ips = json.getAsJsonArray("ips");

                if (ips.isEmpty()) {
                    System.out.println("No running gRPC servers available.");
                    return;
                }

                System.out.println("\n--- Available gRPC Servers ---");

                for (int i = 0; i < ips.size(); i++) {

                    System.out.println(
                            (i + 1) + " - " + ips.get(i).getAsString()
                    );
                }

                Scanner scanner = new Scanner(System.in);
                System.out.print("Choose server: ");
                int choice = scanner.nextInt();

                if (choice < 1 || choice > ips.size()) {
                    System.out.println("Invalid selection.");
                    return;
                }

                String selectedIP = ips.get(choice - 1).getAsString();

                System.out.println("Switching to server: " + selectedIP + ":" + svcPort);

                if (channel != null && !channel.isShutdown()) {
                    channel.shutdown();
                }

                channel = ManagedChannelBuilder
                        .forAddress(selectedIP, svcPort)
                        .usePlaintext()
                        .build();

                blockingStub = CN2026LabelsServiceGrpc.newBlockingStub(channel);
                noBlockStub = CN2026LabelsServiceGrpc.newStub(channel);

                svcIP = selectedIP;

                System.out.println("Connected successfully to " + selectedIP);
            } else {
                System.out.println("Failed to retrieve gRPC server IPs. Status code: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void submitImage() {
        Scanner scanner = new Scanner(System.in);
        String filePath = read("Enter image file path: ", scanner);

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("File not found: " + filePath);
                return;
            }
            if (!file.isFile()) {
                System.out.println("Path is not a file: " + filePath);
                return;
            }
            if (!file.canRead()) {
                System.out.println("File cannot be read: " + filePath);
                return;
            }

            String filename = file.getName();
            long fileSize = file.length();

            if (fileSize > 100 * 1024 * 1024) {
                System.out.println("File size exceeds maximum limit of 100MB");
                return;
            }

            System.out.println("Sending file: " + filename + " (" + (fileSize / 1024) + " KB)");

            StreamObserver<ImageResponse> responseObserver = new StreamObserver<ImageResponse>() {
                @Override
                public void onNext(ImageResponse response) {
                    System.out.println("Image received with Request ID: " + response.getRequestID());
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("Error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("Image submission completed successfully");
                }
            };

            StreamObserver<ImageChunk> requestObserver = noBlockStub.submitImage(responseObserver);

            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            ImageChunk chunk = ImageChunk.newBuilder()
                    .setFilename(filename)
                    .setData(com.google.protobuf.ByteString.copyFrom(fileContent))
                    .build();
            requestObserver.onNext(chunk);
            requestObserver.onCompleted();

        } catch (Exception e) {
            System.out.println("Error submitting image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void findImageLabelById() {
        Scanner scanner = new Scanner(System.in);
        String requestID = read("Enter Request ID: ", scanner);
        try {
            ImageLabelRequest request = ImageLabelRequest.newBuilder()
                    .setRequestID(requestID)
                    .build();

            ImageLabelResponse response = blockingStub.findImageLabelById(request);

            System.out.println("\n--- Image Label Information ---");
            System.out.println("Request ID: " + response.getRequestID());
            System.out.println("Filename: " + response.getFilename());
            System.out.println("Processed At: " + response.getProcessedAt());
            System.out.println("Labels:");

            for (Label label : response.getLabelsList()) {
                System.out.println("  - Original: " + label.getOriginalLanguage());
                System.out.println("    Translated: " + label.getTranslated());
                System.out.println("    Score: " + label.getScore());
            }
        } catch (Exception e) {
            System.out.println("Error retrieving image labels: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void searchImage() {
        Scanner scanner = new Scanner(System.in);
        String label = read("Enter label to search (or leave empty for all): ", scanner);
        String fromDate = read("Enter from date (ISO format(YYYY-MM-DD), or leave empty): ", scanner);
        String toDate = read("Enter to date (ISO format(YYYY-MM-DD), or leave empty): ", scanner);

        try {
            SearchRequest request = SearchRequest.newBuilder()
                    .setLabel(label)
                    .setFromDate(fromDate)
                    .setToDate(toDate)
                    .build();

            SearchResponse response = blockingStub.searchImage(request);

            System.out.println("\n--- Search Results ---");
            if (response.getFilenamesList().isEmpty()) {
                System.out.println("No images found matching the criteria.");
            } else {
                System.out.println("Found " + response.getFilenamesCount() + " image(s):");
                for (String filename : response.getFilenamesList()) {
                    System.out.println("  - " + filename);
                }
            }
        } catch (Exception e) {
            System.out.println("Error searching images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void downloadImage(){
        Scanner scanner = new Scanner(System.in);
        String requestID = read("Enter Request ID: ", scanner);

        try {
            ImageLabelRequest request = ImageLabelRequest.newBuilder()
                    .setRequestID(requestID)
                    .build();

            Iterator<ImageChunk> chunks = blockingStub.downloadImage(request);

            ByteArrayOutputStream imageDataStream = new ByteArrayOutputStream();
            String filename = null;

            while (chunks.hasNext()) {
                ImageChunk chunk = chunks.next();
                if (filename == null) filename = chunk.getFilename();
                chunk.getData().writeTo(imageDataStream);
            }

            System.out.println("Download completed!");

            if (filename == null || filename.isEmpty()) {
                System.out.println("Error: No filename received from server");
                return;
            }

            byte[] imageBytes = imageDataStream.toByteArray();

            Path target = Paths.get("downloads", filename);
            Files.createDirectories(target.getParent());
            Files.write(target, imageBytes);

            System.out.println("Downloaded to " + target.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error downloading image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void scaleGrpcServers() {
        Scanner scanner = new Scanner(System.in);
        String input = read("Enter number of gRPC server instances: ", scanner);

        try {
            int instances = Integer.parseInt(input);
            ScaleRequest request = ScaleRequest.newBuilder()
                    .setWantedInstances(instances)
                    .build();

            ScaleResponse response = blockingStub.scaleGrpcServers(request);
            System.out.println("Server Response: " + response.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format");
        } catch (Exception e) {
            System.out.println("Error scaling gRPC servers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void scaleWorkers() {
        Scanner scanner = new Scanner(System.in);
        String input = read("Enter number of worker instances: ", scanner);

        try {
            int instances = Integer.parseInt(input);
            ScaleRequest request = ScaleRequest.newBuilder()
                    .setWantedInstances(instances)
                    .build();

            ScaleResponse response = blockingStub.scaleWorkers(request);
            System.out.println("Server Response: " + response.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format");
        } catch (Exception e) {
            System.out.println("Error scaling workers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int Menu() {
        int op;
        Scanner scan = new Scanner(System.in);
        do {
            System.out.println();
            System.out.println("    MENU");
            System.out.println(" 1 - Submissão de um ficheiro imagem");
            System.out.println(" 2 - Obter a lista de todas as características encontradas na imagem");
            System.out.println(" 3 - Obter todos os nomes de ficheiros armazenados no sistema entre duas datas e que contêm uma determinada característica");
            System.out.println(" 4 - Download de uma imagem a partir do ID do pedido feito anteriormente");
            System.out.println(" 5 - Aumento e diminuição do número de instâncias de servidores gRPC");
            System.out.println(" 6 - Aumento e diminuição do número de instâncias da aplicação de processamento de imagens.");
            System.out.println(" 7 - Escolher servidor gRPC");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an Option?");
            op = scan.nextInt();
        } while (!((op >= 1 && op <= 7) || op == 99));
        return op;
    }

    private static String read(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }
}