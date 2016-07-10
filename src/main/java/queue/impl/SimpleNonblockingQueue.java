package queue.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleNonblockingQueue<T> {

    public static class Node<T> {
        AtomicReference<Node<T>> next;
        volatile T val;
    }

    private AtomicReference<Node<T>> head;
    private AtomicReference<Node<T>> tail;

    public SimpleNonblockingQueue() {
        Node<T> node = new Node<>();
        node.next = new AtomicReference<>(null);
        head = new AtomicReference<>(node);
        tail = new AtomicReference<>(node);
    }

    public void enqueue(T val) {
        Node<T> node = new Node<>();
        node.val = val;
        node.next = new AtomicReference<>(null);

        for(;;) {
            Node<T> t = tail.get();
            Node<T> tNext = t.next.get();

            if(t == tail.get()) {
                if(tNext == null) {
                    if(t.next.compareAndSet(null, node)) {
                        break;
                    }
                } else {
                    tail.compareAndSet(t, tNext);
                }
            }
        }
        tail.compareAndSet(tail.get(), node);
    }

    public T dequeue() {
        for(;;) {
            Node<T> h = head.get();
            Node<T> t = tail.get();
            Node<T> hNext = h.next.get();

            if(h == head.get()) {
                if(h == t) {
                    if(hNext == null) {
                        return null;
                    }
                    tail.compareAndSet(t, hNext);
                } else {
                    T val = hNext.val;
                    if(head.compareAndSet(h, hNext)) {
                        return val;
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        Node<T> x = head.get().next.get();
        for(;;) {
            if(x == null) {
                return builder.toString();
            }
            builder.append(x.val).append(";");
            if(x.next == null) {
                return builder.toString();
            }
            x = x.next.get();
        }
    }

    public static void main(String[] args) throws Exception {
        final SimpleNonblockingQueue<Integer> queue = new SimpleNonblockingQueue<>();
        final int MAX_COUNT = 1000000;

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(
                () -> {
                    for(int i = 0; i < MAX_COUNT; i++) {
                        queue.enqueue(i);
                        if(i % 5000 == 0) {
                            System.out.println("Equeued " + i + " items");
                        }
                    }
                }
        );
        executor.submit(
                () -> {
                    int deqCount = 0;
                    for(;;) {
                        Integer val = queue.dequeue();
                        if(val != null) {
                            deqCount++;
                        }
                        if(deqCount % 5000 == 0) {
                            System.out.println("Dequeued " + deqCount + " items");
                        }
                        if(deqCount == MAX_COUNT) {
                            break;
                        }
                    }
                    return null;
                }
        );

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);

        System.out.println(queue);
    }
}
