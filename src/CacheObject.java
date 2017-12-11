
/**
 * This class represents a cache object that stores a BTree node and its
 * location The equals method is overridden to compare the location of two cache
 * objects
 * 
 * @author Madeline Ross
 */
public class CacheObject
{
	private BTreeNode cacheNode;
	private int location;

	/**
	 * creates a new cache object with the BTree node and sets its location
	 * 
	 * @param b
	 *            the BTreeNode
	 */
	public CacheObject(BTreeNode b)
	{
		setCacheNode(b);
		location = b.getLocation();
	}

	/**
	 * sets the location of a cache object
	 * 
	 * @param loc
	 *            sets the location
	 */
	public CacheObject(int loc)
	{
		location = loc;
	}

	/**
	 * return the BTree node present in this cache object
	 * 
	 * @return the cacheNode
	 */
	public BTreeNode getCacheNode()
	{
		return cacheNode;
	}

	/**
	 * set the BTree node of this cache object
	 * 
	 * @param cacheNode
	 *            the cacheNode to set
	 */
	public void setCacheNode(BTreeNode cacheNode)
	{
		this.cacheNode = cacheNode;
	}

	/**
	 * returns the location of this cache object
	 * 
	 * @return the location
	 */
	public int getLocation()
	{
		if (cacheNode == null)
		{
			return location;
		}
		return cacheNode.getLocation();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof CacheObject)
		{
			return this.location == ((CacheObject) o).getLocation();
		}
		return false;
	}
}
