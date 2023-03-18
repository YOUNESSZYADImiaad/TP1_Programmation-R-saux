package blocking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MultiThreadedChatServer {
    private int port;
    private List<ClientThread> clients;

    public MultiThreadedChatServer(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Chat server is running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientThread clientThread = new ClientThread(clientSocket, this);
                clients.add(clientThread);
                clientThread.start();
            }
        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void broadcast(String message, ClientThread excludeClient) {
        for (ClientThread client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientThread client) {
        clients.remove(client);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MultiThreadedChatServer <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        MultiThreadedChatServer server = new MultiThreadedChatServer(port);
        server.start();
    }

    private static class ClientThread extends Thread {
        private Socket clientSocket;
        private MultiThreadedChatServer server;
        private PrintWriter out;

        public ClientThread(Socket clientSocket, MultiThreadedChatServer server) {
            this.clientSocket = clientSocket;
            this.server = server;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String username = in.readLine();
                System.out.println("New client connected: " + username);

                String message = "Welcome to the chat room, " + username + "!";
                server.broadcast(message, this);

                while (true) {
                    message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    message = username + ": " + message;
                    server.broadcast(message, this);
                }
            } catch (IOException ex) {
                System.out.println("Error in UserThread: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                server.removeClient(this);
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    System.out.println("Error in closing the client socket: " + ex.getMessage());
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}

