package forumpubsub;

public class ForumMessage {
    String sender;
    String message;

    public ForumMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}
