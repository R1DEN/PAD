import java.io.Serializable;

class Message implements Serializable {
    private String message;
    private int originChannel;

    public Message(String message) {
        this.message = message;
    }

    public Message() {
    }

    public Message(Message input) {
        this.message=input.message;
        this.originChannel=input.originChannel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getOriginChannel() {
        return originChannel;
    }

    public void setOriginChannel(int originChannel) {
        this.originChannel = originChannel;
    }
}
