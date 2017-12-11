import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 
 * @author Madeline Ross 
 * @author Martin Vail 
 *
 */
class GeneBankCreateBTree
{

	private static int tDegree = 0; // DEGREE of the Tree
	private static int kSequenceLength = 0; // SEQUENCE LENGTH
	private static int cacheSize = 0;
	private static String gbkFileName;
	private static String bTreeFileName = "";
	private static boolean useCache;
	private static int debugLevel;

	public static void main(String[] args)
	{
		parseArgs(args);

		// instantiate a GeneBankFile
		GeneBankFile gbf = null;
		try
		{
			gbf = new GeneBankFile(gbkFileName, kSequenceLength);
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Unable to open file " + gbkFileName);
			printUsage();
		}

		// remove path
		String[] gbkName = gbkFileName.split("/");
		gbkFileName = gbkName[gbkName.length - 1];

		bTreeFileName += gbkFileName + ".btree.data." + kSequenceLength + "." + tDegree;

		// create empty BTree
		BTree bt = null;
		try
		{
			BTreeFile btf = BTreeFile.createNewBTreeFile(bTreeFileName, BTree.METADATA_BYTE_SIZE,
					BTreeNode.getByteSize(tDegree));
			if (useCache)
			{
				bt = new BTree(btf, kSequenceLength, tDegree, cacheSize); //
			}
			else
			{
				bt = new BTree(btf, kSequenceLength, tDegree); //
			}
		}
		catch (IOException e)
		{
			System.err.println("Critical Error while creating BTree file " + bTreeFileName);
			printUsage();
		}

		// iterate through GBK file and add to BTree
		int sequenceCount = 0;
		while (gbf.hasNextDataBlock())
		{
			sequenceCount++;
			int count = 0;
			System.err.println("\nStarting data block " + sequenceCount + " in " + gbkFileName);
			while (gbf.hasNextDNA())
			{
				long next = gbf.nextDNAasLong();
				bt.add(next);
				count++;
				if (count % 500 == 0)
				{
					System.err.println("Added " + count + " sequences from " + gbkFileName);
				}
			}

			System.err.println("\nData block " + sequenceCount + " contained " + count + " sequences");
		}

		if (sequenceCount == 0)
		{ // there were no sequences in the file
			System.err.println(gbkFileName + " contained no DNA sequences!");
			bt.close();
			File f = new File(bTreeFileName);
			if (f.exists())
			{
				f.delete();
			}
			System.exit(1);
		}

		// if debug 1, print dump file
		if (debugLevel == 1)
		{
			System.err.println("\nCreating debug dump file");
			bt.dumpInOrderToFile("dump");
			System.err.println("");
		}

		bt.close();
	}

	public static void parseArgs(String[] args)
	{
		// check for min # of args
		if (args.length < 4)
		{
			System.err.println("Incorrect number of args");
			printUsage();
		}

		// handle cache choice
		try
		{
			int cacheLevel = Integer.parseInt(args[0]);
			if (cacheLevel == 0)
			{
				useCache = false;
			}
			else if (cacheLevel == 1)
			{
				useCache = true;
			}
			else
			{
				throw new Exception();
			}
		}
		catch (Exception e)
		{
			System.err.println("First arg must be 0 or 1!");
			printUsage();
		}

		// handle degree
		try
		{
			tDegree = Integer.parseInt(args[1]);
			if (tDegree == 0)
			{

				// brute force love
				int blockSize = 4096;
				tDegree = 2;
				while (BTreeNode.getByteSize(tDegree + 1) < blockSize)
					tDegree++;
			}
			else if (tDegree <= 1)
				throw new Exception();

		}
		catch (Exception e)
		{
			System.err.println("Degree must be an integer t such that, 1 < t. Choose 0 to determine optimal degree");
			printUsage();
		}

		// get gbk file, file not exist failure is handled later in main
		gbkFileName = args[2];

		// get sequence length
		try
		{
			kSequenceLength = Integer.parseInt(args[3]);
			if (kSequenceLength <= 0 || kSequenceLength > 31)
			{
				throw new Exception();
			}

		}
		catch (Exception e)
		{
			System.err.println("Sequence length must be an integer k such that, 0 < k < 32");
			printUsage();
		}

		// get cache size
		if (useCache)
		{
			try
			{
				cacheSize = Integer.parseInt(args[4]);
				if (cacheSize <= 0)
					throw new IllegalArgumentException();
			}
			catch (IndexOutOfBoundsException e)
			{
				System.err.println("5th argument must be the cache size if running program with cache!");
				printUsage();
			}
			catch (NumberFormatException e)
			{
				System.err.println("Cache size must be an Integer!");
				printUsage();
			}
			catch (IllegalArgumentException e)
			{
				System.err.println("Cache size must be greater than zero");
				printUsage();
			}
		}

		// get debug level
		if (useCache)
		{
			getDebugLevel(5, args);
		}
		else
		{
			getDebugLevel(4, args);
		}

	}

	/**
	 * Assigns the proper debug level to the debugLevel static field
	 * 
	 * @param index
	 *            index of the arguments in the args array
	 * @param args
	 *            the command line arguments
	 */
	private static void getDebugLevel(int index, String[] args)
	{
		if (args.length > index + 1)
		{
			System.err.println("Too many arguments!");
			printUsage();
		}
		else if (args.length == index + 1)
		{ // debug level is specified
			try
			{
				debugLevel = Integer.parseInt(args[index]);
				if (!(debugLevel == 0 || debugLevel == 1))
					throw new Exception();
			}
			catch (Exception e)
			{
				System.err.println("Debug level must be either 0 or 1");
				printUsage();
			}
		}

	}

	/**
	 * Prints correct program usage
	 */
	public static void printUsage()
	{
		System.err.println(
				"Usage: java GeneBankCreateBTree <0/1(no/with Cache)> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]");
		System.exit(-1);
	}

}
