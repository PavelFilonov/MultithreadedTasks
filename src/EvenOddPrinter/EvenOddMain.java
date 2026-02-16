package EvenOddPrinter;

public class EvenOddMain {
    public static void main(String[] args) {
        int limit = 10;
        EvenOddPrinter printer = new EvenOddPrinter(limit);

        Thread evenThread = new Thread(printer::printEven, "EvenThread");
        Thread oddThread = new Thread(printer::printOdd, "OddThread");

        evenThread.start();
        oddThread.start();

        try {
            evenThread.join();
            oddThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}