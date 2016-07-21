package com.chibik.taomp.queue;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class SynchDualQueue<T> {

    private AtomicReference<Node> head;
    private AtomicReference<Node> tail;

    public SynchDualQueue() {
        Node sentinel = new Node(null, NodeType.ITEM);
        head = new AtomicReference<>(sentinel);
        tail = new AtomicReference<>(sentinel);
    }

    public void enq(T e) {
        Node offer = new Node(e, NodeType.ITEM);
        while(true) {
            Node t = tail.get(), h = head.get();
            if(h == t || t.type == NodeType.ITEM) {
                Node n = t.next.get();
                if(n != null) {
                    tail.compareAndSet(t, n);
                } else if(t.next.compareAndSet(n, offer)) {
                    tail.compareAndSet(t, offer);
                    while(offer.item.get() == e);
                    h = head.get();
                    if(offer == h.next.get()) {
                        head.compareAndSet(h, offer);
                    }
                    return;
                }
            } else {
                Node n = h.next.get();
                if(t != tail.get() || h != head.get() || n == null) {
                    continue;
                }
                boolean success = n.item.compareAndSet(null, e);
                head.compareAndSet(h, n);
                if(success) {
                    return;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Node curr = this.head.get();
        while(curr.next != null) {
            builder.append(curr);
            curr = curr.next.get();
        }
        return builder.toString();
    }

    private enum NodeType {
        ITEM,
        RESERVATION
    }

    public class Node {
        volatile NodeType type;
        volatile AtomicReference<T> item;
        volatile AtomicReference<Node> next;

        public Node(T item, NodeType type) {
            this.item = new AtomicReference<>(item);
            this.next = new AtomicReference<>(null);
            this.type = type;
        }

        @Override
        public String toString() {
            return "{" + "t=" + type + ", it=" + item + '}';
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SynchDualQueue<Integer> queue = new SynchDualQueue<>();

        final long startMillis = System.currentTimeMillis();

        Thread t = new Thread(
                () -> {
                    for(int i = 0; i < 10; i++) {
                        queue.enq(i);
                        System.out.println(startMillis - System.currentTimeMillis() + ";" + "enq " + i);
                    }
                }
        );
        t.start();

        Thread t2 = new Thread(
                () -> {
                    for(int i = 0; i < 10; i++) {
                        ThreadLocalRandom r = ThreadLocalRandom.current();
                        int nextWait = r.nextInt(3000);

                        try {
                            Thread.sleep(nextWait);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //Integer val = com.chibik.taomp.queue.deq();

                        //System.out.println(startMillis - System.currentTimeMillis() + ";" + "deq " + val);
                    }
                }
        );
        t2.start();

        Thread.sleep(10*2000L);
    }
}
