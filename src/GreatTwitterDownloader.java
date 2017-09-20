import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.lang3.SystemUtils;

/**
 * Created by mathbookpeace on 2017/9/13.
 */


public class GreatTwitterDownloader
{
	private DownloadQueue downloadQueue;
	private DateCounter dateCounter;
	private SimpleDoubleProperty simpleDoubleProperty;

	public GreatTwitterDownloader()
	{
		if(SystemUtils.IS_OS_MAC)
			System.setProperty("webdriver.chrome.driver" , "chromedriver");
		else if(SystemUtils.IS_OS_WINDOWS)
			System.setProperty("webdriver.chrome.driver" , "chromedriver.exe");
		else
			return;

		downloadQueue  = new DownloadQueue();

		DownloadThreadManager downloadThreadManager = new DownloadThreadManager(downloadQueue);
		downloadThreadManager.setDaemon(true);
		downloadThreadManager.start();

		dateCounter = new DateCounter();
	}


	public void SetSimpleDoubleProperty(SimpleDoubleProperty simpleDoubleProperty)
	{
		dateCounter.SetSimpleDoubleProperty(simpleDoubleProperty);
	}


	public void DownloaderWithKeyword(String searchKeyword)
	{
		DownloadWithKeywordThread downloadWithKeywordThread = new DownloadWithKeywordThread(searchKeyword , downloadQueue , dateCounter);
		downloadWithKeywordThread.setDaemon(true);
		downloadWithKeywordThread.start();
	}



}
