/**
 * Offers static methods for converting and confirming DNA sequences
 */
public class DNAUtil
{
	// char index in string is also 2 bit binary representation of chemical
	private static final String validChars = "ACGT";

	/**
	 * checks if a DNA string is valid based on passed sequence length and ACTG
	 * chemical codes (not case sensitive)
	 * 
	 * @param dna
	 *            the sequence to be checked
	 * @param sequenceLength
	 *            the required length of the sequence
	 * @return false if sequence contains invalid characters or dna.length !=
	 *         sequenceLength, true otherwise
	 * @throws IllegalArgumentException
	 *             if 0 < sequence length < 32 is not satisfied
	 */
	public static boolean isValidDNAString(String dna, int sequenceLength)
	{
		checkSequenceLength(sequenceLength);

		if (dna.length() != sequenceLength)
			return false;

		// handle improper casing
		dna = dna.toUpperCase();

		// check if the dna sequence does contain invalid
		boolean isValid = true;
		for (char c : dna.toCharArray())
		{
			if (validChars.indexOf(c) == -1)
				isValid = false;
		}

		return isValid;
	}

	/**
	 * Converts a DNA String to a long format. Checks validity of string
	 * 
	 * @param dna
	 *            the DNA string to be converted
	 * @param sequenceLength
	 *            the length of the DNA sequence
	 * @return a long representation of the passed DNA string
	 */
	public static long convertStringToLong(String dna, int sequenceLength)
	{
		dna = dna.toUpperCase();

		long retVal = 0;

		for (int i = 0; i < sequenceLength - 1; ++i)
		{
			char c = dna.charAt(i);
			if (validChars.indexOf(c) == -1)
			{
				throw new IllegalArgumentException("String length must match sequenceLength");
			}
			retVal += validChars.indexOf(c);
			retVal <<= 2;
		}
		retVal += validChars.indexOf(dna.charAt(sequenceLength - 1));

		return retVal;
	}

	/**
	 * converts a long to the corresponding String representation
	 * 
	 * @param dna
	 *            the DNA long to be converted
	 * @param sequenceLength
	 *            the length of the DNA sequence
	 * @return a String representing the DNA sequence
	 * @throws IllegalArgumentException
	 *             if 0 < sequence length < 32 is not satisfied
	 */
	public static String convertLongToString(long dna, int sequenceLength)
	{
		checkSequenceLength(sequenceLength);
		StringBuilder dnaString = new StringBuilder(sequenceLength);

		for (int i = sequenceLength - 1; i >= 0; --i)
		{
			int index = (int) ((dna >>> i * 2) & 3);

			dnaString.append(validChars.charAt(index));
		}

		return dnaString.toString();
	}

	/**
	 * Checks to make sure that the sequence length is in the bounds 0 < sl < 32
	 * 
	 * @param sequenceLength
	 *            the length of the DNA sequence
	 * @throws IllegalArgumentException
	 *             if 0 < sequence length < 32 is not satisfied
	 */
	private static void checkSequenceLength(int sequenceLength)
	{
		if (sequenceLength > 31 || sequenceLength < 0)
			throw new IllegalArgumentException("Error: must have 0 < SequenceLength < 32");
	}

}
