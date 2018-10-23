import com.google.gson.Gson;

import java.net.*;
import java.io.*;

class ChatClient implements Runnable {
    private Socket socket = null;
    private Thread thread = null;
    private DataInputStream console = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread client = null;
    private Gson gson = new Gson();
    private Message message = new Message();
    private boolean isConnected = false;

    private ChatClient(String serverName, int serverPort) {
        System.out.println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            start();
        } catch (UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected exception: " + ioe.getMessage());
        }
    }

    public static void main(String args[]) {
        ChatClient client = null;
        client = new ChatClient("localhost", 1488);
    }

    public void run() {
        isConnected=true;
        while (thread != null) {
            try {
                String input = console.readLine();
                if (input.equals("connect")){
                    isConnected=true;
                }
                if (input != null && isConnected) {
                    message.setMessage(input);
                    streamOut.writeUTF(gson.toJson(message));
                    streamOut.flush();
                }
                if (input.equals("disconnect")){
                    isConnected=false;
                }

            } catch (IOException ioe) {
                System.out.println("Sending error: " + ioe.getMessage());
                stop();
            }
        }
    }

    public void handle(String msg) {
        System.out.println(gson.fromJson(msg, Message.class).getMessage());
    }

    private void start() throws IOException {
        console = new DataInputStream(System.in);
        streamOut = new DataOutputStream(socket.getOutputStream());
        if (thread == null) {
            client = new ChatClientThread(this, socket);
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        try {
            if (console != null) console.close();
            if (streamOut != null) streamOut.close();
            if (socket != null) socket.close();
        } catch (IOException ioe) {
            System.out.println("Error closing ...");
        }
        client.close();
        client.stop();
    }
}