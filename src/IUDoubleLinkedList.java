import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A double linked list implementation of the IndexedUnsortedList interface.
 * This collection supports fail fast Iterators and ListIterators from the
 * java.util package.
 * This List checks for element equality by address, not value
 * @author Jordan Paoletti
 */
public class IUDoubleLinkedList<T> implements IndexedUnsortedList<T> {

    //fields
    private DLLNode<T> dummy;
    private int size;
    private int modcount;


    //constructors

    /**
     * Creates a new empty double linked list
     */
    public IUDoubleLinkedList() {
        dummy = new DLLNode<>(); //dummy node with no element
        dummy.setNext(dummy);
        dummy.setPrevious(dummy);

        size = 0;
        modcount = 0;
    }

    //iterator

    /**
     * An implementation of the java.util.ListIterator interface for the
     * IUDoubleLinkedList class.
     */
    private class IUDoubleLinkedList_ListIterator implements ListIterator<T> {

        //fields
        int nextIndex;
        int modcount;
        DLLNode<T> next;
        DLLNode<T> previous;
        boolean canRemove;
        boolean nextWasCalled;

        //constructors

        /**
         * Creates a new ListIterator at the beginning of the list
         */
        private IUDoubleLinkedList_ListIterator() {
            nextIndex = 0;
            modcount = IUDoubleLinkedList.this.modcount;
            canRemove = false;
            nextWasCalled = false;

            next = head();
            previous = dummy;
        }

        /**
         * Creates a new ListIterator at the specified index
         * @param index the index where the iterator should be created at
         */
        private IUDoubleLinkedList_ListIterator(int index) {
            nextIndex = index;
            modcount = IUDoubleLinkedList.this.modcount;
            canRemove = false;
            nextWasCalled = false; //used to determine if previous or next was the last movement

            next = nodeAtIndex(index);
            previous = next.getPrevious();
        }

        //methods

        /**
         * verifies that the iterator modcount and list modcount are identical
         * @throws ConcurrentModificationException if modcounts are not identical
         */
        private void verifyModCount() {
            if (modcount != IUDoubleLinkedList.this.modcount) {
                throw new ConcurrentModificationException("IUDoubleLinkedList_ListIterator - verifyModCount");
            }
        }

        /**
         * Increments the list and iterator modcounts
         */
        private void incModCounts() {
            modcount++;
            IUDoubleLinkedList.this.modcount++;
        }

        /**
         * removes the proper node after next() has been called
         */
        private void removeAfterNext() {
            DLLNode<T> toBeRemoved = previous;
            previous = previous.getPrevious();

            //update connections
            previous.setNext(next);
            next.setPrevious(previous);

            toBeRemoved.setNext(null);
            toBeRemoved.setPrevious(null);
            toBeRemoved.setElement(null);
        }

        /**
         * removes the proper node after previous() has been called
         */
        private void removeAfterPrevious() {
            DLLNode<T> toBeRemoved = next;
            next = next.getNext();

            //update connections
            previous.setNext(next);
            next.setPrevious(previous);

            toBeRemoved.setNext(null);
            toBeRemoved.setPrevious(null);
            toBeRemoved.setElement(null);
        }

        @Override
        public boolean hasNext() {
            return next != dummy;
        }

        @Override
        public T next() {
            verifyModCount();

            if (!hasNext()) {
                throw new NoSuchElementException("IUDoubleLinkedList - next");
            }

            //update node references
            previous = next;
            next = next.getNext();

            //update fields
            nextIndex++;
            canRemove = true;
            nextWasCalled = true;

            return previous.getElement();
        }

        @Override
        public boolean hasPrevious() {
            return previous != dummy;
        }

        @Override
        public T previous() {
            verifyModCount();

            if (!hasPrevious()) {
                throw new NoSuchElementException("IUDoubleLinkedList - previous");
            }

            //update node references
            next = previous;
            previous = previous.getPrevious();

            //update fields
            nextIndex--;
            canRemove = true;
            nextWasCalled = false;

            return next.getElement();

        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            if (canRemove) {
                if (nextWasCalled) {
                    removeAfterNext();

                    nextIndex--;

                }
                else {
                    removeAfterPrevious();
                }

                canRemove = false;
                incModCounts();
                --size;
            }
            else {
                throw new IllegalStateException("IUDoubleLinkedList_ListIterator - remove");
            }
        }

        @Override
        public void set(T t) {
            if (canRemove) { //canRemove is only true if add/remove have not been called
                if (nextWasCalled) {
                    previous.setElement(t);
                }
                else {
                    next.setElement(t);
                }

                incModCounts();
            }
            else {
                throw new IllegalStateException("IUDoubleLinkedList_ListIterator - set");
            }
        }

        @Override
        public void add(T t) {
            //create newNode
            DLLNode<T> newNode = new DLLNode<>(t);

            //update connections
            newNode.setNext(next);
            newNode.setPrevious(previous);

            next.setPrevious(newNode);
            previous.setNext(newNode);

            //update previous reference
            previous = newNode;

            canRemove = false;
            nextIndex++;
            size++;
            incModCounts();
        }
    }

    //methods

    /**
     * Finds the first node that contains the specified element.
     * compares by address, not value
     * @param element the element to be found
     * @return the node holding said element
     * @throws NoSuchElementException if the element is not found
     */
    private DLLNode<T> firstNodeWithElement(T element) {
        DLLNode<T> retVal = head();

        boolean found = false;

        while (!found && retVal != dummy) {
            if (retVal.getElement() == element) {
                found = true;
            }
            else {
                retVal = retVal.getNext();
            }
        }

        if (retVal == dummy) {
            throw new NoSuchElementException("IUDoubleLinkedList - firstNodeWithElement");
        }

        return retVal;
    }

    /**
     * Returns a reference to the head node of the list
     * returns the dummy if list is empty
     * @return the head node
     */
    private DLLNode<T> head() {
        return dummy.getNext();
    }

    /**
     * Returns a reference to the tail node of the list
     * returns the dummy if list is empty
     * @return the tail node
     */
    private DLLNode<T> tail() {
        return dummy.getPrevious();
    }

    /**
     * Gets the reference to the node at the specified index
     * This is not a safe method, no index checks are performed
     * @param index the index of the node to be returned
     * @return the node at the specified index
     */
    private DLLNode<T> nodeAtIndex(int index) {
        DLLNode<T> retVal = dummy;

        if (index > size / 2) {//start from tail
            for (int i = size ; i > index ; --i) {
                retVal = retVal.getPrevious();
            }
        }
        else { //start from head
            for (int i = -1 ; i < index ; ++i) {
                retVal = retVal.getNext();
            }
        }

        return retVal;
    }


    @Override
    public void addToFront(T element) {
        add(0, element);
    }

    @Override
    public void addToRear(T element) {
        add(size, element);
    }

    @Override
    public void add(T element) {
        addToRear(element);
    }

    @Override
    public void addAfter(T element, T target) {
        //find target node
        //throws NoSuchElementException if node is not found
        DLLNode<T> targetNode = firstNodeWithElement(target);

        //create new node
        DLLNode<T> newNode = new DLLNode<>(element);

        //update connections
        DLLNode<T> next = targetNode.getNext();

        newNode.setNext(next);
        newNode.setPrevious(targetNode);

        targetNode.setNext(newNode);
        next.setPrevious(newNode);

        //update fields
        size++;
        modcount++;
    }

    @Override
    public void add(int index, T element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("IUDoubleLinkedList - add");
        }

        //get node before and after insertion point
        DLLNode<T> prev = nodeAtIndex(index - 1);
        DLLNode<T> next = prev.getNext();

        //create a new node
        DLLNode<T> newNode = new DLLNode<>(element);

        //update links
        newNode.setNext(next);
        newNode.setPrevious(prev);

        prev.setNext(newNode);
        next.setPrevious(newNode);

        size++;
        modcount++;
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            throw new IllegalStateException("IUDoubleLinkedList - removeFirst");
        }

        return remove(0);
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            throw new IllegalStateException("IUDoubleLinkedList - removeLast");
        }

        return remove(size - 1);
    }

    @Override
    public T remove(T element) {
        //throws NoSuchElementException if element is not found
        DLLNode<T> target = firstNodeWithElement(element);
        T retVal = target.getElement();

        DLLNode<T> prev = target.getPrevious();
        DLLNode<T> next = target.getNext();

        //update connections
        prev.setNext(next);
        next.setPrevious(prev);

        target.setNext(null);
        target.setPrevious(null);
        target.setElement(null);

        //update fields
        size--;
        modcount++;

        return retVal;
    }

    @Override
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("IUDoubleLinkedList - remove(index)");
        }

        //get references
        DLLNode<T> toBeRemoved = nodeAtIndex(index);
        DLLNode<T> prev = toBeRemoved.getPrevious();
        DLLNode<T> next = toBeRemoved.getNext();

        T retVal = toBeRemoved.getElement();

        //update links
        toBeRemoved.setPrevious(null);
        toBeRemoved.setNext(null);
        toBeRemoved.setElement(null);

        prev.setNext(next);
        next.setPrevious(prev);

        size--;
        modcount++;
        return retVal;

    }

    @Override
    public void set(int index, T element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("IUDoubleLinkedList - set");
        }

        nodeAtIndex(index).setElement(element);
        modcount++;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("IUDoubleLinkedList - get");
        }

        return nodeAtIndex(index).getElement();
    }

    @Override
    public int indexOf(T element) {
        DLLNode<T> current = head();
        int index = 0;
        boolean found = false;

        while (!found && current != dummy) {
            if (current.getElement() == element) {
                found = true;
            }
            else {
                current = current.getNext();
                index++;
            }
        }

        if (!found) {
            index = -1;
        }
        return index;
    }

    @Override
    public T first() {
        if (size == 0) {
            throw new IllegalStateException("IUDoubleLinkedList - first");
        }

        return head().getElement();
    }

    @Override
    public T last() {
        if (size == 0) {
            throw new IllegalStateException("IUDoubleLinkedList - last");
        }

        return tail().getElement();
    }

    @Override
    public boolean contains(T target) {
        return indexOf(target) != -1;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new IUDoubleLinkedList_ListIterator();
    }

    @Override
    public ListIterator<T> listIterator() {
        return new IUDoubleLinkedList_ListIterator();
    }

    @Override
    public ListIterator<T> listIterator(int startingIndex) {
        if (startingIndex < 0 || startingIndex > size) {
            throw new IndexOutOfBoundsException("IUDoubleLinkedList - ListIterator(index)");
        }

        return new IUDoubleLinkedList_ListIterator(startingIndex);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");

        boolean firstElement = true;

        for (T t : this) {
            if (!firstElement) {
                sb.append(", ");
            }
            else {
                firstElement = false;
            }

            sb.append(t.toString());

        }
        sb.append("]");

        return sb.toString();
    }
}
