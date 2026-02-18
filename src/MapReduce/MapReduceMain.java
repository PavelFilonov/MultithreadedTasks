package MapReduce;

import java.util.ArrayList;
import java.util.List;

public class MapReduceMain {

    public static void main(String[] args) throws InterruptedException {
        int nReduce = 10;
        int nWorkers = 10;
        List<String> inputs = new ArrayList<>();
        inputs.add("src/MapReduce/files/first.txt");
        inputs.add("src/MapReduce/files/second.txt");
        inputs.add("src/MapReduce/files/third.txt");

        Coordinator coordinator = new Coordinator(inputs, nReduce);
        List<Thread> workers = new ArrayList<>();

        System.out.println("Задача началась");
        for (int i = 0; i < nWorkers; i++) {
            Thread thread = new Thread(new Worker(i, coordinator));
            thread.start();
            workers.add(thread);
        }
        for (Thread thread : workers) {
            thread.join();
        }
        System.out.println("Задача завершилась");
    }

}
