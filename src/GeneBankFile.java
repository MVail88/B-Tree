import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;

/**
 * This class parses a Gene Bank File(gbk) file. Find and return the next valid
 * gene sequence(moving window).
 *
 * The code in this class could definitely be improved
 * @author Martin Vail
 */
public class GeneBankFile
{
	// fields
	private PushbackReader file;
	private int sequenceLength;
	private boolean inSequence;
	private StringBuffer sb;
	private Window window;
	private boolean NFound;

	// constructors

	/**
	 * opens fileName as GeneBankFile for parsing with specified sequenceLength
	 * 
	 * @param fileName
	 *            the name of the file to be opened
	 * @param sequenceLength
	 *            the DNA sequence length 0 < length < 32
	 * @throws FileNotFoundException
	 *             if file was not found
	 * @throws IllegalArgumentException
	 *             if invalid sequence length, 0 < length < 32
	 */
	public GeneBankFile(String fileName, int sequenceLength) throws FileNotFoundException
	{
		if (sequenceLength < 0 || sequenceLength > 31)
			throw new IllegalArgumentException("Invalid sequence length!");

		this.sequenceLength = sequenceLength;
		inSequence = false;
		window = new Window(sequenceLength);
		
		NFound = false;

		file = new PushbackReader(new BufferedReader(new FileReader(fileName)));
	}

	// public methods

	/**
	 * Searches file for next sequence and prepares for returning DNA strings. Can
	 * only be called when current sequence is finished (eg, hasNextDNA = false)
	 * 
	 * @return true if another sequence is available, false otherwise
	 * @throws IllegalStateException
	 *             if current sequence is not finished
	 */
	public boolean hasNextDataBlock()
	{
		if (hasNextDNA())
		{
			throw new IllegalStateException("Current DNA data block is not finished");
		}

		try
		{
			boolean foundStart = false;
			char charToAdd = (char) file.read();
			String line = "";

			while (charToAdd != (char) -1 && !foundStart)
			{
				if (line != null && line.contains("ORIGIN"))
				{
					foundStart = true;
				}
				else if (NFound && (charToAdd == 'a' || charToAdd == 'c' || charToAdd == 'g' || charToAdd == 't' || charToAdd == 'A' || charToAdd == 'C' || charToAdd == 'G' || charToAdd == 'T'))
				{
					file.unread(charToAdd);
					foundStart = true;
				}
				else
				{
					line = line + charToAdd; // readLine() returns null if the end of the stream is reached
					charToAdd = (char) file.read();
				}
			}

			inSequence = foundStart;

			if (inSequence)
			{
				sb = fillBufferWithSequence();
				window = new Window(sequenceLength);
			}
			else
			{ // there are no more data blocks
				file.close();
			}

			return foundStart;

		}
		catch (IOException e)
		{
			System.err.println("Unable to read gbk file");
		}

		return false;
	}

	/**
	 * @return true if another DNA string is available, false otherwise
	 */
	public boolean hasNextDNA()
	{
		if (!inSequence)
			return false;

		boolean atEnd = false;
		boolean found = false;
		while (!atEnd && !found)
		{
			String cur = window.get();

			if (DNAUtil.isValidDNAString(cur, sequenceLength))
			{
				found = true;
			}
			else if (!window.canShift())
			{
				atEnd = true;
				inSequence = false;
			}
			else
			{
				window.shift();
			}
		}

		return found;
	}

	/**
	 * @return the next DNA string from the current data block
	 * @throws IllegalStateException
	 *             if there is no next DNA String
	 */
	public String nextDNAString()
	{
		if (!hasNextDNA())
			throw new IllegalStateException("There are no more DNA sequence in the data block");

		String retVal = window.get();

		if (window.canShift())
		{
			window.shift();
		}
		else
		{
			inSequence = false;
		}

		return retVal;
	}

	/**
	 * @return the next DNA String from the current data block as a long
	 * @throws IllegalStateException
	 *             if there is no next DNA String
	 */
	public long nextDNAasLong()
	{
		return DNAUtil.convertStringToLong(nextDNAString(), sequenceLength);
	}

	// private methods

	/**
	 * returns a string of either the chemicals, or //
	 * 
	 * @return returns a sanitized line of DNA from file
	 */
	private String nextDNALine()
	{
		try
		{
			char charToAdd = (char) file.read();
			String retVal = "";
			boolean foundEnd = false;

			while (retVal != null && !foundEnd)
			{
				if (retVal != null &&  (charToAdd == 'n' || charToAdd == 'N'))
				{
					foundEnd = true;
					NFound = true;
				}
				else if (retVal.length() > 0 && (retVal.charAt(retVal.length() - 1) == '/' && charToAdd == '/'))
				{
					foundEnd = true;
					NFound = false;
					retVal = retVal.substring(0, retVal.length() - 1);
				}
				else
				{
					retVal = retVal + charToAdd;
					charToAdd = (char) file.read();
				}
			}

			retVal = retVal.replaceAll("\\s+", "");
			retVal = retVal.replaceAll("\\d+", "");

			return retVal;
		}
		catch (IOException e)
		{
			System.err.println("Unable to read from gbk file");
		}

		return null;
	}

	/**
	 * fills a StringBuffer with the full sequence
	 * 
	 * @return the filled StringBuffer
	 */
	private StringBuffer fillBufferWithSequence()
	{
		StringBuffer buff = new StringBuffer(500);

		buff.append(nextDNALine());

		return buff;
	}

	/**
	 * Handles the window movement
	 */
	private class Window
	{
		int start;// inclusive
		int end; // exclusive

		Window(int sequenceLength)
		{
			start = 0;
			end = sequenceLength;
		}

		boolean canShift()
		{
			return end < sb.length();
		}

		void shift()
		{
			if (end > sb.length())
			{
				throw new IllegalStateException("Window is at end of sequence");
			}

			start++;
			end++;
		}

		String get()
		{
			return sb.substring(start, end);
		}

	}

}
