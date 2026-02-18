package MapReduce;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class Coordinator {

    private final Queue<String> mapFiles = new LinkedList<>();
    private final int nReduce;
    private final int nMapTotal;
    private final AtomicInteger mapAssigned = new AtomicInteger(0);
    private final AtomicInteger mapCompleted = new AtomicInteger(0);
    private final AtomicInteger reduceAssigned = new AtomicInteger(0);
    private final AtomicInteger reduceCompleted = new AtomicInteger(0);
    private boolean mapPhaseDone = false;

    public Coordinator(List<String> inputFiles, int nReduce) {
        this.mapFiles.addAll(inputFiles);
        this.nReduce = nReduce;
        this.nMapTotal = inputFiles.size();
    }

    public synchronized Task getTask() {
        if (!mapFiles.isEmpty()) {
            String file = mapFiles.poll();
            int mapId = mapAssigned.getAndIncrement();
            return new Task(TaskType.MAP, mapId, file, nReduce, null);
        }
        if (!mapPhaseDone) {
            return null;
        }
        if (reduceAssigned.get() < nReduce) {
            int reduceId = reduceAssigned.getAndIncrement();
            List<String> interm = collectIntermediateFilesForReduce(reduceId);
            return new Task(TaskType.REDUCE, reduceId, null, nReduce, interm);
        }
        if (reduceCompleted.get() >= nReduce) {
            return new Task(TaskType.EXIT, -1, null, nReduce, null);
        }
        return null;
    }

    public synchronized void reportMapDone() {
        int done = mapCompleted.incrementAndGet();
        if (done >= nMapTotal) {
            mapPhaseDone = true;
            notifyAll();
        }
    }

    public synchronized void reportReduceDone() {
        int done = reduceCompleted.incrementAndGet();
        if (done >= nReduce) {
            notifyAll();
        }
    }

    private List<String> collectIntermediateFilesForReduce(int reduceId) {
        List<String> res = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("."), "mr-*-" + reduceId)) {
            for (Path p : ds) {
                res.add(p.toString());
            }
        } catch (IOException ignored) {
        }
        return res;
    }

}
