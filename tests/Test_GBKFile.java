import java.io.IOException;

/**
 * This Class isn't so much of a test as it is example usage code. It parses
 * each gbk file and doesn't use any of the strings. rather, it only counts how
 * many data blocks are in each file
 */
public class Test_GBKFile
{
	public static void main(String[] args) throws IOException
	{
		String[] files = { "res/test1.gbk", "res/test2.gbk", "res/test3.gbk", "res/test4.gbk", "res/test5.gbk" };

		for (String file : files)
		{
			GeneBankFile gbk = new GeneBankFile(file, 7);

			int count = 0;

			while (gbk.hasNextDataBlock())
			{
				count++;

				int i = 0;
				while (gbk.hasNextDNA())
				{
					if (i == 5021)
						i++;

					gbk.nextDNAString();
					// System.out.println(i + " " + gbk.nextDNAString());
					++i;
				}
			}

			System.out.println(file + " has " + count + " sequences");
		}

	}
}
