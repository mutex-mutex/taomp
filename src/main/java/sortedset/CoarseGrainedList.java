package sortedset;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoarseGrainedList<T extends Keyed> implements ISortedSet<T> {

    private Node head;
    private Lock lock = new ReentrantLock();

    public CoarseGrainedList() {
        head = new Node(Integer.MIN_VALUE);
        head.next = new Node(Integer.MAX_VALUE);
    }

    public boolean add(T item) {
        Node pred, curr;
        int key = item.key();
        lock.lock();
        try {
            pred = head;
            curr = pred.next;
            while(curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            if(key == curr.key) {
                return false;
            } else {
                Node node = new Node(item, key);
                pred.next = node;
                node.next = curr;
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(T x) {
        Node pred, curr;
        int key = x.key();
        lock.lock();
        try {
            pred = head;
            curr = pred.next;

            while(curr.key < key) {
                pred = curr;
                curr = curr.next;
            }

            if(curr.key == key) {
                pred.next = curr.next;
                return true;
            } else {
                return false;
            }

        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Node curr = head.next;
        while(curr.next != null) {
            builder.append(curr);
            builder.append("->");

            curr = curr.next;
        }
        return builder.toString();
    }

    private class Node {
        T item;
        int key;
        Node next;

        public Node(int key) {
            this.key = key;
        }

        public Node(T item, int key) {
            this.item = item;
            this.key = key;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "item=" + item +
                    '}';
        }
    }
}
