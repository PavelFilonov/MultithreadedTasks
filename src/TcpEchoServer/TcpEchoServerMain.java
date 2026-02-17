package TcpEchoServer;

import java.io.IOException;

public class TcpEchoServerMain {

    public static void main(String[] args) {
        int port = 7007;
        int nThreads = 4;
        TcpEchoServer server = new TcpEchoServer(port, nThreads);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Ошибка создания сервера: " + e.getMessage());
        }
    }

}
