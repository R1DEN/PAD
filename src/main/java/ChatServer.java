import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ChatServer implements Runnable {
    private ServerSocket server = null;
    private Thread thread = null;
    private final Map<Socket, ChatServerThread> connectedClients = new HashMap<>();
    private final List<List<ChatServerThread>> channelClients = new ArrayList<>();
    private boolean publisherIsSet = false;

    private ChatServer(int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            server = new ServerSocket(port);
            System.out.println("Server started: " + server);
            start();
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    public static void main(String args[]) {
        ChatServer server = null;
        server = new ChatServer(1488);
    }

    public void run() {
        while (thread != null) {
            try {
                System.out.println("Waiting for a client ...");
                addThread(server.accept());
            } catch (IOException ie) {
                System.out.println("Acceptance Error: " + ie);
            }
        }
    }

    private void addThread(Socket socket) {
        System.out.println("Client accepted: " + socket);
        ChatServerThread client = new ChatServerThread(this, socket);
        connectedClients.put(socket, client);
        try {
            client.open();
            client.start();
        } catch (IOException ioe) {
            System.out.println("Error opening thread: " + ioe);
        }
    }

//    public List<Socket> getConnectedClients() {
//        return new ArrayList<>(connectedClients.keySet());
//    }

//    public Socket getSocket(int port) {
//        List<Socket> socketList = new ArrayList<>(connectedClients.keySet());
//        for (Socket curSocket :
//                socketList) {
//            if (curSocket.getPort()==port) return curSocket;
//        }
//        return null;
//    }

    private void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
            for (int i = 0; i < 3; i++) {
                channelClients.add(new ArrayList<ChatServerThread>());
            }
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }


    String setPublisher(ChatServerThread chatServerThread) {
        if (!publisherIsSet) {
            publisherIsSet = true;
            chatServerThread.setAsPublisher();
            return "Publisher set successfully";
        } else {
            return "Publisher already set";
        }
    }

    void broadcast(Message input) {
        for (ChatServerThread chatServerThread : channelClients.get(input.getOriginChannel() - 1)) {
            chatServerThread.broadcastTo(input);
        }

    }

    void setChannel(int pickedChannel, ChatServerThread chatServerThread) {
        channelClients.get(pickedChannel - 1).add(chatServerThread);
    }

    void leaveChannel(int pickedChannel, ChatServerThread chatServerThread) {
        channelClients.get(pickedChannel - 1).remove(chatServerThread);
    }

    List<Integer> getChannels(ChatServerThread chatServerThread) {
        List<Integer> channelList = new ArrayList<>();
        for (int i = 0; i < channelClients.size(); i++) {
            if (channelClients.get(i).contains(chatServerThread)) {
                channelList.add(i + 1);
            }
        }
        return channelList;
    }
}
