package MapReduce;

import java.util.List;

public record Task(TaskType type, int id, String fileName, int nReduce, List<String> intermediateFiles) {
}
