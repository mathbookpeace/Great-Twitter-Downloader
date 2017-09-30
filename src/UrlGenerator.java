import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Created by mathbookpeace on 2017/9/28.
 */
class UrlGenerator extends Thread
{
	private static byte parserIndexStatic = 0;

	private byte parserIndex;
	private String searchKeyword;
	private LocalDate sinceDate , untilDate;

	private DateCounter dateCounter;
	private DuplicatedImageChecker duplicatedImageChecker;
	private TwitterUrlQueue twitterUrlQueue;


	public UrlGenerator (String searchKeyword , LocalDate sinceDate , LocalDate untilDate)
	{
		this.searchKeyword = searchKeyword;
		this.sinceDate = sinceDate;
		this.untilDate = untilDate;

		parserIndex = parserIndexStatic++;

		dateCounter = DateCounter.getInstance();
		duplicatedImageChecker = DuplicatedImageChecker.getInstance();
		twitterUrlQueue = TwitterUrlQueue.getInstance();

		dateCounter.addTotalDate(DAYS.between(sinceDate , untilDate));
	}


	public void run()
	{
//		try
//		{
			File folder = new File("Download");
			if(!folder.exists())
				folder.mkdir();

			folder = new File("Download/" + searchKeyword.replaceAll("[\\\\/><:\"|?*]" , "_"));
			if(!folder.exists())
				folder.mkdir();

			for (File existFile : folder.listFiles())
				if (duplicatedImageChecker.isExistImage(existFile , parserIndex))
					existFile.delete();

			LocalDate currentDate = untilDate;
			DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-M-d");
			DateTimeFormatter dateTimeFormatFilename = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			String searchURLBase = "https://twitter.com/search?q=";

			while (GreatTwitterDownloader.isActive && !currentDate.isBefore(sinceDate))
			{
				String searchURL;
				searchURL = " until:" + currentDate.format(dateTimeFormat);
				currentDate = currentDate.minusDays(1);
				searchURL = " since:" + currentDate.format(dateTimeFormat) + searchURL;
				searchURL = " filter:images" + searchURL;

				searchURL = searchKeyword + searchURL;
//				searchURL = searchURLBase + URLEncoder.encode(searchURL, "UTF-8");

				String untilDateStr = currentDate.format(dateTimeFormatFilename);

				String imageFilename = "Download/" + searchKeyword.replaceAll("[\\\\/><:\"|?*]" , "_") + "/" + untilDateStr;
				twitterUrlQueue.add(new ImageInfo(searchURL , imageFilename , parserIndex));
			}

//		}
//		catch (UnsupportedEncodingException e) { e.printStackTrace(); }
	}
}
