import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.lang3.SystemUtils;

import java.time.LocalDate;

/**
 * Created by mathbookpeace on 2017/9/13.
 */


public class GreatTwitterDownloader
{
	private DownloadQueue downloadQueue;
	private DateCounter dateCounter;
	static public boolean isActive = true;

	public GreatTwitterDownloader()
	{
		if(SystemUtils.IS_OS_MAC)
			System.setProperty("webdriver.chrome.driver" , "chromedriver");
		else if(SystemUtils.IS_OS_WINDOWS)
			System.setProperty("webdriver.chrome.driver" , "chromedriver.exe");
		else
			return;

		// one download queue and one manager to handle the download request from web parser.
		downloadQueue  = new DownloadQueue();


		DownloadThreadManager downloadThreadManager = new DownloadThreadManager(downloadQueue);
		downloadThreadManager.start();

		dateCounter = new DateCounter();
	}


	public void SetSimpleDoubleProperty(SimpleDoubleProperty simpleDoubleProperty)
	{
		dateCounter.SetSimpleDoubleProperty(simpleDoubleProperty);
	}


	public void Dispose()
	{
		isActive = false;
	}


	public void DownloaderWithKeyword(String searchKeyword , LocalDate sinceDate , LocalDate untilDate)
	{
		if(untilDate == null)
			untilDate = LocalDate.now();
		if(sinceDate == null)
			sinceDate = untilDate.plusDays(-1);
		if(!untilDate.isAfter(sinceDate))
			return;

		// parse the twitter website and put the images data into download queue.
		DownloadWithKeywordParser downloadWithKeywordParser = new DownloadWithKeywordParser(searchKeyword , sinceDate , untilDate , downloadQueue , dateCounter);
		downloadWithKeywordParser.start();
	}



}
