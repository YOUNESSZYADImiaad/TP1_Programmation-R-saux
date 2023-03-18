package nonblocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SingleThreadChatServer {
    private static int CLIENT_COUNT = 0;
    private Map<SocketChannel, Integer> clientMap = new HashMap<>();
    private List<SocketChannel> clientChannels = new ArrayList<>();
    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    public static void main(String[] args) throws IOException {
        new SingleThreadChatServer().start();
    }

    public void start() throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("localhost", 4444));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("The Server is Start in Port: 4444");

        while (true) {
            selector.select();

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    SocketChannel clientChannel = serverSocketChannel.accept();
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);
                    CLIENT_COUNT++;
                    clientMap.put(clientChannel, CLIENT_COUNT);
                    clientChannels.add(clientChannel);

                    System.out.println("New Client Connnecting => id: " + CLIENT_COUNT + " IP: " + clientChannel.getRemoteAddress());
                    broadcastMessage("Welcome, your ID is " + CLIENT_COUNT + "\n", clientChannel);
                }

                if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    String message = read(clientChannel);

                    if (message == null) {
                        removeClient(clientChannel);
                    } else {
                        System.out.println("IP: " + clientChannel.getRemoteAddress() + " Request: " + message);
                        broadcastMessage(message, clientChannel, clientChannels);
                        
                    }
                }

                keyIterator.remove();
            }
        }
    }


    public String read(SocketChannel clientChannel) throws IOException {
        buffer.clear();
        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            return null;
        }
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes);
    }

    public void removeClient(SocketChannel clientChannel) throws IOException {
        int clientId = clientMap.remove(clientChannel);
        clientChannels.remove(clientChannel);

        System.out.println("Client disconnected => id: " + clientId + " IP: " + clientChannel.getRemoteAddress());
        broadcastMessage("Client " + clientId + " has left the chat\n", clientChannel, clientChannels);

        clientChannel.close();
    }

    public void broadcastMessage(String message, SocketChannel fromChannel, List<SocketChannel> clientChannels) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        for (SocketChannel clientChannel : clientChannels) {
            if (clientChannel != fromChannel) {
                clientChannel.write(buffer);
                buffer.rewind();
            }
        }
    }
    public void broadcastMessage(String message, SocketChannel toChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        toChannel.write(buffer);
        buffer.clear();
    }
}