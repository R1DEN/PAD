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
    private final Map<Socket,ChatServerThread> connectedClients = new HashMap<>();

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

    public List<Socket> getConnectedClients() {
        return new ArrayList<>(connectedClients.keySet());
    }
    public Socket getSocket(int port) {
        List<Socket> socketList = new ArrayList<>(connectedClients.keySet());
        for (Socket curSocket :
                socketList) {
            if (curSocket.getPort()==port) return curSocket;
        }
        return null;
    }

    public void sendTo(Socket receiver, String message, Socket sender) {
        connectedClients.get(receiver).setPickedPartner(sender);
        connectedClients.get(receiver).sendTo(message, sender);
    }
    private void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }


}
