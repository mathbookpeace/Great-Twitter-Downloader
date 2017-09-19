import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mathbookpeace on 2017/9/13.
 */



public class GreatTwitterDownloader
{
	public static SimpleDoubleProperty simpleDoubleProperty;

	private final Object lockForProgress;
	private final Object lockForQueue;

	private double dateLimite = 1000;
	private double totalDate = 0;
	private double completedDate = 0;

	private final int threadLimit = 40;
	private int threadCount = 0;


	public GreatTwitterDownloader()
	{
		simpleDoubleProperty = new SimpleDoubleProperty();
		lockForQueue = new Object();
		lockForProgress = new Object();
	}


	public SimpleDoubleProperty GetProgressProperty()
	{
		return simpleDoubleProperty;
	}


	public void DownloaderWithKeyword(String searchKeyword)
	{
		Queue <String> downloadQueue = new LinkedList();
		MutableBoolean threadStatus = new MutableBoolean();
		threadStatus.setTrue();

		new DownloaderWithKeywordThread(searchKeyword , downloadQueue , threadStatus).start();
		new DownloadThreadManager(searchKeyword , downloadQueue , threadStatus).start();
	}


	public void increaseCompletedDateBy1()
	{
		synchronized (lockForProgress)
		{
			++completedDate;
			simpleDoubleProperty.set( (totalDate == 0) ? 0 : (completedDate / totalDate) );
		}
	}

	public void addTotalDate(double totalDate)
	{
		synchronized (lockForProgress)
		{
			this.totalDate += totalDate;
			simpleDoubleProperty.set( (totalDate == 0) ? 0 : (completedDate / totalDate) );
		}
	}

	synchronized public void updateThreadCount(int addThreadCount)
	{
		threadCount += addThreadCount;
	}

	private String pollFromQueue(Queue <String> downloadQueue)
	{
		synchronized (lockForQueue)
		{
			return downloadQueue.poll();
		}
	}

	private void pushToQueue(String url , Queue <String> downloadQueue)
	{
		synchronized (lockForQueue)
		{
			downloadQueue.offer(url);
		}
	}


	private class DownloaderWithKeywordThread extends Thread
	{
		private String searchKeyword;
		private Queue <String> downloadQueue;
		MutableBoolean threadStatus;

		public DownloaderWithKeywordThread(String searchKeyword , Queue <String> downloadQueue , MutableBoolean threadStatus)
		{
			this.searchKeyword = searchKeyword;
			this.downloadQueue = downloadQueue;
			this.threadStatus = threadStatus;
		}


		public void run()
		{
			File folder = new File(searchKeyword);
			if(!folder.exists())
				folder.mkdir();


			if(SystemUtils.IS_OS_MAC)
				System.setProperty("webdriver.chrome.driver" , "chromedriver");
			else if(SystemUtils.IS_OS_WINDOWS)
				System.setProperty("webdriver.chrome.driver" , "chromedriver.exe");
			else
				return;


			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-d");


			try
			{
				ChromeOptions chromeOptions = new ChromeOptions();
				chromeOptions.addArguments("--headless");
				WebDriver webDriver = new ChromeDriver(chromeOptions);


				String searchURLBase = "https://twitter.com/search?f=images&vertical=default&q=";
				addTotalDate(dateLimite);

				for(int i = 0 ; i < dateLimite ; ++i)
				{
					System.out.println(simpleDateFormat.format(calendar.getTime()));

					int currentSize = 0 , lastSize = 0;
					int trueTotalSize;

					String searchURL;

					searchURL = " until:" + simpleDateFormat.format(calendar.getTime());
					calendar.add(Calendar.DATE , -1);
					searchURL = " since:" + simpleDateFormat.format(calendar.getTime()) + searchURL;

					searchURL = searchKeyword + searchURL;
					searchURL = searchURLBase + URLEncoder.encode(searchURL , "UTF-8");

					webDriver.get(searchURL);
					List <WebElement> searchResult = webDriver.findElements(By.className("stream"));

					if(searchResult.size() == 0)
						continue;

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
								pushToQueue(imageElementList.get(currentDownloadIndex).getAttribute("src"), downloadQueue);

							lastSize = currentSize;
						}
						else if (trueTotalSize <= lastSize && downloadQueue.size() == 0)
						{
							System.out.println("Parse End");
							break;
						}
					}

					increaseCompletedDateBy1();
				}

				threadStatus.setFalse();

				System.out.println("Download completed !");
				webDriver.quit();
			}
			catch (InterruptedException e) { e.printStackTrace(); }
			catch (UnsupportedEncodingException e) { e.printStackTrace(); }
		}
	}



	class DownloadThreadManager extends Thread
	{
		String folderPath;
		Queue <String> downloadQueue;
		MutableBoolean threadStatus;

		public DownloadThreadManager(String folderPath , Queue <String> downloadQueue , MutableBoolean threadStatus)
		{
			this.folderPath = folderPath;
			this.downloadQueue = downloadQueue;
			this.threadStatus = threadStatus;
		}

		public void run()
		{
			try
			{
				while(threadStatus.isTrue() || downloadQueue.size() > 0)
				{
					if(downloadQueue.size() > 0)
					{
						while(threadCount >= threadLimit)
							Thread.sleep(200);

						updateThreadCount(1);
						new DownloadThread(pollFromQueue(downloadQueue), folderPath).start();
					}

					Thread.sleep(200);
				}
			}
			catch (InterruptedException e) { e.printStackTrace(); }
		}
	}

	class DownloadThread extends Thread
	{
		String imageUrl , folderPath;
		int downloadTimeout = 30000;

		public DownloadThread(String imageUrl , String folderPath)
		{
			this.imageUrl = imageUrl;
			this.folderPath = folderPath;
		}


		public void run()
		{
			try
			{
				String extension = imageUrl.substring( imageUrl.lastIndexOf(".") + 1 );
				String filename = imageUrl.substring( imageUrl.lastIndexOf("/") + 1 );
				filename = filename.replace("\\/><:\"|?*" , "A");

				while(new File(folderPath + "/" + filename).exists())
				{
					filename = filename.substring(0 , filename.lastIndexOf('.')) + "A" + filename.substring(filename.lastIndexOf('.'));
					System.out.println("Duplicated Filename !");
				}


				URL url = new URL(imageUrl);

				URLConnection urlConnection = url.openConnection();
				urlConnection.setConnectTimeout(downloadTimeout);
				urlConnection.setReadTimeout(downloadTimeout);

				BufferedImage bufferedImage = ImageIO.read(urlConnection.getInputStream());
				ImageIO.write(bufferedImage, extension, new File(folderPath + "/" + filename));

				updateThreadCount(-1);
			}
			catch (MalformedURLException e) { e.printStackTrace(); }
			catch (IOException e) { e.printStackTrace(); }
		}
	}
}
