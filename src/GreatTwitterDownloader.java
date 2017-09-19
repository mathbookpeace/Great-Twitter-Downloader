import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
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
		Queue <Pair> downloadQueue = new LinkedList();
		MutableBoolean threadStatus = new MutableBoolean();
		threadStatus.setTrue();

		DownloadWithKeywordThread downloadWithKeywordThread = new DownloadWithKeywordThread(searchKeyword , downloadQueue , threadStatus);
		downloadWithKeywordThread.setDaemon(true);
		downloadWithKeywordThread.start();

		DownloadThreadManager downloadThreadManager = new DownloadThreadManager(searchKeyword , downloadQueue , threadStatus);
		downloadThreadManager.setDaemon(true);
		downloadThreadManager.start();
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

	private Pair pollFromQueue(Queue <Pair> downloadQueue)
	{
		synchronized (lockForQueue)
		{
			return downloadQueue.poll();
		}
	}

	private void pushToQueue(Pair pair , Queue <Pair> downloadQueue)
	{
		synchronized (lockForQueue)
		{
			downloadQueue.offer(pair);
		}
	}


	private class Pair
	{
		String url;
		String filename;

		public Pair(String url , String filename)
		{
			this.url = url;
			this.filename = filename;
		}
	}


	private class DownloadWithKeywordThread extends Thread
	{
		private String searchKeyword;
		private Queue <Pair> downloadQueue;
		MutableBoolean threadStatus;

		public DownloadWithKeywordThread(String searchKeyword , Queue <Pair> downloadQueue , MutableBoolean threadStatus)
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
			SimpleDateFormat simpleDateFormatFilename = new SimpleDateFormat("yyyy-MM-dd");


			try
			{
				ChromeOptions chromeOptions = new ChromeOptions();
				chromeOptions.addArguments("--headless");
				WebDriver webDriver = new ChromeDriver(chromeOptions);


				String searchURLBase = "https://twitter.com/search?f=images&vertical=default&q=";
				addTotalDate(dateLimite);

				for(int i = 0 ; i < dateLimite ; ++i)
				{
					System.out.println(simpleDateFormatFilename.format(calendar.getTime()));

					int currentSize = 0 , lastSize = 0;
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
								pushToQueue(new Pair(imageElementList.get(currentDownloadIndex).getAttribute("src") , untilDate + "_" + StringUtils.leftPad("" + currentPageNumber++ , 2 , "0")) , downloadQueue);

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
		Queue <Pair> downloadQueue;
		MutableBoolean threadStatus;

		public DownloadThreadManager(String folderPath , Queue <Pair> downloadQueue , MutableBoolean threadStatus)
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
		Pair fileData;
		String folderPath;
		int downloadTimeout = 30000;

		public DownloadThread(Pair fileData , String folderPath)
		{
			this.fileData = fileData;
			this.folderPath = folderPath;
		}


		public void run()
		{
			try
			{
				String extension = fileData.url.substring( fileData.url.lastIndexOf(".") + 1 );
				String filename = fileData.filename + "." + extension;
				filename = filename.replace("\\/><:\"|?*" , "A");

				while(new File(folderPath + "/" + filename).exists())
				{
					filename = filename.substring(0 , filename.lastIndexOf('.')) + "A" + filename.substring(filename.lastIndexOf('.'));
					System.out.println("Duplicated Filename !");
				}


				URL url = new URL(fileData.url);

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
