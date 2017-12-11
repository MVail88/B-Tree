import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Read in a query file a make sure the String sequences are valid figure out
 * the sequence length
 *
 * @author Madeline Ross
 * @author Martin Vail
 */
public class QueryFile implements Iterable<String>
{
	private int sequenceLength;
	private String fileName;

	/**
	 * Instantiates a new QueryFile ready to be iterated through with a for each
	 * loop
	 * 
	 * @param fileName
	 *            the name of the file to open
	 * @throws FileNotFoundException
	 *             if file doesn't exist
	 * @throws IllegalArgumentException
	 *             if file is improper format
	 */
	public QueryFile(String fileName) throws FileNotFoundException, IllegalArgumentException
	{
		Scanner scanner = new Scanner(new File(fileName));
		this.fileName = fileName;

		if (scanner.hasNext())
		{
			String first = scanner.nextLine();
			sequenceLength = first.length();

			if (!DNAUtil.isValidDNAString(first, sequenceLength))
			{
				scanner.close();
				throw new IllegalArgumentException("Query file contains invalid DNA sequences");
			}

		}

		scanner.close();
	}

	/**
	 * @return the length of the sequence
	 */
	public int getSequenceLength()
	{
		return sequenceLength;
	}

	@Override
	public Iterator<String> iterator()
	{
		return new QueryFileIterator();
	}

	private class QueryFileIterator implements Iterator<String>
	{
		// fields
		private Scanner scan;

		// constructor
		public QueryFileIterator()
		{
			try
			{
				scan = new Scanner(new File(fileName));
			}
			catch (FileNotFoundException e)
			{
				System.err.println("Unable to find query file: " + fileName);
			}
		}

		@Override
		public boolean hasNext()
		{
			if (scan == null)
				return false;

			if (!scan.hasNextLine())
			{
				scan.close();
				scan = null;
				return false;
			}

			return true;
		}

		@Override
		public String next()
		{
			if (hasNext())
			{
				return scan.nextLine();
			}
			else
				throw new NoSuchElementException();
		}

	}
}
