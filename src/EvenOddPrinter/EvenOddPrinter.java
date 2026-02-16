package EvenOddPrinter;

public class EvenOddPrinter {

    private final int limit;

    private boolean isEven = false;

    public EvenOddPrinter(int limit) {
        this.limit = limit;
    }

    public void printEven() {
        for (int i = 0; i <= limit; i += 2) {
            synchronized (this) {
                while (isEven) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                System.out.println(i);
                isEven = true;
                notifyAll();
            }
        }
    }

    public void printOdd() {
        for (int i = 1; i <= limit; i += 2) {
            synchronized (this) {
                while (!isEven) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                System.out.println(i);
                isEven = false;
                notifyAll();
            }
        }
    }

}
