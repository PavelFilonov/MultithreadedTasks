package TcpEchoServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpEchoClient {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 7007;
        String message = "Сообщение успешно получено и отправлено";
        try (
                Socket socket = new Socket(host, port);
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream()
        ) {
            byte[] send = message.getBytes(StandardCharsets.UTF_8);
            out.write(send);
            out.flush();
            byte[] buffer = new byte[4096];
            int read = in.read(buffer);
            if (read != -1) {
                String echoed = new String(buffer, 0, read, StandardCharsets.UTF_8);
                System.out.println("Ответ от сервера: " + echoed);
            } else {
                System.out.println("Нет ответа от сервера");
            }
        } catch (Exception e) {
            System.err.println("Ошибка создания клиента: " + e.getMessage());
        }
    }

}
