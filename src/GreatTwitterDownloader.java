import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.lang3.SystemUtils;

import java.time.LocalDate;

/**
 * Created by mathbookpeace on 2017/9/13.
 */


class GreatTwitterDownloader
{
	public static boolean isActive = true;
	private boolean isFirstRequest = true;

	WebParserManager webParserManager;
	DownloadThreadManager downloadThreadManager;

	public GreatTwitterDownloader()
	{
	}


	public void setSimpleDoubleProperty(SimpleDoubleProperty simpleDoubleProperty)
	{
		DateCounter.getInstance().setSimpleDoubleProperty(simpleDoubleProperty);
	}


	public void dispose()
	{
		isActive = false;
		WebParser.notifyAllForTask();
		DownloadThread.notifyAllForTask();
	}



	public void login()
	{
		LoginTwitter loginTwitter = new LoginTwitter();
		loginTwitter.start();
	}

	public void downloaderWithKeyword(String searchKeyword , LocalDate sinceDate , LocalDate untilDate)
	{
		if(untilDate == null)
			untilDate = LocalDate.now();
		untilDate = untilDate.plusDays(1);

		if(sinceDate == null)
			sinceDate = untilDate.plusDays(-1);
		if(!untilDate.isAfter(sinceDate))
			return;


		if (isFirstRequest)
		{
			isFirstRequest = false;

			// the WebParserManager just maintain the WebParser thread pool
			webParserManager = new WebParserManager();
			webParserManager.start();

			// one download queue and one manager to handle the download request from web parser.
			downloadThreadManager = new DownloadThreadManager();
			downloadThreadManager.start();
		}

		// parse the twitter website and put the images data into download queue.
		TwitterUrlQueue twitterUrlQueue = TwitterUrlQueue.getInstance();
		twitterUrlQueue.offer(searchKeyword , sinceDate , untilDate);
	}
}
