package homework_1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class ChatController implements Initializable {

    public TextField input;
    public ListView<String> listView;
    private DataInputStream is;
    private DataInputStream isFile;
    private OutputStream os;
    private String fileName = "";
    private int b;
    private Thread writeTread;


    @SneakyThrows
    public void send(ActionEvent actionEvent) {

        //os.writeUTF(input.getText());
        byte[] namefile = input.getText().getBytes();
        fileName = new String(namefile);
        log.debug(fileName);

        input.clear();
        writeTread = new Thread(()-> {
            try {
                File file = new File("chattext/" +fileName);
                isFile = new DataInputStream(new FileInputStream(file));
                os.write(30);
                os.write(namefile.length);
                os.write(namefile);
                os.write((int)file.length());
                while ((b = isFile.read()) != -1) {
                        os.write(b);
                }
            } catch (IOException e) {
                log.error("e = ",e);
            }
        });
        writeTread.setDaemon(true);
        writeTread.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 11111);

            is = new DataInputStream(socket.getInputStream());
            os = socket.getOutputStream();
            Thread readTread = new Thread(() -> {
                try {
                    while (true) {
                        String msg = is.readUTF();
                        Platform.runLater(() -> {
                            listView.getItems().add("hello");
                            listView.getItems().add(msg);
                            fileName = "";
                        } );
                    }
                } catch (Exception e) {
                    log.error("e = ", e);
                }
            });
            readTread.setDaemon(true);
            readTread.start();
        } catch (Exception e) {
            log.error("e = ", e);
        }
    }
}
