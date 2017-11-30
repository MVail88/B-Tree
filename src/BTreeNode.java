import java.io.*;
import java.util.ArrayList;

public class BTreeNode {
    //byte sizes, update if changing what is written to file
    public static final int NODE_META_BYTE_SIZE = 9; //location=4, #keys=4, isLeaf=1
    public static final int POINTER_BYTE_SIZE = 4; //int size
    public static final int OBJECT_BYTE_SIZE = 12; //key=8, frequency=4


    //fields
    //stored on disk
    private boolean isLeafNode; //boolean stored as byte that is 1 for true, 0 for false
    private int location = -1;
    private int parent = -1;
    private ArrayList<TreeObject> objects;
    private ArrayList<Integer> children;

    //contents
    private int keyLimit;

    //constructors

    /**
     * Creates a new object from array of keys and children
     * @param objects keys for new node
     * @param children children pointers
     * @param parent parent pointer
     * @param degree degree of the tree
     * @param isLeaf if the node is a leaf
     */
    public BTreeNode(ArrayList<TreeObject> objects, ArrayList<Integer> children, int parent, int degree, boolean isLeaf) {
        if (objects.size() != children.size() - 1)
            throw new IllegalArgumentException("there key count must be child count + 1");

        isLeafNode = isLeaf;
        this.parent = parent;
        this.objects = objects;
        this.children = children;
        keyLimit = 2*degree - 1;

        if (objects.size() > keyLimit) {
            throw new IllegalArgumentException("Node has too many objects for tree of specified degree");
        }
    }

    /**
     * Creates a new Node in a root context
     * @param initial the initial tree object
     * @param left left child
     * @param right right child
     * @param degree degree of the tree
     */
    public BTreeNode(TreeObject initial, int left, int right, int degree, boolean isLeaf) {
        if (initial == null) {
            throw new NullPointerException("Node cannot hold null object");
        }
        isLeafNode = isLeaf;
        parent = -1;

        objects = new ArrayList<>();
        objects.add(initial);

        children = new ArrayList<>();
        children.add(left);
        children.add(right);

        keyLimit = 2*degree - 1;

    }

    /**
     * Instantiates a Node from existing data in storage
     * @param bytes data related to node
     * @param degree degree of the tree
     */
    public BTreeNode(byte[] bytes, int degree) {
        keyLimit = 2*degree - 1;
        if (bytes.length != getByteSize(degree)) {
            throw new IllegalArgumentException("byte array must be the amount of bytes required for a BTreeNode of specified degree");
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

        try {
            location = in.readInt();
            int numOfKeys = in.readInt();
            isLeafNode = in.readBoolean();
            objects = new ArrayList<>();
            children = new ArrayList<>();

            //object data
            for (int i = 0 ; i < numOfKeys ; ++i) {
                long key = in.readLong();
                int frequency = in.readInt();
                objects.add(new TreeObject(key, frequency));
            }

            parent = in.readInt();

            //children
            for (int i = 0 ; i < numOfKeys + 1 ; ++i) {
                children.add(in.readInt());
            }

        } catch (IOException e) {
            System.err.println("Unable to build BTreeNode from byte array");
        }
    }


    //public methods

    /**
     * @return how many keys are in the node
     */
    public int getNumOfKeys() {
        return objects.size();
    }

    /**
     * sets the location of the node in file
     * @param location the location of the node
     */
    public void setLocation(int location) {
        this.location = location;
    }

    /**
     * @return true if the node has a location, false if node location == -1
     */
    public boolean hasLocation() {
        return location != -1;
    }
    /**
     * @return the nodes location in file
     */
    public int getLocation() {
        return location;
    }

    /**
     * increments the frequency of a TreeObject at the specified index
     * @param index the index of the oject
     */
    public void incrementFrequency(int index) {
        objects.get(index).incrementFrequency();
    }

    /**
     * @return the max amount of keys the node is able to hold
     */
    public int getKeyLimit() {
        return keyLimit;
    }

    /**
     * sets the parent pointer of the node
     * @param pointer pointer for the parent to be set to
     */
    public void setParent(int pointer) {
        this.parent = pointer;
    }
    /**
     * @return the parent position in the BTreeFile
     */
    public int getParent() {
        return parent;
    }

    /**
     * @return if this node is a leaf node in the BTree
     */
    public boolean isLeafNode() {
        return isLeafNode;
    }

    /**
     * Sets a child pointer in node
     * @throws IndexOutOfBoundsException if index < 0 or >= #children
     * @param index index of child pointer
     * @param value value of child pointer
     */
    public void setChild(int index, int value) {
        children.set(index, value);
    }

    /**
     * @param index index of the child pointer to be returned
     * @return the pointer at the specified index in the node
     */
    public int getChild(int index) {
        return children.get(index);
    }

    /**
     * @return the amount of child pointers in the node
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * @param index index of the object to be returned
     * @return The object at the specified index in the node
     */
    public TreeObject getObject(int index) {
        return objects.get(index);
    }

    public boolean isFull() {
        return objects.size() >= keyLimit;
    }

    /**
     * Adds a tree object and child pointer to the node. child pointer is added to right of index
     * @throws IllegalStateException if node is full
     * @return The index where the object was added or -1 if object was a duplicate
     * @param obj TreeObject to be added
     */
    public int addObject(TreeObject obj) {
        if (isFull()) {
            throw new IllegalStateException("Node is full");
        }

        int index = 0;
        boolean atEnd = index >= objects.size();
        while (!atEnd && obj.compareTo(objects.get(index)) > 0) {
            index++;
            atEnd = index >= objects.size();
        }

        if (!atEnd && obj.compareTo(objects.get(index)) == 0) {
            objects.get(index).incrementFrequency();
            return -1;
        }
        else {
            objects.add(index, obj);
            children.add(index+1, -1);
            return index;
        }
    }

    /**
     * Searches node for an object and returns a result.
     * SearchResult contains two values, a boolean wasFound, and int location
     * location represents different values depending on the state of wasFound
     * if wasFound = true: location in the index of the object in the node
     * if wasFound = false: location is the pointer to the correct child to continue the BTree search
     *                      location can also be -1, meaning the node is a leaf and the object was not found
     *
     * @param obj the object to search for
     * @return the result from the search
     */
    public SearchResult searchNode(TreeObject obj) {
        int index = 0;
        boolean atEnd = index >= objects.size();
        while (!atEnd && obj.compareTo(objects.get(index)) > 0) {
            index++;
            atEnd = index >= objects.size();
        }

        if (!atEnd && obj.compareTo(objects.get(index)) == 0) { //found object
            return new SearchResult(index, true);
        }
        else { //object was not found
            if (isLeafNode) { //no children, so return nothing found
                return new SearchResult(-1, false);
            }
            else { //return child pointer
                return new SearchResult(children.get(left(index)), false);
            }
        }
    }

    /**
     * @return the middle key in the full node
     * @throws IllegalStateException if the node isn't full
     */
    public TreeObject middleFromSplit() {
        if (!isFull())
            throw new IllegalStateException("A node can only split when it is full");

        return objects.get(keyLimit/2);
    }
    /**
     * @return the right portion BTreeNode after splitting
     * @throws IllegalStateException if the node isn't full
     */
    public BTreeNode rightFromSplit() {
        if (!isFull())
            throw new IllegalStateException("A node can only split when it is full");

        ArrayList<TreeObject> ary = new ArrayList<>(objects.subList((keyLimit / 2)+1, objects.size()));
        ArrayList<Integer> childs = new ArrayList<>(children.subList((keyLimit / 2)+1, children.size()));

        return new BTreeNode(ary, childs, parent, (keyLimit+1)/2, isLeafNode);
    }

    /**
     * @return the left portion BTreeNode after splitting
     * @throws IllegalStateException if the node isn't full
     */
    public BTreeNode leftFromSplit() {
        if (!isFull())
            throw new IllegalStateException("A node can only split when it is full");

        ArrayList<TreeObject> ary = new ArrayList<>(objects.subList(0, keyLimit / 2));
        ArrayList<Integer> childs = new ArrayList<>(children.subList(0, (keyLimit / 2)+1));

        return new BTreeNode(ary, childs, parent, (keyLimit+1)/2, isLeafNode);
    }

    /**
     * returns the index where the object is in the node. -1 if the object is not present
     * @param obj object to search for
     * @return index of object, -1 if not found
     */
    public int indexOf(TreeObject obj) {
        int index = 0;
        boolean found = false;

        while (!found && index < objects.size()) {
            if (objects.get(index).equals(obj)) {
                found = true;
            }
            else
                index++;
        }

        if (!found) {
            return -1;
        }

        return index;
    }

    /**
     * given a treeObject in the node, this method returns the index of the child to the object direct right
     * @param index index of object
     * @return location of right child to object in file
     */
    public int right(int index) {

        return index + 1;
    }

    /**
     * given a treeObject in the node, this method returns the index of the child to the objects direct left
     * @param index index of the object
     * @return location of the left child to object in file
     */
    public int left(int index) {

        return index;
    }

    /**
     * @return The array of bytes that represent the data stored in this object
     * @throws IOException if unable to write to byte array
     */
    public byte[] getBytes() throws IOException{
        int byteSize = getByteSize();
        ByteArrayOutputStream ary = new ByteArrayOutputStream(byteSize);
        DataOutputStream out = new DataOutputStream(ary);

        //metadata
        out.writeInt(location);
        out.writeInt(objects.size());
        out.writeBoolean(isLeafNode);

        //contents
        //write keys
        for (TreeObject obj : objects) {
            out.writeLong(obj.getKey());
            out.writeInt(obj.getFrequency());
        }

        //parent
        out.writeInt(parent);

        //children
        for (int pointer : children) {
            out.writeInt(pointer);
        }

        //empty space
        while (ary.size() < byteSize) {
            out.write(0);
        }

        byte[] bytes = ary.toByteArray();

        assert bytes.length == byteSize;

        return ary.toByteArray();
    }

    /**
     * @param degree The degree of the tree
     * @return how many bytes a node in a tree of specified degree will take on storage
     */
    public static int getByteSize(int degree) {
        if (degree <= 1) {
            throw new IllegalArgumentException("Degree must be > 1");
        }

        int byteSize = 0;
        byteSize += 9; //location=4, #keys=4, isLeaf=1
        byteSize += POINTER_BYTE_SIZE; //parent pointer
        byteSize += (2 * degree - 1) * OBJECT_BYTE_SIZE; //largest amount of space possible to be taken by objects
        byteSize += (2 * degree) * POINTER_BYTE_SIZE; //largest amount of space possible to be taken by child pointers
        return byteSize;
    }

    /**
     * @return how many bytes this node will take on a storage
     */
    public int getByteSize() {
        int degree = (keyLimit + 1) / 2;
        return getByteSize(degree);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("#keys=" + objects.size() + " isLeaf=" + isLeafNode +
                        " location=" + location + "\n");
        sb.append("parent=" + parent + "\n");

        sb.append("Objects:\n");
        for (TreeObject t : objects) {
            sb.append(t.toString()).append("\n");
        }

        sb.append("Children:\n");
        for (int c : children) {
            sb.append(c).append("\n");
        }

        sb.append("end\n");
        return sb.toString();
    }

    //private methods

    /**
     * Returned from the search method.
     * The value of location represents two different things depending on the state of wasFound
     *
     * wasFound = true: location represents searched objects index in the current node
     * wasFound = false: location represents the pointer to the proper child to continue the search
     *                   location can also be -1, meaning the node is a leaf and the object was not there
     */
    class SearchResult {
        final boolean wasFound;
        final int location;

        SearchResult(int location, boolean wasFound) {
            this.location = location;
            this.wasFound = wasFound;
        }
    }
}
