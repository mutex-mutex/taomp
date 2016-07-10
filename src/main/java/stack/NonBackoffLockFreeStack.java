package stack;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class NonBackoffLockFreeStack<T> {

    private AtomicReference<Node> top = new AtomicReference<>(null);

    protected boolean tryPush(Node node) {
        Node oldTop = top.get();
        node.next = oldTop;
        return top.compareAndSet(oldTop, node);
    }

    protected void push(T value) {
        Node node = new Node(value);
        while(true) {
            if(tryPush(node)) {
                return;
            }
        }
    }

    protected Node tryPop() {
        Node oldTop = top.get();
        if(oldTop == null) {
            throw new RuntimeException();
        }
        Node newTop = oldTop.next;
        if(top.compareAndSet(oldTop, newTop)) {
            return oldTop;
        } else {
            return null;
        }
    }

    public T pop() {
        while(true) {
            Node returning = tryPop();
            if(returning != null) {
                return returning.item;
            }
        }
    }

    private class Node {
        private T item;
        private Node next;

        public Node(T item) {
            this.item = item;
            this.next = null;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        NonBackoffLockFreeStack<Integer> stack = new NonBackoffLockFreeStack<>();

        Thread t = new Thread(
                () -> {
                    for(int i = 0; i < 100; i++) {
                        int time = ThreadLocalRandom.current().nextInt(1000);
                        try {
                            Thread.sleep(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stack.push(i);
                        System.out.println("pushed " + i);
                    }
                }
        );
        t.start();

        Thread.sleep(2000L);

        Thread t2 = new Thread(
                () -> {
                    for(int i = 0; i < 100; i++) {
                        int time = ThreadLocalRandom.current().nextInt(2000);
                        try {
                            Thread.sleep(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        System.out.println("poped " + stack.pop());
                    }
                }
        );
        t2.start();

        Thread.sleep(50*1000L);
    }
}
