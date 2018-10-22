import java.io.Serializable;

class Message implements Serializable {
    public Message(String message) {
        this.message = message;
    }
    public Message() {
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

}
