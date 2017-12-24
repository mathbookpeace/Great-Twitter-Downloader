/**
 * Created by mathbookpeace on 2017/9/20.
 */

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.List;

class WebParser extends Thread
{

	private static DownloadQueue downloadQueue = DownloadQueue.getInstance();
	private static TwitterUrlQueue twitterUrlQueue = TwitterUrlQueue.getInstance();

	private static Object waitKey = new Object();

//----------------------------------------------------------------------------------------------------------------------------------------

	public WebParser()
	{
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
		while(GreatTwitterDownloader.isActive)
		{
			int currentPageNumber = 1;
			boolean toBeContinue = false;

			ImageInfo imageInfo = null;
			while (GreatTwitterDownloader.isActive && (imageInfo = twitterUrlQueue.poll()) == null)
				try { waitForTask(); }    catch (InterruptedException e) { e.printStackTrace(); }
			if (!GreatTwitterDownloader.isActive)
				break;

			System.out.println(imageInfo.keywordUTF8);

			String firstConnectResult = null;
			while(true)
			{
				try
				{
					firstConnectResult = Jsoup.connect(imageInfo.url)
							.header("X-Push-State-Request" , "true")
							.header("X-Asset-Version" , "ec23cf")
							.header("X-Twitter-Active-Use" , "yes")
							.header("X-Requested-With" , "XMLHttpRequest")
							.header("Connection" , "keep-alive")
							.get().text();
					break;
				}
				catch (IOException e)
				{
					System.out.println("Jsoup connect error 60: " + imageInfo.url);
					try { Thread.sleep(5000); }    catch (InterruptedException e1) { e1.printStackTrace(); }
				}
			}


			TwitterJsonParser firstImageUrlParser = new TwitterJsonParser(firstConnectResult , "data-image-url");

			String firstImageUrl;
			while ( (firstImageUrl = firstImageUrlParser.getNext()) != null )
			{
				toBeContinue = true;

				firstImageUrl = firstImageUrl.replaceAll("\\\\", "");
				String firstImageFilename = imageInfo.filename + "_" + StringUtils.leftPad("" + currentPageNumber++, 2, "0");

				downloadQueue.add(new ImageInfo(firstImageUrl, firstImageFilename, imageInfo.parserIndex));
				DownloadThread.notifyForTask();
			}


			TwitterJsonParser firstMinPositionParser = new TwitterJsonParser(firstConnectResult , "data-max-position");
			String currentMinPosition = firstMinPositionParser.getNext();

			if (currentMinPosition == null)
				toBeContinue = false;
			else
				currentMinPosition = currentMinPosition.replaceAll("\\\\" , "");


			while (toBeContinue)
			{
				toBeContinue = false;

				String timelineUrlString = "https://twitter.com/i/search/timeline?vertical=default&q="
						+ imageInfo.keywordUTF8
						+ "&src=typd&include_available_features=1&include_entities=1&max_position="
						+ currentMinPosition;

				String connectResult = null;

				while (true)
				{
					try
					{
						connectResult = Jsoup.connect(timelineUrlString)
								.header("X-Twitter-Active-User", "yes")
								.header("X-Requested-With", "XMLHttpRequest")
								.ignoreContentType(true).get().text();
						break;
					}
					catch (IOException e)
					{
						System.out.println("Jsoup connect error 107: " + timelineUrlString);
						try { Thread.sleep(5000); } catch (InterruptedException e1) { e1.printStackTrace(); }
					}
				}


				TwitterJsonParser minPositionParser = new TwitterJsonParser(connectResult , "min_position");
				currentMinPosition = minPositionParser.getNext();


				TwitterJsonParser imageUrlParser = new TwitterJsonParser(connectResult , "data-image-url");

				String imageUrl;
				while ( (imageUrl = imageUrlParser.getNext()) != null )
				{
					toBeContinue = true;

					imageUrl = imageUrl.replaceAll("\\\\", "");
					String imageFilename = imageInfo.filename + "_" + StringUtils.leftPad("" + currentPageNumber++, 2, "0");

					downloadQueue.add(new ImageInfo(imageUrl, imageFilename, imageInfo.parserIndex));
					DownloadThread.notifyForTask();
				}
			}
		}

		System.out.println("Download completed !");
	}
}



class TwitterJsonParser
{
	private String json = "";
	private String keyword = "";
	private int currentIndex = 0;


	public TwitterJsonParser(String json , String keyword)
	{
		this.json = json;
		this.keyword = keyword;
	}

	public String getNext()
	{
		int data_maxIndex = json.indexOf(keyword , currentIndex);

		if(data_maxIndex ==  -1)
			return null;

		int data_maxIndexStart = json.indexOf("\"" , data_maxIndex + keyword.length() + 1) + 1;
		int data_maxIndexEnd = json.indexOf("\"" , data_maxIndexStart);

		currentIndex = data_maxIndexEnd + 1;

		return json.substring(data_maxIndexStart , data_maxIndexEnd);
	}
}