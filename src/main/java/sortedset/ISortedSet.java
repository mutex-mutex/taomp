package sortedset;

public interface ISortedSet<T extends Keyed> {

    public boolean add(T x);

    public boolean remove(T x);
}
