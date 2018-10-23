import java.io.Serializable;

class Message implements Serializable {
    private String message;
    private int originChannel;

    Message() {
    }

    Message(Message input) {
        this.message=input.message;
        this.originChannel=input.originChannel;
    }

    String getMessage() {
        return message;
    }

    void setMessage(String message) {
        this.message = message;
    }

    int getOriginChannel() {
        return originChannel;
    }

    void setOriginChannel(int originChannel) {
        this.originChannel = originChannel;
    }
}
