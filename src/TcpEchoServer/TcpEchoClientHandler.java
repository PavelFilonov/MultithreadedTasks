package TcpEchoServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public record TcpEchoClientHandler(Socket socket) implements Runnable {

    private static final int BUFFER_SIZE = 4096;

    @Override
    public void run() {
        String clientInfo = socket.getRemoteSocketAddress().toString();
        System.out.println("Подключен к: " + clientInfo);
        try (
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream()
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                out.flush();
            }
        } catch (IOException e) {
            System.err.printf("Ошибка подключения к %s: %s%n", clientInfo, e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            System.out.println("Закрыта связь с: " + clientInfo);
        }
    }

}
