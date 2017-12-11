import sun.misc.FloatingDecimal;

import java.util.Random;

public class Test_DNAUtil
{
	public static void main(String[] args)
	{

		// test isValidDNAString
		System.out.println("isValidDNAString Tests:\n");
		BTest.testBoolean("Case-insensitive", DNAUtil.isValidDNAString("AcTg", 4), true);
		BTest.testBoolean("Improper sequence length", DNAUtil.isValidDNAString("AcTg", 1), false);
		BTest.testBoolean("Invalid character", DNAUtil.isValidDNAString("al;ksdj", 7), false);
		BTest.testException("Sequence length < 0", IllegalArgumentException.class, () ->
		{
			DNAUtil.isValidDNAString("1", -1);
			return true;
		});
		BTest.testException("Sequence length > 31", IllegalArgumentException.class, () ->
		{
			DNAUtil.isValidDNAString("1", 32);
			return true;
		});

		// test long to String
		System.out.println("\nconvertLongToString\n");
		// all A's T's etc
		long[] longs = { 0xffffffffffffffffL, 0x0000000000000000L, 0xAAAAAAAAAAAAAAAAL, 0x5555555555555555L };
		BTest.testString("All T's", DNAUtil.convertLongToString(longs[0], 3), "TTT");
		BTest.testString("All A's", DNAUtil.convertLongToString(longs[1], 3), "AAA");
		BTest.testString("All G's", DNAUtil.convertLongToString(longs[2], 3), "GGG");
		BTest.testString("All C's", DNAUtil.convertLongToString(longs[3], 3), "CCC");
		// alternating values
		longs = new long[] { 0xCCCCCCCCCCCCCCCCL, 0x9999999999999999L };
		BTest.testString("Alternating TA", DNAUtil.convertLongToString(longs[0], 7), "ATATATA");
		BTest.testString("Alternating GC", DNAUtil.convertLongToString(longs[1], 7), "CGCGCGC");
		// full sequence
		longs = new long[] { 0x07B639B26CB6CB60L };
		BTest.testString("Full sequence", DNAUtil.convertLongToString(longs[0], 31), "ACTGTCGATGCGTAGCGTAGTCGTAGTCGAA");

		// test string to long
		System.out.println("\nconvertStringToLong\n");
		boolean hasPassed = true;
		Random rand = new Random();
		for (int i = 0; i < 100; ++i)
		{
			long testLong = rand.nextLong() & ~(3L << 31);
			String testString = DNAUtil.convertLongToString(testLong, 31);
			long result = DNAUtil.convertStringToLong(testString, 31);

			if (testLong != result)
			{
				BTest.testString("Random long test", DNAUtil.convertLongToString(result, 31), testString);
				hasPassed = false;
				break;
			}
		}

		if (hasPassed)
		{
			BTest.testBoolean("Random long test", true, true);
		}
	}

}
