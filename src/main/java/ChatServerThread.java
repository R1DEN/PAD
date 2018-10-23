import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ChatServerThread extends Thread {
    private Socket socket;
    private ChatServer server;
    private int ID;
    private DataInputStream streamIn = null;
    private DataOutputStream streamOut = null;
    private boolean isPublisher = false;
    private Message message = new Message();
    private Gson gson = new Gson();

    private List<Message> deadLetters = new ArrayList<>();
    private boolean isConnected = false;

    public ChatServerThread(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        ID = socket.getPort();
    }

    public void run() {
        System.out.println("Server Thread " + ID + " running.");
        isConnected = true;
        while (true) {
            try {
                String input = gson.fromJson(streamIn.readUTF(), Message.class).getMessage();
                    switch (input.toLowerCase()) {
                        case ("disconnect"):
                            isConnected = false;
                            break;
                        case ("connect"):
                            isConnected = true;
                            for (Message deadLetter:
                                 deadLetters) {
                                broadcastTo(deadLetter);
                            }
                            deadLetters.clear();
                            break;
                        case ("switch channel"):
                        case ("get channel"):
                        case ("add channel"):
                            message.setMessage("pick channel: 1,2,3");
                            streamOut.writeUTF(gson.toJson(message));
                            streamOut.flush();
                            int pickedChannel = Integer.parseInt(gson.fromJson(streamIn.readUTF(), Message.class).getMessage());
                            server.setChannel(pickedChannel, this);
                            message.setMessage("picked channels:" + server.getChannels(this).toString());
                            streamOut.writeUTF(gson.toJson(message));
                            streamOut.flush();
                            break;
                        case("leave channel"):
                            message.setMessage("pick channel: "+ server.getChannels(this).toString());
                            streamOut.writeUTF(gson.toJson(message));
                            streamOut.flush();
                            int pickedLeaveChannel = Integer.parseInt(gson.fromJson(streamIn.readUTF(), Message.class).getMessage());
                            server.leaveChannel(pickedLeaveChannel, this);
                            message.setMessage("picked channels:" + server.getChannels(this).toString());
                            streamOut.writeUTF(gson.toJson(message));
                            streamOut.flush();
                            break;
                        case ("publisher"):
                            message.setMessage(server.setPublisher(this));
                            streamOut.writeUTF(gson.toJson(message));
                            streamOut.flush();
                            break;
                        case ("publish"):
                            if (isPublisher) {
                                message.setMessage("pick channel: 1,2,3");
                                streamOut.writeUTF(gson.toJson(message));
                                streamOut.flush();
                                int channel = Integer.parseInt(gson.fromJson(streamIn.readUTF(), Message.class).getMessage());
                                message.setMessage("enter message: ");
                                streamOut.writeUTF(gson.toJson(message));
                                streamOut.flush();
                                message.setMessage(gson.fromJson(streamIn.readUTF(), Message.class).getMessage());
                                message.setOriginChannel(channel);
                                server.broadcast(message);
                            }

                        default:
                            //  if (pickedPartner != null) {
                            //      server.sendTo(pickedPartner, input, socket);
                            //  }
                            break;
                    }
            } catch (IOException ioe) {
                System.out.println("Unexpected exception: " + ioe.getMessage());
            }
        }
    }


    public void open() throws IOException {
        streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void close() throws IOException {
        if (socket != null) socket.close();
        if (streamIn != null) streamIn.close();
    }

    public void setAsPublisher() {
        isPublisher = true;
    }

    public void broadcastTo(Message input) {
        try {
            if (isConnected) {
                message.setMessage("Channel number " + input.getOriginChannel() + " sent:\n" + input.getMessage());
                streamOut.writeUTF(gson.toJson(message));
                streamOut.flush();
            }
            else {
                deadLetters.add(new Message(input));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
