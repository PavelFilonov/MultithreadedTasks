package RingBuffer;

public class RingBufferMain {

    public static void main(String[] args) {
        RingBuffer<Integer> ringBuffer = new RingBuffer<Integer>(5);
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 30; i++) {
                Integer overwritten = ringBuffer.put(i);
                if (overwritten != null) {
                    System.out.printf("Записано %s (перезаписано %s)%n", i, overwritten);
                } else {
                    System.out.println("Записано " + i);
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        Thread consumer = new Thread(() -> {
            for (int i = 0; i < 15; i++) {
                Integer value;
                while ((value = ringBuffer.get()) == null) {
                    Thread.yield();
                }
                System.out.println("Получено " + value);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        producer.start();
        consumer.start();
    }

}
