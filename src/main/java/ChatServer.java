import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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

    void broadcast(Message input) throws Exception {
        for (Integer i :
                defineChannel(input)) {
            for (ChatServerThread chatServerThread :
                    channelClients.get(i - 1)) {
                input.setOriginChannel(i);
                chatServerThread.broadcastTo(input);
            }
        }
    }

    private List<Integer> defineChannel(Message message) throws Exception {
        List<Integer> result = new ArrayList<>();
        String vocab1 = "games,E3,gamescon,game,graphics";
        String[] vocabChan1 = new ArrayList<>(Arrays.asList(vocab1.split(","))).toArray(new String[0]);
        String vocab2 = "car,cars,engine,tuning,tuned,spoiler";
        String[] vocabChan2 = new ArrayList<>(Arrays.asList(vocab2.split(","))).toArray(new String[0]);
        String vocab3 = "phone,snapdragon,smartphone,touchscreen";
        String[] vocabChan3 = new ArrayList<>(Arrays.asList(vocab3.split(","))).toArray(new String[0]);
        if (Arrays.stream(vocabChan1).parallel().anyMatch(message.getMessage().toLowerCase()::contains)) {
            result.add(1);
        }
        if (Arrays.stream(vocabChan2).parallel().anyMatch(message.getMessage().toLowerCase()::contains)) {
            result.add(2);
        }
        if (Arrays.stream(vocabChan3).parallel().anyMatch(message.getMessage().toLowerCase()::contains)) {
            result.add(3);
        }
        if (result.size() == 0) throw new Exception("No such channel");
        return result;
    }

    void setChannel(int pickedChannel, ChatServerThread chatServerThread) {
        channelClients.get(pickedChannel - 1).add(chatServerThread);
    }

    void leaveChannel(int pickedChannel, ChatServerThread chatServerThread) {
        channelClients.get(pickedChannel - 1).remove(chatServerThread);
    }

    List<String> getChannels(ChatServerThread chatServerThread) {
        List<String> channelList = new ArrayList<>();
        for (int i = 0; i < channelClients.size(); i++) {
            if (channelClients.get(i).contains(chatServerThread)) {
                switch (i) {
                    case 0:
                        channelList.add("Channel n." + (i + 1) + ": Games");
                        break;
                    case 1:
                        channelList.add("Channel n." + (i + 1) + ": Automotive");
                        break;
                    case 2:
                        channelList.add("Channel n." + (i + 1) + ": Tech");
                        break;
                }
            }
        }
        return channelList;
    }
}
