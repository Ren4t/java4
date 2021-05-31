package homework_2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;


public class Server {
    private ByteBuffer buffer;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private String dir = "server";

    public Server() throws Exception {
        buffer = ByteBuffer.allocate(100);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(11111));
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (serverSocketChannel.isOpen()) {
            selector.select();

            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if(key.isReadable()) {
                    handleRead(key);
                }
                keyIterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder sb = new StringBuilder();
        int r;
        while(true) {
            r = channel.read(buffer);
            if( r == -1) {
                channel.close();
                return;
            }
            if (r == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                sb.append((char) buffer.get());
            }
            buffer.clear();
        }

        String message = sb.toString().trim();

        if(message.equals("ls")) {
            String files = Files.list(Paths.get(dir))
                    .map(f -> f.getFileName().toString())
                    .collect(Collectors.joining("\n\r"));
            channel.write(ByteBuffer.wrap(files.getBytes(UTF_8)));
        }else if (message.startsWith("cat")) {
            String fileName = message.replace("cat ","");
            byte[] bytes = Files.readAllBytes(Paths.get(dir,fileName));
            channel.write(ByteBuffer.wrap(bytes));
        } else if(message.startsWith("touch")) {
            try {
                String[] str = message.split(" ",0);
                Path path =Paths.get(dir,str[1]);
                if (!Files.exists(path)) {
                        Files.createDirectories(path.getParent());
                        Files.createFile(path);

                }
            } catch (Exception e){
                System.err.println(e);
            }
        } else if(message.startsWith("mkdir")) {
            String[] str = message.split(" ",0);
            Path path = Paths.get(dir, str[1]);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectory(path);
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }

        channel.write(ByteBuffer.wrap(("\n\r").getBytes(UTF_8)));
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverSocketChannel.accept();
        channel.configureBlocking(false);
        channel.write(ByteBuffer.wrap("Welcome to server".getBytes(UTF_8)));
        channel.register(selector, SelectionKey.OP_READ,"Hello");
}
}
