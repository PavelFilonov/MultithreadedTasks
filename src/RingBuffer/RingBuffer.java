package RingBuffer;

import java.util.concurrent.locks.ReentrantLock;

public class RingBuffer<E> {

    private final Object[] buffer;
    private final int length;
    private int writeIndex = 0;
    private int readIndex = 0;
    private int count = 0;
    private final ReentrantLock lock = new ReentrantLock(); // для синхронизации ключевых секций

    public RingBuffer(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Размер должен быть больше нуля");
        }
        this.length = length;
        this.buffer = new Object[length];
    }


    public E put(E item) {
        if (item == null) {
            throw new NullPointerException();
        }
        lock.lock();
        try {
            E overwritten = null;
            if (count == length) {
                overwritten = (E) buffer[readIndex];
                buffer[readIndex] = null;
                readIndex = (readIndex + 1) % length;
                count--;
            }
            buffer[writeIndex] = item;
            writeIndex = (writeIndex + 1) % length;
            count++;
            return overwritten;
        } finally {
            lock.unlock();
        }
    }

    public E get() {
        lock.lock();
        try {
            if (count == 0) {
                return null;
            }
            E item = (E) buffer[readIndex];
            buffer[readIndex] = null;
            readIndex = (readIndex + 1) % length;
            count--;
            return item;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        lock.lock();
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(buffer[i]);
            }
            return sb.toString();
        } finally {
            lock.unlock();
        }
    }

}
