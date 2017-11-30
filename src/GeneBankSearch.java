import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 
 * Given a query file and a BTree binary file, builds a Btree from the binary
 * and searches for instances of each line in the query file and prints out the
 * results
 * 
 * @author ravi
 */
public class GeneBankSearch
{

	private static boolean useCache;
	private static int cacheSize;
	private static String btreeFileName;
	private static String queryFileName;
	private static boolean debug;

	public static void main(String[] args) throws IOException
	{

		checkCommandLineArgs(args);

		// instantiate a QueryFile
		QueryFile qFile = null;
		try
		{
			qFile = new QueryFile(queryFileName);
		}
		catch (FileNotFoundException fnfe)
		{
			System.err.println("Unable to open/ Cannot find file: " + queryFileName);
			printUsage();
		}

		// instantiate a BTree (add constructor later)
		BTree tree = null;
		try
		{
			BTreeFile file = BTreeFile.openBTreeFile(btreeFileName, BTree.METADATA_BYTE_SIZE);
			tree = null;
			if (useCache)
			{
				tree = new BTree(file, cacheSize);
			}
			else
			{
				tree = new BTree(file);
			}
		}
		catch (IOException e)
		{
			System.err.println("Critical Error while creating BTree file: " + btreeFileName);
			printUsage();
		}

		// ensure that the sequence length matches on both
		if (qFile.getSequenceLength() != tree.getSequenceLength())
		{
			System.err.println("Non-matching sequence lengths. Exiting..");
			System.exit(-1);
		}

		// for each query in file, search BTree for query and print TreeObject into a
		// file
		// o/p file name is gbkfile_queryfilename_result
		for (String stringSearchSequence : qFile)
		{
			long longSearchSequence = DNAUtil.convertStringToLong(stringSearchSequence, stringSearchSequence.length());
			TreeObject tObj = tree.search(longSearchSequence);
			if (tObj != null)
			{
				System.out.println(stringSearchSequence.toLowerCase() + ": " + tObj.getFrequency());
			}
		}

		tree.close();

	}

	/**
	 * @param args
	 *            checks the arguments of this program usage: java GeneBankSearch
	 *            <0/1(without/with Cache)> <btree file> <query file> [<cache size>]
	 *            [<debug level>]
	 */
	private static void checkCommandLineArgs(String[] args)
	{
		if (args.length < 3 || args.length > 5)
		{
			printUsage();
		}

		/*
		 * Check if first argument is an integer --> either 0 or 1, if 1, checks that
		 * argument length is at least 4
		 */
		if (isAnInteger(args[0]))
		{
			if (Integer.parseInt(args[0]) == 0)
			{
				useCache = false;
			}
			else if (Integer.parseInt(args[0]) == 1)
			{
				useCache = true;
			}
			else
			{
				System.err.println("First argument must be 0 or 1!");
				printUsage();
			}
		}
		else
		{
			System.err.println("First argument must be 0 or 1!");
			printUsage();
		}

		// Set the BTree file name. Error handling is done in main()
		btreeFileName = args[1];

		// set the query file name. Error handling is done in main()
		queryFileName = args[2];

		/*
		 * If useCache is false, if arguments length == 3, then debug level = false(0)
		 * by default. if arguments length == 4, then argument 4 is the debug level and
		 * can only be 0.
		 * 
		 * Only default debug functionality has been implemented.
		 */
		if (useCache == false)
		{
			if (args.length == 3)
			{
				debug = false;
			}
			else if (args.length == 4)
			{
				if (Integer.parseInt(args[3]) == 0)
				{
					debug = false;
				}
				else
				{
					System.err.println("Debug level can only be 0!");
					printUsage();
				}
			}
			else
			{
				System.err.println("Debug level can only be 0!");
				printUsage();
			}
		}

		/*
		 * If useCache == true, then argument 3 must be the cache size, and argument 5
		 * if present must be the debug level
		 */
		if (useCache == true)
		{
			// if no debug level
			if (args.length == 4 && isAnInteger(args[3]))
			{
				debug = false;
				// cache cannot be -ve
				if (Integer.parseInt(args[3]) <= 0)
				{
					System.err.println("Cache size must be greater than 0!");
					printUsage();
				}
				cacheSize = Integer.parseInt(args[3]);
			}

			// if debug level is present
			// check the length and whether the last two arguments are integers
			else if (args.length == 5 && isAnInteger(args[4]) && isAnInteger(args[3]))
			{
				if (Integer.parseInt(args[4]) == 0)
				{
					debug = false;
				}
				else
				{
					System.err.println("Debug level can only be 0!");
					printUsage();
				}
				// cache cannot be -ve
				if (Integer.parseInt(args[3]) <= 0)
				{
					System.err.println("Cache size must be greater than 0!");
					printUsage();
				}
				cacheSize = Integer.parseInt(args[3]);
			}
			else
			{
				System.err.println("Debug level by default is 0 and if present can only be 0!");
				printUsage();
			}
		}
	}

	/**
	 * Prints an error message and exits
	 */
	private static void printUsage()
	{
		System.err.println("java GeneBankSearch <0/1(no/with Cache)> <btree file>"
				+ " <query file> [<cache size>] [<debug level>]");
		System.exit(-1);
	}

	/**
	 * @param stringToCheck
	 *            The String to be checked if is an integer
	 * @return true if the data in the string is an integer
	 */
	private static boolean isAnInteger(String stringToCheck)
	{
		try
		{
			Integer.parseInt(stringToCheck);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
}