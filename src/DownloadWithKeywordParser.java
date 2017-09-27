/**
 * Created by mathbookpeace on 2017/9/20.
 */

import org.apache.bcel.generic.DUP;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public class DownloadWithKeywordParser extends Thread
{
	private static byte parserIndexStatic = 0;
	public static DateCounter dateCounter = new DateCounter();

	private byte parserIndex;
	private String searchKeyword;
	private DownloadQueue downloadQueue;
	private DuplicatedImageChecker duplicatedImageChecker;
	private LocalDate sinceDate , untilDate;

//----------------------------------------------------------------------------------------------------------------------------------------

	public DownloadWithKeywordParser(String searchKeyword , LocalDate sinceDate , LocalDate untilDate)
	{
		this.searchKeyword = searchKeyword;
		this.sinceDate = sinceDate;
		this.untilDate = untilDate;

		downloadQueue = DownloadQueue.getInstance();
		duplicatedImageChecker = DuplicatedImageChecker.getInstance();

		parserIndex = parserIndexStatic++;
	}


	public void run()
	{
		LocalDate currentDate = untilDate;
		DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-M-d");
		DateTimeFormatter dateTimeFormatFilename = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		File folder = new File("Download");
		if(!folder.exists())
			folder.mkdir();

		folder = new File("Download/" + searchKeyword.replaceAll("[\\\\/><:\"|?*]" , "_"));
		if(!folder.exists())
			folder.mkdir();

		for (File existFile : folder.listFiles())
			if (duplicatedImageChecker.isExistImage(existFile , parserIndex))
				existFile.delete();


		try
		{
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.addArguments("--headless");
			WebDriver webDriver = new ChromeDriver(chromeOptions);

			String searchURLBase = "https://twitter.com/search?q=";
			dateCounter.addTotalDate(DAYS.between(sinceDate , untilDate));

			while(GreatTwitterDownloader.isActive && !currentDate.isBefore(sinceDate))
			{
				System.out.println(currentDate.format(dateTimeFormatFilename));

				int currentSize , lastSize = 0;
				int currentPageNumber = 1;
				String untilDateStr;

				String searchURL;

				untilDateStr = currentDate.format(dateTimeFormatFilename);
				searchURL = " until:" + currentDate.format(dateTimeFormat);
				currentDate = currentDate.minusDays(1);
				searchURL = " since:" + currentDate.format(dateTimeFormat) + searchURL;
				searchURL = " filter:images" + searchURL;

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

					List<WebElement> imageElementList = searchResult.get(0).findElements(By.className("AdaptiveMedia-photoContainer"));
					currentSize = imageElementList.size();

					if (currentSize > lastSize)
					{
						for (int currentDownloadIndex = lastSize; currentDownloadIndex < currentSize; ++currentDownloadIndex)
						{
							String imageUrl = imageElementList.get(currentDownloadIndex).getAttribute("data-image-url");
							String imageFilename = "Download/" + searchKeyword.replaceAll("[\\\\/><:\"|?*]" , "_") + "/" + untilDateStr + "_" + StringUtils.leftPad("" + currentPageNumber++, 2, "0");

							downloadQueue.pushToQueue(new ImageInfo(imageUrl , imageFilename , parserIndex));
						}

						lastSize = currentSize;
					}
					else
					{
						Thread.sleep(2000);
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
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }
	}
}
