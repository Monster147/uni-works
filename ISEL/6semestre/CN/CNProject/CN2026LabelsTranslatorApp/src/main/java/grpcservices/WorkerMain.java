package grpcservices;

public class WorkerMain {
    public static void main(String[] args) throws Exception {

        PubSubSubscriber subscriber =
                new PubSubSubscriber();

        subscriber.work();
    }
}
