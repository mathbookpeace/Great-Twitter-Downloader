/**
 * Created by mathbookpeace on 2017/9/20.
 */

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DownloadWithKeywordThread extends Thread
{
	private String searchKeyword;
	private DownloadQueue downloadQueue;
	DateCounter dateCounter;
	private double dateLimit = 1000;

//----------------------------------------------------------------------------------------------------------------------------------------

	public DownloadWithKeywordThread(String searchKeyword , DownloadQueue downloadQueue , DateCounter dateCounter)
	{
		this.searchKeyword = searchKeyword;
		this.downloadQueue = downloadQueue;
		this.dateCounter = dateCounter;
	}


	public void run()
	{
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-d");
		SimpleDateFormat simpleDateFormatFilename = new SimpleDateFormat("yyyy-MM-dd");

		File folder = new File(searchKeyword.replace("\\/><:\"|?*" , "A"));
		if(!folder.exists())
			folder.mkdir();


		try
		{
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.addArguments("--headless");
			WebDriver webDriver = new ChromeDriver(chromeOptions);

			String searchURLBase = "https://twitter.com/search?f=images&vertical=default&q=";
			dateCounter.addTotalDate(dateLimit);

			for(int i = 0; i < dateLimit; ++i)
			{
				System.out.println(simpleDateFormatFilename.format(calendar.getTime()));

				int currentSize , lastSize = 0;
				int trueTotalSize;
				int currentPageNumber = 1;
				String untilDate;

				String searchURL;

				untilDate = simpleDateFormatFilename.format(calendar.getTime());
				searchURL = " until:" + simpleDateFormat.format(calendar.getTime());
				calendar.add(Calendar.DATE , -1);
				searchURL = " since:" + simpleDateFormat.format(calendar.getTime()) + searchURL;

				searchURL = searchKeyword + searchURL;
				searchURL = searchURLBase + URLEncoder.encode(searchURL , "UTF-8");

				webDriver.get(searchURL);
				List<WebElement> searchResult = webDriver.findElements(By.className("stream"));

				if(searchResult.size() == 0)
				{
					dateCounter.increaseCompletedDateBy1();
					continue;
				}

				while (lastSize < 1000)
				{
					((JavascriptExecutor) webDriver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
					Thread.sleep(100);

					List<WebElement> imageElementList = searchResult.get(0).findElements(By.tagName("img"));
					currentSize = imageElementList.size();

					trueTotalSize = searchResult.get(0).findElements(By.className("AdaptiveStreamGridImage")).size();

					if (currentSize > lastSize)
					{
						for (int currentDownloadIndex = lastSize; currentDownloadIndex < currentSize; ++currentDownloadIndex)
						{
							String imageUrl = imageElementList.get(currentDownloadIndex).getAttribute("src");
							String imageFilename = searchKeyword + "/" + untilDate + "_" + StringUtils.leftPad("" + currentPageNumber++, 2, "0");
							downloadQueue.pushToQueue(new ImageInfo(imageUrl , imageFilename));
						}

						lastSize = currentSize;
					}
					else if (trueTotalSize <= lastSize)
					{
						Thread.sleep(1000);
						if(!(searchResult.get(0).findElements(By.className("AdaptiveStreamGridImage")).size() > trueTotalSize))
						{
							System.out.println("Parse End");
							break;
						}
					}
				}

				dateCounter.increaseCompletedDateBy1();
			}

			System.out.println("Download completed !");
			webDriver.quit();
		}
		catch (InterruptedException e) { e.printStackTrace(); }
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }
	}
}
