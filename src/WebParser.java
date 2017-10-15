/**
 * Created by mathbookpeace on 2017/9/20.
 */

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

class WebParser extends Thread
{

	private static DateCounter dateCounter = DateCounter.getInstance();;
	private static DownloadQueue downloadQueue = DownloadQueue.getInstance();
	private static TwitterUrlQueue twitterUrlQueue = TwitterUrlQueue.getInstance();

	private WebDriver webDriver;
	private static Object waitKey = new Object();

//----------------------------------------------------------------------------------------------------------------------------------------

	public WebParser()
	{
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--headless");

		// need to set header
		webDriver = new ChromeDriver(chromeOptions);
		webDriver.get("https://twitter.com/search-home");
		for (Cookie cookie : LoginTwitter.twitterCookie)
			webDriver.manage().addCookie(cookie);
	}


	private void waitForTask() throws InterruptedException
	{
		synchronized (waitKey) { waitKey.wait(); }
	}

	public static void notifyAllForTask()
	{
		synchronized (waitKey) { waitKey.notifyAll(); }
	}



	//-------------------------------------------------------------------------------------------------------------------

	public void run()
	{
		try
		{
			WebDriverWait waitForLogin = new WebDriverWait(webDriver , 60000);

			while(GreatTwitterDownloader.isActive)
			{
				int currentSize , lastSize = 0 , currentPageNumber = 1;

				ImageInfo imageInfo = null;
				while (GreatTwitterDownloader.isActive && (imageInfo = twitterUrlQueue.poll()) == null)
					waitForTask();
				if (!GreatTwitterDownloader.isActive)
					break;

				dateCounter.increaseCompletedDateBy1();

				webDriver.get("https://twitter.com/search-home");
				while (webDriver.findElements(By.id("search-home-input")).size() == 0);
				webDriver.findElement(By.id("search-home-input")).sendKeys(imageInfo.url + Keys.ENTER);

				waitForLogin.until(ExpectedConditions.presenceOfElementLocated(By.className("AppContainer")));

				if (webDriver.findElements(By.className("SearchEmptyTimeline")).size() != 0)
				{
					dateCounter.increaseCompletedDateBy1();
					continue;
				}


				List<WebElement> searchResult = webDriver.findElements(By.className("stream"));

				while (lastSize < 1000)
				{
					((JavascriptExecutor) webDriver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
					Thread.sleep(2000);

					List<WebElement> imageElementList = searchResult.get(0).findElements(By.className("AdaptiveMedia-photoContainer"));
					currentSize = imageElementList.size();

					if (currentSize > lastSize)
					{
						for (int currentDownloadIndex = lastSize; currentDownloadIndex < currentSize; ++currentDownloadIndex)
						{
							String imageUrl = imageElementList.get(currentDownloadIndex).getAttribute("data-image-url");
							String imageFilename = imageInfo.filename + "_" + StringUtils.leftPad("" + currentPageNumber++, 2, "0");

							downloadQueue.add(new ImageInfo(imageUrl , imageFilename , imageInfo.parserIndex));
							DownloadThread.notifyForTask();
						}

						lastSize = currentSize;
					}
					else
					{
						Thread.sleep(4000);
						if(searchResult.get(0).findElements(By.className("AdaptiveMedia-photoContainer")).size() <= lastSize)
							break;
					}
				}
			}

			System.out.println("Download completed !");
			webDriver.quit();
		}
		catch (InterruptedException e) { e.printStackTrace(); }
	}
}
