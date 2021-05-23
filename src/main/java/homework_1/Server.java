package homework_1;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;




@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        int i = 1;
        ServerSocket server = new ServerSocket(11111);
        log.debug("server started");

        while(true) {
            System.out.println("server run " + i++);
            log.debug("Client accepted");
            try {
                Socket socket = server.accept();
                Handler handler = new Handler(socket);
                new Thread(handler).start();
            }catch (Exception e) {
                log.error("Connect was broken");
            }
        }
    }
}
