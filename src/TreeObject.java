
/**
 * Holds the long representation of the DNA sequence and keeps track of frequency of occurrences
 */
public class TreeObject implements Comparable<TreeObject> {
    private long key;
    private int frequency = 1;

    /**
     * Creates a new key object with frequency 1 and specified DNA sequence
     *
     * @param key the DNA sequence
     */
    public TreeObject(long key) {
        this.key = key;
    }

    /**
     * creates a new object with specified key and frequency
     * @param key the DNa sequence
     * @param frequency of occurrence in BTree
     */
    public TreeObject(long key, int frequency) {
        this.key = key;
        this.frequency = frequency;
    }

    /**
     * @return the frequency of occurrence for the DNA sequence
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Increments the frequency by 1
     */
    public void incrementFrequency() {
        frequency++;
    }

    /**
     * @return the DNA sequence
     */
    public long getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof TreeObject) {
            return key == ((TreeObject) o).getKey();
        }
        return false;
    }

    @Override
    public String toString() {
        return "Key: " + key + " freq: " + frequency;
    }

    @Override
    public int compareTo(TreeObject o) {
        return Long.compare(key, o.getKey());
    }
}
