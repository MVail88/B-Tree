import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Handles writing and reading to/from a BTreeFile There is no public
 * constructor for this class, construction must be done from factory methods
 * createNewBTreeFile or openBTreeFile. this class abstracts byte offsets to
 * node positions. positions start at 0 and increment from there. eg. third node
 * in file is at position 2.
 */
public class BTreeFile
{
	// fields
	private RandomAccessFile file;
	private int treeMetaDataLength;
	private int nodeDataLength;
	private int nextNodePlacement;

	// constructor
	private BTreeFile()
	{

	}

	/**
	 * Private constructor for initializing BTreeFile
	 * 
	 * @param treeMetaDataLength
	 *            length in bytes of the BTree meta data
	 * @param nodeDataLength
	 *            length in bytes of the BTreeNode
	 * @param nextNodePlacement
	 *            Next node placement (eg end of file)
	 */
	private BTreeFile(int treeMetaDataLength, int nodeDataLength, int nextNodePlacement)
	{
		this.treeMetaDataLength = treeMetaDataLength;
		this.nodeDataLength = nodeDataLength;
		this.nextNodePlacement = nextNodePlacement;
	}

	// public methods

	public byte[] readNodeData(int position)
	{
		if (position < 0 || position >= nextNodePlacement)
		{
			throw new IllegalArgumentException("Invalid position argument: " + position);
		}

		byte[] retVal = null;

		try
		{

			file.seek(nodeByteOffset(position));
			retVal = new byte[nodeDataLength];
			file.read(retVal);

		}
		catch (IOException e)
		{
			System.err.println("Unable to read node data at position " + position);
		}

		return retVal;
	}

	/**
	 * Reads the BTree meta data at the beginning of the file
	 * 
	 * @return a byte array containing the meta data
	 */
	public byte[] readMetaData()
	{
		try
		{

			file.seek(0);
			byte[] retVal = new byte[treeMetaDataLength];
			file.read(retVal);
			return retVal;

		}
		catch (IOException e)
		{
			System.err.println("Unable to read meta data");
			return null;
		}

	}

	/**
	 * @return the next empty position for a node to be written to
	 */
	public int getNextPosition()
	{
		return nextNodePlacement;
	}

	/**
	 * Writes node data at the end of the list of nodes in the file
	 * 
	 * @param node
	 *            the node whose data will be written
	 */
	public void appendNodeData(BTreeNode node)
	{
		writeNodeData(node, nextNodePlacement);
	}

	/**
	 * @param node
	 *            writes node to file that already has a set location variable
	 */
	public void writeNodeData(BTreeNode node)
	{
		if (!node.hasLocation())
		{
			throw new IllegalArgumentException("Node's location is not set!");
		}

		writeNodeData(node, node.getLocation());
	}

	/**
	 * Writes node data at specified node position in file sets the node's location
	 * to passed position
	 * 
	 * @param node
	 *            the node whose data will be written
	 * @param position
	 *            the node position where the data will be written (not a byte
	 *            offset)
	 */
	public void writeNodeData(BTreeNode node, int position)
	{
		if (position < 0 || position > nextNodePlacement)
		{
			throw new IllegalArgumentException("Invalid node position");
		}

		try
		{
			node.setLocation(position);
			byte[] bytes = node.getBytes();
			if (bytes.length != nodeDataLength)
			{
				throw new IllegalArgumentException(
						"Amount of Bytes to be written must equal specified node data length");
			}

			file.seek(nodeByteOffset(position));
			file.write(bytes);
		}
		catch (IOException e)
		{
			System.err.println("Unable to write node data at position " + position);
		}

		if (position == nextNodePlacement)
		{ // placed at the end of file
			nextNodePlacement++;
		}
	}

	/**
	 * Writes the BTrees meta data to the beginning of the file
	 * 
	 * @param tree
	 *            the tree with the data to write
	 */
	public void writeMetaData(BTree tree)
	{
		try
		{
			byte[] bytes = tree.getMetaDataBytes();

			if (bytes.length != treeMetaDataLength)
				throw new IllegalArgumentException(
						"Amount of bytes to be written must equal specified meta data length");

			file.seek(0);
			file.write(bytes);
		}
		catch (IOException e)
		{
			System.err.println("Unable to write BTree meta data");
		}
	}

	/**
	 * Creates a new BTreeFile. Deletes any existing file with same name
	 * 
	 * @param fileName
	 *            file path to be created, can be absolute or relative
	 * @param treeMetaDataLength
	 *            the length of the BTree meta data in bytes
	 * @param nodeDataLength
	 *            the length of a BTreeNode in bytes
	 * @return A BtreeFile tied to the specified file path
	 * @throws IOException
	 *             if an error occurs in the file creation/deletion process
	 */
	public static BTreeFile createNewBTreeFile(String fileName, int treeMetaDataLength, int nodeDataLength)
			throws IOException
	{
		BTreeFile retVal = new BTreeFile(treeMetaDataLength, nodeDataLength, 0);

		File f = new File(fileName);
		if (fileName.contains("/"))
		{
			f.getParentFile().mkdirs();
		}
		if (f.exists())
		{
			if (!f.delete())
				throw new IOException("Could not delete " + fileName);
		}

		if (!f.createNewFile())
			throw new IOException("Could not create " + fileName);

		retVal.file = new RandomAccessFile(f, "rw");

		return retVal;
	}

	/**
	 * Opens an existing BTreeFile from memory
	 * 
	 * @param fileName
	 *            file path to existing BTreeFile
	 * @return A BTreeFile tied to the specified file path
	 * @throws IOException
	 *             if an error occurs during the process
	 */
	public static BTreeFile openBTreeFile(String fileName, int treeMetaDataLength) throws IOException
	{
		File f = new File(fileName);
		if (!f.exists())
			throw new FileNotFoundException("Unable to find file " + fileName);

		RandomAccessFile file = new RandomAccessFile(f, "rw");

		int nodeCount = file.readInt();
		int degree = file.readInt();
		int nodeDataLength = BTreeNode.getByteSize(degree);

		BTreeFile retVal = new BTreeFile(treeMetaDataLength, nodeDataLength, nodeCount);
		retVal.file = file;

		if (nodeCount != 0)
		{
			retVal.readNodeData(0);
			retVal.readNodeData(nodeCount - 1);
		}

		return retVal;
	}

	/**
	 * Closes the BTreeFile (no further read or write actions can be made)
	 */
	public void close()
	{
		try
		{
			file.close();
		}
		catch (IOException e)
		{
			System.err.println("Unable to close BTreeFile");
		}
	}

	// private methods

	/**
	 * returns the byte offset for a node position in a file
	 * 
	 * @param position
	 *            the desired byte offset for said position
	 * @return the byte offset
	 */
	private long nodeByteOffset(int position)
	{
		return treeMetaDataLength + position * nodeDataLength;
	}
}
