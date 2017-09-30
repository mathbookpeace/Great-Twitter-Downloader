import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mathbookpeace on 2017/9/26.
 */
public class DuplicatedImageChecker
{
	private static DuplicatedImageChecker instance = null;

	private HashMap <Long , List<File>> imageList;
	private final Object listLock;


	private DuplicatedImageChecker()
	{
		imageList = new HashMap();
		listLock = new Object();
	}

	static synchronized public DuplicatedImageChecker getInstance()
	{
		if(instance == null)
			instance = new DuplicatedImageChecker();
		return instance;
	}


	public boolean isExistImage(File file , byte prefix)
	{
		long binaryCode = prefix;
		binaryCode = (binaryCode << 56) | getBinaryCode(file);

		synchronized (listLock)
		{
			if (imageList.containsKey(binaryCode))
			{
				for (File existFile : imageList.get(binaryCode))
					if (compareImagesBinary(existFile, file))
						return true;

				imageList.get(binaryCode).add(file);
				return false;
			}
			else
			{
				imageList.put(binaryCode, new ArrayList());
				imageList.get(binaryCode).add(file);
				return false;
			}
		}
	}


	private boolean compareImagesBinary(File file1, File file2)
	{
		try
		{
			BufferedInputStream bufferedReader1 = new BufferedInputStream(new FileInputStream(file1));
			BufferedInputStream bufferedReader2 = new BufferedInputStream(new FileInputStream(file2));

			while (true)
			{
				int binary1 = bufferedReader1.read();
				int binary2 = bufferedReader2.read();

				if(binary1 != binary2)
					return false;
				if(binary1 == -1)
					break;
			}

			bufferedReader1.close();
			bufferedReader2.close();
		}
		catch (IOException e) { e.printStackTrace(); }

		return true;
	}

	private long getBinaryCode(File file)
	{
		if(!file.exists())
			return -1;

		try
		{
			long binary , result = 0;
			int binaryPosition = 0;

			BufferedInputStream bufferedReader = new BufferedInputStream(new FileInputStream(file));

			while ( (binary = bufferedReader.read()) != -1)
				result ^= binary << (binaryPosition++ % 7) * 8;

			bufferedReader.close();
			return result;

		}
		catch (IOException e) { e.printStackTrace(); }

		return -1;
	}
}
