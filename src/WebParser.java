/**
 * Created by mathbookpeace on 2017/9/20.
 */

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

class WebParser extends Thread
{

	private static DateCounter dateCounter;
	private DownloadQueue downloadQueue;
	private TwitterUrlQueue twitterUrlQueue;

	private WebDriver webDriver;

//----------------------------------------------------------------------------------------------------------------------------------------

	public WebParser()
	{
		downloadQueue = DownloadQueue.getInstance();
		dateCounter = DateCounter.getInstance();
		twitterUrlQueue = TwitterUrlQueue.getInstance();

		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--headless");

		// neet to set header
		webDriver = new ChromeDriver(chromeOptions);
		webDriver.get("https://twitter.com/search-home");
		for (Cookie cookie : LoginTwitter.twitterCookie)
			webDriver.manage().addCookie(cookie);
	}


	public void run()
	{
		try
		{
			while(GreatTwitterDownloader.isActive)
			{
				int currentSize , lastSize = 0 , currentPageNumber = 1;

				ImageInfo imageInfo;
				while ( (imageInfo = twitterUrlQueue.poll()) == null)
				{
					if (GreatTwitterDownloader.isActive)
						Thread.sleep(5000);
					else
					{
						System.out.println("Download completed !");
						webDriver.quit();
						return;
					}
				}

				webDriver.get("https://twitter.com/search-home");
				while (webDriver.findElements(By.id("search-home-input")).size() == 0);
				webDriver.findElement(By.id("search-home-input")).sendKeys(imageInfo.url + Keys.ENTER);


				boolean isFoundTimeline;
				while (true)
				{
					if(webDriver.findElements(By.id("timeline")).size() != 0)
					{
						isFoundTimeline = true;
						break;
					}

					if (webDriver.findElements(By.className("SearchEmptyTimeline")).size() != 0)
					{
						isFoundTimeline = false;
						dateCounter.increaseCompletedDateBy1();
						break;
					}
					Thread.sleep(2000);
				}

				if(!isFoundTimeline)
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

				dateCounter.increaseCompletedDateBy1();
			}

			System.out.println("Download completed !");
			webDriver.quit();
		}
		catch (InterruptedException e) { e.printStackTrace(); }
	}
}
