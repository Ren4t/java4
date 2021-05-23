package homework_1;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;


@Slf4j
public class Handler implements Runnable, Closeable {

    private final Socket socket;
    private int i=0;
    private int b;
    private boolean iswrite = false;
    private boolean isname =false;
    private boolean isLenFile = false;
    private long lenFile;
    private String fileName = "";
    private BufferedOutputStream osf;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try(InputStream is = socket.getInputStream();
            DataOutputStream os = new DataOutputStream(socket.getOutputStream())
            ) {
            while (true) {
                if(!iswrite && is.read() == 30) {
                    iswrite = true;
                }
                if(!isname && iswrite){
                    isname = true;
                    b = is.read();
                    for(int i =0; i < b;i++){
                        fileName = fileName + ((char)is.read());
                    }
                    log.debug(fileName);
                    osf = new BufferedOutputStream(new FileOutputStream("server/" + fileName,true));
                    isLenFile = true;
                }
                if(isLenFile){
                    lenFile = is.read();
                    isLenFile = false;
                }

                if(iswrite) {
                    for (int j = 0; j < lenFile; j++) {
                        b = is.read();
                        osf.write(b);
                        osf.flush();
                    }
                    iswrite = false;
                    isname = false;
                    String msg ="copy file " + fileName + " done!";
                    os.writeUTF(msg);
                }
                log.debug("received: {}",b);
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("e= ", e);
        }finally {
            try {
                osf.close();
            } catch (IOException e) {
                log.debug("e = ", e);
            }
        }
    }

    public void close() throws IOException {
        socket.close();
        osf.close();
    }
}
