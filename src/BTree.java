import java.io.*;
import java.util.Stack;

public class BTree
{
	// byte sizes, update if changing what is written to file
	public static final int METADATA_BYTE_SIZE = 12; // nodeCount=4, degree=4, sequenceLength=4

	// fields
	// stored on disk
	private int degree;
	private int nodeCount;
	private int sequenceLength;

	// contents
	private BTreeNode root;
	private BTreeFile file;
	private boolean isUsingCache;
	private Cache<CacheObject> cache;

	// constructors

	/**
	 * Creates a BTree from an already existing BTreeFile with cache
	 * 
	 * @param file
	 *            an existing BTreeFile
	 * @param cacheSize
	 *            The max limit of cache
	 */
	public BTree(BTreeFile file, int cacheSize)
	{
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(file.readMetaData()));

		try
		{
			this.nodeCount = in.readInt();
			this.degree = in.readInt();
			this.sequenceLength = in.readInt();

			if (!isEmpty())
			{
				this.root = new BTreeNode(file.readNodeData(nodeCount - 1), this.degree);
			}
			isUsingCache = true;
			cache = new Cache<>(cacheSize);
			this.file = file;
		}
		catch (IOException e)
		{
			System.err.println("Unable to instantiate BTree from byte array!");
		}
	}

	/**
	 * Creates a BTree from an already existing BTreeFile
	 * 
	 * @param file
	 *            an existing BTreeFile
	 */
	public BTree(BTreeFile file)
	{
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(file.readMetaData()));

		try
		{
			this.nodeCount = in.readInt();
			this.degree = in.readInt();
			this.sequenceLength = in.readInt();

			if (!isEmpty())
			{
				this.root = new BTreeNode(file.readNodeData(nodeCount - 1), this.degree);
			}
			isUsingCache = false;
			this.file = file;
		}
		catch (IOException e)
		{
			System.err.println("Unable to instantiate BTree from byte array!");
		}
	}

	/**
	 * Init Btree from empty BTreeFile
	 * 
	 * @param file
	 *            empty file
	 * @param sequenceLength
	 *            length of DNA sequences
	 * @param degree
	 *            degree of the BTree
	 */
	public BTree(BTreeFile file, int sequenceLength, int degree)
	{
		init(degree, 0, sequenceLength, false);
		this.file = file;

		file.writeMetaData(this);
	}

	/**
	 * Init BTree from empty BTreeFile with cache
	 * 
	 * @param file
	 *            empty file
	 * @param sequenceLength
	 *            length of DNA sequences
	 * @param degree
	 *            degree of the BTree
	 * @param cacheSize
	 *            Limit on the size of the cache
	 */
	public BTree(BTreeFile file, int sequenceLength, int degree, int cacheSize)
	{
		init(degree, 0, sequenceLength, true);
		this.file = file;

		cache = new Cache<>(cacheSize);

		file.writeMetaData(this);
	}

	// public methods

	/**
	 * Writes the contents of the node cache to disk
	 */
	public void writeCacheToDisk()
	{
		while (!cache.isEmpty())
		{
			BTreeNode cur = (cache.removeFirst()).getCacheNode();
			file.writeNodeData(cur);
		}
	}

	/**
	 * search the tree for object by passed key and returns
	 * 
	 * @param key
	 *            the key to search for
	 * @return the tree object with matching key, null if nothing is found
	 */
	public TreeObject search(long key)
	{
		if (isEmpty())
		{
			return null;
		}

		TreeObject objToSearchFor = new TreeObject(key);
		boolean isFound = false;
		boolean atEnd = false;
		int tempLocation = -1; // temp variable to store location if the object was found
		BTreeNode current = root;

		while (!isFound && !atEnd)
		{
			BTreeNode.SearchResult result = current.searchNode(objToSearchFor);
			// found the object int the current node
			if (result.wasFound)
			{
				tempLocation = result.location;
				isFound = true;
			}
			// did not find the object in the current node
			else
			{
				// at the end of the true
				if (result.location == -1)
				{
					atEnd = true;
				}
				// update the current to the next node
				else
				{
					current = getNode(result.location);
				}
			}
		}
		if (!isFound)
		{
			return null;
		}
		return current.getObject(tempLocation);
	}

	/**
	 * Adds a key to the BTree
	 * 
	 * @param key
	 *            the key to be added
	 */
	public void add(long key)
	{
		TreeObject obj = new TreeObject(key);

		if (isEmpty())
		{
			createRootNode(obj, -1, -1);
		}
		else
		{
			BTreeNode parent = null;
			BTreeNode current = root;
			boolean isDuplicate = false;

			while ((!current.isLeafNode() && !isDuplicate) || (current.isFull() && current != parent))
			{

				if (current.isFull() && current != parent)
				{
					split(current, parent);
					current = parent == null ? root : parent;
				}
				else
				{
					BTreeNode.SearchResult result = current.searchNode(obj);
					if (result.wasFound)
					{
						current.incrementFrequency(result.location);
						isDuplicate = true;
					}
					else
					{ // not found
						parent = current;
						current = getNode(result.location);
					}
				}
			}

			if (!isDuplicate)
			{
				current.addObject(obj);
			}

			if (current != root)
			{
				writeNode(current);
			}
		}
	}

	/**
	 * write root to file and closes the BTree file No further actions can be made
	 * with the BTree after this is called
	 */
	public void close()
	{

		if (isUsingCache)
		{
			writeCacheToDisk();
		}

		if (root != null)
		{
			if (root.hasLocation())
			{
				writeNode(root);
			}
			else
			{
				file.appendNodeData(root);
			}

			if (!root.isLeafNode())
			{
				for (int i = 0; i < root.getNumOfKeys() + 1; ++i)
				{ // iterate through children
					BTreeNode child = getNode(root.getChild(i));
					child.setParent(root.getLocation());
					writeNode(child);
				}
			}
		}
		file.writeMetaData(this);
		file.close();
	}

	/**
	 * Adds a key to the BTree
	 * 
	 * @param key
	 *            the key to be added
	 */
	public void add(String key)
	{
		add(DNAUtil.convertStringToLong(key, sequenceLength));
	}

	/**
	 * @return the sequence length of the DNA sequences held in the BTree
	 */
	public int getSequenceLength()
	{
		return sequenceLength;
	}

	/**
	 * @return the degree of the BTree
	 */
	public int getDegree()
	{
		return degree;
	}

	/**
	 * @return true if the BTree is empty, false otherwise
	 */
	public boolean isEmpty()
	{
		return nodeCount == 0;
	}

	/**
	 * @return an array of the individual byte data for the BTree meta data
	 * @throws IOException
	 *             if unable to write to byte array
	 */
	public byte[] getMetaDataBytes() throws IOException
	{
		ByteArrayOutputStream ary = new ByteArrayOutputStream(METADATA_BYTE_SIZE);
		DataOutputStream out = new DataOutputStream(ary);

		out.writeInt(nodeCount);
		out.writeInt(degree);
		out.writeInt(sequenceLength);

		return ary.toByteArray();

	}

	/**
	 * Dumps the tree meta data and sequential node data to file
	 * 
	 * @param fileName
	 *            the name of file to create
	 */
	public void dumpDataToFile(String fileName)
	{
		try (PrintWriter out = new PrintWriter(new File(fileName)))
		{
			out.println("nodeCount=" + nodeCount);
			out.println("degree=" + degree);
			out.println("sequenceLength=" + sequenceLength);
			out.println();

			for (int i = 0; i < nodeCount - 1; ++i)
			{
				BTreeNode node = getNode(i);
				out.println(node.toString());
				out.println();
			}

			if (root != null)
			{
				out.println(root);
			}

			out.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Unable to create file " + fileName);
		}
	}

	/**
	 * Dumps the contents of the BTree in order to a file
	 * 
	 * @param fileName
	 *            the name of file to create
	 */
	public void dumpInOrderToFile(String fileName)
	{
		try (PrintWriter out = new PrintWriter(new File(fileName)))
		{

			BTreeNode current = root;
			Stack<Integer> childLocs = new Stack<>();
			childLocs.push(-1);

			while (!childLocs.empty())
			{
				int loc = childLocs.pop();

				if (current.isLeafNode())
				{
					out.print(getNodeContents(current));
					current = getNode(current.getParent());
				}
				else
				{
					int childCount = current.getChildCount();
					loc++;

					if (loc < childCount)
					{
						if (loc > 0)
						{
							TreeObject obj = current.getObject(loc - 1);
							out.println(DNAUtil.convertLongToString(obj.getKey(), sequenceLength) + ": "
									+ obj.getFrequency());
						}

						childLocs.push(loc);
						childLocs.push(-1); // load for next child
						current = getNode(current.getChild(loc));
					}
					else
					{
						current = getNode(current.getParent());
					}
				}
			}

			out.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Unable to create file " + fileName);
		}
	}

	// private methods

	/**
	 * returns a string of each key in a node on a newline
	 * 
	 * @param node
	 *            the node to get string of
	 * @return the string
	 */
	private String getNodeContents(BTreeNode node)
	{
		int keyCount = node.getNumOfKeys();
		StringBuilder sb = new StringBuilder((sequenceLength + 4) * keyCount + 1);

		for (int i = 0; i < keyCount; ++i)
		{
			TreeObject obj = node.getObject(i);
			sb.append(DNAUtil.convertLongToString(obj.getKey(), sequenceLength)).append(": ").append(obj.getFrequency())
					.append("\n");
		}

		return sb.toString();
	}

	/**
	 * Returns the node at a specified location in the file handles whether the
	 * BTree is using a cache or not
	 * 
	 * @param location
	 *            location of the node in file to be retrieved
	 * @return the BTreeNode at the location
	 */
	private BTreeNode getNode(int location)
	{
		BTreeNode retVal = null;

		if (location == -1)
		{
			retVal = root;
		}
		else if (!isUsingCache)
		{
			retVal = new BTreeNode(file.readNodeData(location), degree);
		}
		else
		{
			CacheObject cObj = cache.getObject(new CacheObject(location));

			if (cObj == null)
			{
				retVal = new BTreeNode(file.readNodeData(location), degree);

				CacheObject removed = cache.addObject(new CacheObject(retVal));
				if (removed != null)
					file.writeNodeData(removed.getCacheNode());
			}
			else
			{
				retVal = cObj.getCacheNode();
			}
		}
		return retVal;
	}

	/**
	 * Writes a node to file. Handles whether or not the tree is using a cache
	 * 
	 * @param node
	 *            the node to write
	 */
	private void writeNode(BTreeNode node)
	{
		if (!isUsingCache)
		{
			file.writeNodeData(node);
		}
		else
		{
			CacheObject obj = cache.getObject(new CacheObject(node));
			if (obj == null)
			{
				file.writeNodeData(node);
			}
		}
	}

	/**
	 * Inits the objects variables
	 * 
	 * @param degree
	 * @param nodeCount
	 * @param sequenceLength
	 * @param isUsingCache
	 */
	private void init(int degree, int nodeCount, int sequenceLength, boolean isUsingCache)
	{
		this.degree = degree;
		this.nodeCount = nodeCount;
		this.sequenceLength = sequenceLength;
		this.isUsingCache = isUsingCache;
	}

	/**
	 * 
	 */
	private void split(BTreeNode current, BTreeNode parent)
	{

		// assign left and right halves after split
		BTreeNode left = current.leftFromSplit();
		BTreeNode right = current.rightFromSplit();

		if (isUsingCache)
		{
			cache.removeObject(new CacheObject(current));
		}

		// handle split of root
		if (current == root)
		{
			if (current.hasLocation())
			{
				writeNode(left);
			}
			else
			{
				file.appendNodeData(left); // append
			}

			file.appendNodeData(right); // append
			createRootNode(current.middleFromSplit(), left.getLocation(), right.getLocation());
		}
		else
		{ // handle non root split
			left.setLocation(current.getLocation());
			writeNode(left); // by node location
			file.appendNodeData(right);// append
			int index = parent.addObject(current.middleFromSplit());

			// update parent child pointers
			parent.setChild(parent.left(index), left.getLocation());
			parent.setChild(parent.right(index), right.getLocation());
			if (parent != root)
			{
				writeNode(parent);
			}

		}

		// update right node children and parent pointers
		if (!right.isLeafNode())
		{
			int childCount = right.getNumOfKeys() + 1;

			// update children
			for (int i = 0; i < childCount; ++i)
			{
				BTreeNode child = getNode(right.getChild(i));
				child.setParent(right.getLocation());
				writeNode(child);

				// update left only if current was root with no location
				if (!current.hasLocation())
				{
					child = getNode(left.getChild(i));
					child.setParent(left.getLocation());
					writeNode(child);
				}
			}
		}

		nodeCount++;

	}

	/**
	 * Creates a new root node and assigns root variable to said node
	 * 
	 * @param key
	 *            the object to create the root node with
	 */
	private void createRootNode(TreeObject key, int left, int right)
	{
		if (isEmpty())
		{
			root = new BTreeNode(key, left, right, degree, true);
		}
		else
		{
			root = new BTreeNode(key, left, right, degree, false);
		}
		nodeCount++;
	}
}
