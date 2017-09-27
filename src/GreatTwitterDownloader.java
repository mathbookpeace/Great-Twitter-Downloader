import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.lang3.SystemUtils;

import java.time.LocalDate;

/**
 * Created by mathbookpeace on 2017/9/13.
 */


public class GreatTwitterDownloader
{
	public static boolean isActive = true;

	public GreatTwitterDownloader()
	{
		if(SystemUtils.IS_OS_MAC)
			System.setProperty("webdriver.chrome.driver" , "chromedriver");
		else if(SystemUtils.IS_OS_WINDOWS)
			System.setProperty("webdriver.chrome.driver" , "chromedriver.exe");
		else
			return;

		// one download queue and one manager to handle the download request from web parser.
		DownloadThreadManager downloadThreadManager = new DownloadThreadManager();
		downloadThreadManager.start();
	}


	public void setSimpleDoubleProperty(SimpleDoubleProperty simpleDoubleProperty)
	{
		DownloadWithKeywordParser.dateCounter.setSimpleDoubleProperty(simpleDoubleProperty);
	}


	public void dispose()
	{
		isActive = false;
	}


	public void downloaderWithKeyword(String searchKeyword , LocalDate sinceDate , LocalDate untilDate)
	{
		if(untilDate == null)
			untilDate = LocalDate.now();
		untilDate.plusDays(1);

		if(sinceDate == null)
			sinceDate = untilDate.plusDays(-1);
		if(!untilDate.isAfter(sinceDate))
			return;

		// parse the twitter website and put the images data into download queue.
		DownloadWithKeywordParser downloadWithKeywordParser = new DownloadWithKeywordParser(searchKeyword , sinceDate , untilDate);
		downloadWithKeywordParser.start();
	}



}
