import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Created by mathbookpeace on 2017/9/28.
 */
class TwitterUrlQueue
{
	private static TwitterUrlQueue instance = null;

	private byte parserIndex = 0;
	private String searchKeyword;
	private LocalDate sinceDate , currentTomorrowDate;

	private DateCounter dateCounter;
	private DuplicatedImageChecker duplicatedImageChecker;

	private DateTimeFormatter dateTimeFormat;
	private DateTimeFormatter dateTimeFormatFilename;

	private Queue<RequestContent> requestQueue;
	private static final Object queueLock = new Object();


	private TwitterUrlQueue()
	{
		dateCounter = DateCounter.getInstance();
		duplicatedImageChecker = DuplicatedImageChecker.getInstance();

		requestQueue = new LinkedList();

		sinceDate = LocalDate.now();
		currentTomorrowDate = LocalDate.now().minusDays(1);

		dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-M-d");
		dateTimeFormatFilename = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	}


	public static synchronized TwitterUrlQueue getInstance ()
	{
		if (instance == null)
			instance = new TwitterUrlQueue();
		return instance;
	}


	public ImageInfo poll ()
	{
		synchronized (queueLock)
		{
			if (!currentTomorrowDate.isAfter(sinceDate) && requestQueue.size() > 0)
			{
				RequestContent nextRequest = requestQueue.poll();

				searchKeyword = nextRequest.searchKeyword;
				sinceDate = nextRequest.sinceDate;
				currentTomorrowDate = nextRequest.untilDate;

				++parserIndex;
			}

			if (currentTomorrowDate.isAfter(sinceDate))
			{
				ImageInfo currentImageInfo = getCurrentImageInfo();
				currentTomorrowDate = currentTomorrowDate.minusDays(1);
				return currentImageInfo;
			}
			else
				return null;
		}
	}


	public void offer (String searchKeyword , LocalDate sinceDate , LocalDate untilDate)
	{
		File folder = new File("Download");
		if (!folder.exists())
			folder.mkdir();

		folder = new File("Download/" + searchKeyword.replaceAll("[\\\\/><:\"|?*]", "_"));
		if (!folder.exists())
			folder.mkdir();

		for (File existFile : folder.listFiles())
			if (duplicatedImageChecker.isExistImage(existFile, parserIndex))
				existFile.delete();

		synchronized (queueLock)
		{
			requestQueue.offer(new RequestContent(searchKeyword, sinceDate, untilDate));
		}

		WebParser.notifyAllForTask();
	}


	private String getCurrentSearchUrl()
	{
		String searchURL = "";

		try
		{
			searchURL = "+until:" + currentTomorrowDate.format(dateTimeFormat);
			searchURL = "+since:" + currentTomorrowDate.minusDays(1).format(dateTimeFormat) + searchURL;
			searchURL = "+filter:images" + searchURL;

			searchURL = URLEncoder.encode(searchKeyword , "UTF-8") + searchURL;

			searchURL = "https://twitter.com/search?q=" + searchURL;
		}
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }

		return searchURL;
	}


	private String getCurrentKeywordUTF8()
	{
		try
		{
			String searchURL;
			searchURL = "+until:" + currentTomorrowDate.format(dateTimeFormat);
			searchURL = "+since:" + currentTomorrowDate.minusDays(1).format(dateTimeFormat) + searchURL;
			searchURL = "+filter:images" + searchURL;
			searchURL = URLEncoder.encode(searchKeyword , "UTF-8") + searchURL;

			return searchURL;

		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}


	private ImageInfo getCurrentImageInfo ()
	{
		String keywordUTF8 = getCurrentKeywordUTF8();
		String searchURL = getCurrentSearchUrl();
		String currentDateString = currentTomorrowDate.minusDays(1).format(dateTimeFormatFilename);
		String imageFilename = "Download/" + searchKeyword.replaceAll("[\\\\/><:\"|?*]" , "_") + "/" + currentDateString;

		return new ImageInfo(keywordUTF8 , searchURL , imageFilename , parserIndex);
	}
}
