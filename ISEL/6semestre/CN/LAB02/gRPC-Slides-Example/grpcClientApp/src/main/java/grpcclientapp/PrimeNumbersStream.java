package grpcclientapp;

import io.grpc.stub.StreamObserver;
import servicestubs.IntNumber;

public class PrimeNumbersStream implements StreamObserver<IntNumber> {
    boolean completed=false;

    private final int start;
    private final int end;
    private final int callId;

    public PrimeNumbersStream(int callId, int start, int end) {
        this.callId = callId;
        this.start = start;
        this.end = end;
    }

    @Override
    public void onNext(IntNumber reply) {
        System.out.println("[Call " + callId + " | " + start + "-" + end + "] Prime: " + reply.getIntnumber());
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("[Call " + callId + "] Completed with error: " + throwable.getMessage());
        completed=true;
    }

    @Override
    public void onCompleted() {
        System.out.println("[Call " + callId + "] Completed interval " + start + "-" + end);
        completed=true;
    }

    public boolean isCompleted() {
        return completed;
    }
}
