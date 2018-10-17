import java.io.*;
import java.net.Socket;

public class ChatServerThread extends Thread {
    private Socket socket;
    private ChatServer server;
    private int ID;
    private ObjectInputStream streamIn = null;
    private ObjectOutputStream streamOut = null;
    private boolean isPublisher = false;
    private Message message;

    public ChatServerThread(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        ID = socket.getPort();
    }

    public void run() {
        System.out.println("Server Thread " + ID + " running.");
        while (true) {
            try {
                String input = streamIn.readUTF();
                switch (input.toLowerCase()) {
                    case ("switch channel"):
                    case ("get channel"):
                        message.setMessage("pick channel: 1,2,3");
                        streamOut.writeObject(message);
                        streamOut.flush();
                        message= (Message) streamIn.readObject();
                        int pickedChannel = Integer.parseInt(message.getMessage());
                        server.setChannel(pickedChannel, this);
                        message.setMessage("picked channel:" + pickedChannel);
                        streamOut.writeObject(message);
                        streamOut.flush();
                        break;
                    case ("publisher"):
                        message.setMessage(server.setPublisher(this));
                        streamOut.writeObject(message);
                        streamOut.flush();
                        break;
                    case ("publish"):
                        if (isPublisher) {
                            streamOut.writeUTF("pick channel: 1,2,3");
                            streamOut.flush();
                            int channel = Integer.parseInt(streamIn.readUTF());
                            streamOut.writeUTF("enter message: ");
                            streamOut.flush();
                            server.broadcast(channel, streamIn.readUTF());
                        }

                    default:
                        //  if (pickedPartner != null) {
                        //      server.sendTo(pickedPartner, input, socket);
                        //  }
                        break;
                }
            } catch (IOException ioe) {
                System.out.println("Unexpected exception: " + ioe.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public void open() throws IOException {
        streamIn = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        streamOut = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void close() throws IOException {
        if (socket != null) socket.close();
        if (streamIn != null) streamIn.close();
    }

    public void setAsPublisher() {
        isPublisher = true;
    }

    public void broadcastTo(String input, int originChannel) {
        try {
            streamOut.writeUTF("Channel number " + originChannel + " sent:\n" + input);
            streamOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
