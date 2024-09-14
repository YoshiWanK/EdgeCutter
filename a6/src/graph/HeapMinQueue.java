package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A min priority queue of distinct elements of type `KeyType` associated with (extrinsic) integer
 * priorities, implemented using a binary heap paired with a hash table.
 */
public class HeapMinQueue<KeyType> implements MinQueue<KeyType> {

    /**
     * Pairs an element `key` with its associated priority `priority`.
     */
    private record Entry<KeyType>(KeyType key, int priority) {
        // Note: This is equivalent to declaring a static nested class with fields `key` and
        //  `priority` and a corresponding constructor and observers, overriding `equals()` and
        //  `hashCode()` to depend on the fields, and overriding `toString()` to print their values.
        // https://docs.oracle.com/en/java/javase/17/language/records.html
    }

    /**
     * Associates each element in the queue with its index in `heap`.  Satisfies
     * `heap.get(index.get(e)).key().equals(e)` if `e` is an element in the queue. Only maps
     * elements that are in the queue (`index.size() == heap.size()`).
     */
    private final Map<KeyType, Integer> index;

    /**
     * Sequence representing a min-heap of element-priority pairs.  Satisfies
     * `heap.get(i).priority() >= heap.get((i-1)/2).priority()` for all `i` in `[1..heap.size()]`.
     */
    private final ArrayList<Entry<KeyType>> heap;

    /**
     * Assert that our class invariant is satisfied.  Returns true if it is (or if assertions are
     * disabled).
     */
    private boolean checkInvariant() {
        for (int i = 1; i < heap.size(); ++i) {
            int p = (i - 1) / 2;
            assert heap.get(i).priority() >= heap.get(p).priority();
            assert index.get(heap.get(i).key()) == i;
        }
        assert index.size() == heap.size();
        return true;
    }

    /**
     * Create an empty queue.
     */
    public HeapMinQueue() {
        index = new HashMap<>();
        heap = new ArrayList<>();
        assert checkInvariant();
    }

    /**
     * Return whether this queue contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Return the number of elements contained in this queue.
     */
    @Override
    public int size() {
        return heap.size();
    }

    /**
     * Return an element associated with the smallest priority in this queue.  This is the same
     * element that would be removed by a call to `remove()` (assuming no mutations in between).
     * Throws NoSuchElementException if this queue is empty.
     */
    @Override
    public KeyType get() {
        // Propagate exception from `List::getFirst()` if empty.
        return heap.getFirst().key();
    }

    /**
     * Return the minimum priority associated with an element in this queue.  Throws
     * NoSuchElementException if this queue is empty.
     */
    @Override
    public int minPriority() {
        return heap.getFirst().priority();
    }

    /**
     * If `key` is already contained in this queue, change its associated priority to `priority`.
     * Otherwise, add it to this queue with that priority.
     */
    @Override
    public void addOrUpdate(KeyType key, int priority) {
        if (!index.containsKey(key)) {
            add(key, priority);
        } else {
            update(key, priority);
        }
    }

    /**
     * Remove and return the element associated with the smallest priority in this queue.  If
     * multiple elements are tied for the smallest priority, an arbitrary one will be removed.
     * Throws NoSuchElementException if this queue is empty.
     */
    @Override
    public KeyType remove() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException();
        }
        Entry<KeyType> smallest = heap.get(0);
        KeyType smallKey = smallest.key();

        Entry<KeyType> end = heap.remove(heap.size()-1);
        if (!heap.isEmpty()) {
            heap.set(0, end);
            index.put(end.key(), 0);
            bubbleDown(0);
        }
        index.remove(smallKey);

        assert checkInvariant();
        return smallKey;
    }

    /**
     * Remove all elements from this queue (making it empty).
     */
    @Override
    public void clear() {
        index.clear();
        heap.clear();
        assert checkInvariant();
    }

    /**
     * Swap the Entries at indices `i` and `j` in `heap`, updating `index` accordingly.  Requires `0
     * <= i,j < heap.size()`.
     */
    private void swap(int i, int j) {
        assert i >= 0 && i < heap.size();
        assert j >= 0 && j < heap.size();

        Entry<KeyType> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);

        index.put(heap.get(i).key(), i);
        index.put(heap.get(j).key(), j);
    }

    /**
     * Add element `key` to this queue, associated with priority `priority`.  Requires `key` is not
     * contained in this queue.
     */
    private void add(KeyType key, int priority) {
        assert !index.containsKey(key);

        Entry<KeyType> toAdd = new Entry<>(key, priority);
        heap.add(toAdd);
        int addIndex = heap.size()-1;
        index.put(key, addIndex);

        bubbleUp(addIndex);

        assert checkInvariant();
    }

    /**
     * Change the priority associated with element `key` to `priority`.  Requires that `key` is
     * contained in this queue.
     */
    private void update(KeyType key, int priority) {
        assert index.containsKey(key);

        int toChange = index.get(key);
        int currPriority = heap.get(toChange).priority();
        Entry<KeyType> newElement = new Entry<>(key, priority);
        heap.set(toChange, newElement);
        if (priority < currPriority) {
            bubbleUp(toChange);
        } else if (priority > currPriority) {
            bubbleDown(toChange);
        }

        assert checkInvariant();
    }
    /**
     * If a new element is added that has a lower priority than its parent, or an element's priority
     * decreases such that it is smaller than its parent, it is swapped with its parent, and this process
     * continues until it no longer has a lower priority than its parent, or it is the root of the tree if there
     * is no element with a smaller priority. Requires index i is contained in this heap.
     */
    private void bubbleUp(int i) {
        while (i > 0) {
            int parent = (i-1)/2;
            if (heap.get(i).priority() >= heap.get(parent).priority()) {
                break;
            }
            swap(i, parent);
            i = parent;
        }
    }
    /**
     * If an element is removed from the top, or an element's priority increases such that an element in the
     * tree gains a higher priority than one of its children, it is swapped with the child with the lowest priority
     * and this continues until it no longer has a higher priority than any of its children, or if it reaches
     * the lowest layer of the tree. Requires index i is contained in this heap.
     */
    private void bubbleDown(int i) {
        int size = heap.size();
        while (true) {
            int left = 2*i+1;
            int right = 2*i+2;
            int low = i;

            if (left < size && heap.get(left).priority() < heap.get(low).priority()) {
                low = left;
            }
            if (right < size && heap.get(right).priority() < heap.get(low).priority()) {
                low = right;
            }
            if (low == i) {
                break;
            }
            swap(i, low);
            i = low;
        }
    }
}
