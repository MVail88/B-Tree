/**
 * A node for use with a generic double linked list implementation of a data
 * structure.
 * 
 * @author Madeline Ross
 */
public class DLLNode<E>
{

	// fields
	private DLLNode<E> next;
	private DLLNode<E> previous;
	private E element;

	// constructors

	/**
	 * Constructs a new node with a given element
	 * 
	 * @param element
	 *            the element to be referenced by the node
	 */
	public DLLNode(E element)
	{
		init(element);
	}

	/**
	 * Constructs a new node with null as the element
	 */
	public DLLNode()
	{
		init(element);
	}

	// methods

	/**
	 * Initializes the links of a node to null and sets the element to the passed
	 * one
	 * 
	 * @param element
	 *            the element to be referenced by the node
	 */
	private void init(E element)
	{
		setNext(null);
		setPrevious(null);
		setElement(element);
	}

	/**
	 * @return the element referenced by the node
	 */
	public E getElement()
	{
		return element;
	}

	/**
	 * Sets the element referenced by the node
	 * 
	 * @param element
	 *            the element to be referenced
	 */
	public void setElement(E element)
	{
		this.element = element;
	}

	/**
	 * Sets the reference to the next node
	 * 
	 * @param next
	 *            the node to be referenced
	 */
	public void setNext(DLLNode<E> next)
	{
		this.next = next;
	}

	/**
	 * Gets the next node reference
	 * 
	 * @return the next node
	 */
	public DLLNode<E> getNext()
	{
		return next;
	}

	/**
	 * Sets the reference to the previous node
	 * 
	 * @param previous
	 *            the node to be referenced
	 */
	public void setPrevious(DLLNode<E> previous)
	{
		this.previous = previous;
	}

	/**
	 * Gets the previous node reference
	 * 
	 * @return the previous node
	 */
	public DLLNode<E> getPrevious()
	{
		return previous;
	}
}
