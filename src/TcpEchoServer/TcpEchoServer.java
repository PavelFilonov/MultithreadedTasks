package TcpEchoServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;

public class TcpEchoServer {

    private final int port;

    private final ExecutorService executorService;

    private volatile boolean running = true; // гарантировать видимость при остановке

    private ServerSocket serverSocket;

    public TcpEchoServer(int port, int nThreads) {
        this.port = port;
        if (nThreads <= 0) {
            throw new IllegalArgumentException("Количество потоков должно быть больше нуля");
        }
        this.executorService = Executors.newFixedThreadPool(nThreads);
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Сервер успешно создан (порт=" + port + ")");
        try {
            while (running) {
                Socket client = serverSocket.accept();
                executorService.submit(new TcpEchoClientHandler(client));
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        ofNullable(serverSocket)
                .filter(Predicate.not(ServerSocket::isClosed))
                .ifPresent(socket -> {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        executorService.shutdown();
        System.out.println("Сервер остановлен");
    }

}
