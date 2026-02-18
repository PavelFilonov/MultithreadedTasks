package MapReduce;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Worker implements Runnable {

    private final Coordinator coordinator;

    private final int id;

    public Worker(int id, Coordinator coordinator) {
        this.id = id;
        this.coordinator = coordinator;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Task task;
                synchronized (coordinator) {
                    task = coordinator.getTask();
                    if (task == null) {
                        coordinator.wait(100);
                        continue;
                    }
                }
                if (task.type() == TaskType.EXIT) {
                    System.out.println("Воркер " + id + " завершает работу");
                    break;
                } else if (task.type() == TaskType.MAP) {
                    System.out.println("Воркер " + id + " взял задачу " + task.id() + ", файл " + task.fileName());
                    doMap(task.id(), task.fileName(), task.nReduce());
                    coordinator.reportMapDone();
                } else if (task.type() == TaskType.REDUCE) {
                    System.out.println("Воркер " + id + " делает reduce " + task.id());
                    doReduce(task.id(), task.intermediateFiles());
                    coordinator.reportReduceDone();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public List<KeyValue> map(String content) {
        List<KeyValue> res = new ArrayList<>();
        String[] tokens = content.split("\\W+");
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            res.add(new KeyValue(token.toLowerCase(), "1"));
        }
        return res;
    }

    private void doMap(int mapId, String fileName, int nReduce) {
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            System.err.println("Воркер " + id + " упал с ошибкой, читая файл " + fileName);
            return;
        }
        List<KeyValue> kvs = map(content);
        Map<Integer, BufferedWriter> writers = new HashMap<>();
        try {
            for (KeyValue kv : kvs) {
                int bucket = (kv.key().hashCode() & Integer.MAX_VALUE) % nReduce;
                BufferedWriter bw = writers.get(bucket);
                if (bw == null) {
                    String fName = String.format("mr-%d-%d", mapId, bucket);
                    bw = new BufferedWriter(new FileWriter(fName, true));
                    writers.put(bucket, bw);
                }
                bw.write(kv.key() + "\t" + kv.value());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Воркер " + id + " упал во время записи: " + e.getMessage());
        } finally {
            for (BufferedWriter bw : writers.values()) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public String reduce(List<String> values) {
        int sum = 0;
        for (String v : values) {
            try {
                sum += Integer.parseInt(v);
            } catch (NumberFormatException ignored) {
            }
        }
        return String.valueOf(sum);
    }

    private void doReduce(int reduceId, List<String> intermediateFiles) {
        Map<String, List<String>> groups = new HashMap<>();
        for (String fName : intermediateFiles) {
            try (BufferedReader br = new BufferedReader(new FileReader(fName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\t", 2);
                    if (parts.length < 2) {
                        continue;
                    }
                    String k = parts[0];
                    String v = parts[1];
                    groups.computeIfAbsent(k, kk -> new ArrayList<>()).add(v);
                }
            } catch (IOException ignored) {
            }
        }
        List<String> keys = new ArrayList<>(groups.keySet());
        Collections.sort(keys);
        String outName = String.format("mr-out-%d", reduceId);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outName))) {
            for (String k : keys) {
                String result = reduce(groups.get(k));
                bw.write(k + " " + result);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Воркер " + id + " упал во время записи: " + e.getMessage());
        }
    }

}
