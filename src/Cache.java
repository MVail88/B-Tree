import java.util.ListIterator;

/**
 * this class stores objects up to a specified capacity. Adding objects when the capacity is at max
 * results in the last object in the cache to be removed. Objects are always added to the front of the
 * cache. when an object is retrieved from a cache, that object is move to the front of the cache.
 * @author Jordan Paoletti
 */
public class Cache<T> {

    //fields
    private int capacity;
    private IUDoubleLinkedList<T> list;

    //constructor

    /**
     * creates a Cache with specified capacity
     * @param size the size of the cache
     */
    public Cache(int size) {
        capacity = size;
        list = new IUDoubleLinkedList<>();
    }


    //public methods

    /**
     * Returns the first object that .equals the passed object and moves said object to front of cache
     * @param object the object to get
     * @return the returned object, null if the object was not found
     */
    public T getObject(T object) {
        T retVal = removeObject(object);

        if (retVal != null) {
            addObject(retVal);
        }

        return retVal;
    }

    /**
     * Adds an object to the front of the cache
     * @param object the object to be added
     * @throws NullPointerException if passed object is null
     * @return the object pushed out of the cache, or null if cache was not full
     */
    public T addObject(T object) {
        if (object == null) {
            throw new NullPointerException("added object cannot be null");
        }

        T retVal = null;

        if (list.size() >= capacity) {
           retVal = list.removeLast();
        }
        list.addToFront(object);

        return retVal;
    }

    /**
     * removes an object from the cache and returns it
     * @param object the object to be removed
     * @return the removed object. returns null if the object was not found
     */
    public T removeObject(T object) {
        boolean found = false;
        ListIterator<T> itr = list.listIterator();
        T retVal = null;

        while (!found && itr.hasNext()) {
            retVal = itr.next();

            if (retVal.equals(object)) {
                found = true;
            }
        }

        if (!found) {
            retVal = null;
        }
        else {
            itr.remove();
        }

        return retVal;
    }

    /**
     * @return true if cache is empty, false otherwise
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }
    /**
     * @return the first object in the cache, null if empty
     */
    public T removeFirst() {
        return list.isEmpty() ? null : list.removeFirst();
    }

    /**
     * clears the cache
     */
    public void clearCache() {
        list = new IUDoubleLinkedList<>();
    }
}
