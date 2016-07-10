package hash;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseHashSet<T> {

    protected List<T>[] table;
    protected int setSize;

    public BaseHashSet(int capacity) {
        setSize = 0;
        table = (List<T>[]) new List[capacity];
        for(int i = 0; i < capacity; i++) {
            table[i] = new ArrayList<T>();
        }
    }

    public boolean contains(T x) {
        acquire(x);
        try {

            int myBucket = x.hashCode() % table.length;
            return table[myBucket].contains(x);
        } finally {
            release(x);
        }
    }

    public boolean add(T x) {
        boolean result = false;
        acquire(x);
        try {

            int myBucket = x.hashCode() % table.length;
            result = table[myBucket].add(x);
            setSize = result ? setSize + 1 : setSize;
        } finally {
            release(x);
        }
        if(policy()) {
            resize();
        }
        return result;
    }

    protected abstract boolean policy();

    protected abstract void resize();

    public abstract void acquire(T x);

    public abstract void release(T x);
}
