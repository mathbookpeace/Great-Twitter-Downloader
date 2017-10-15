import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by mathbookpeace on 2017/9/20.
 */
class DownloadThread extends Thread
{
	private DuplicatedImageChecker duplicatedImageChecker = DuplicatedImageChecker.getInstance();
	private static DownloadQueue downloadQueue = DownloadQueue.getInstance();

	private final int downloadTimeout = 30000;
	private static Object waitKey = new Object();


	public DownloadThread()
	{
	}



	private void waitForTask() throws InterruptedException
	{
		synchronized (waitKey) { waitKey.wait(); }
	}

	public static void notifyForTask()
	{
		synchronized (waitKey) { waitKey.notify(); }
	}
	public static void notifyAllForTask()
	{
		synchronized (waitKey) { waitKey.notifyAll(); }
	}



	public void run()
	{
		try
		{
			ImageInfo imageInfo = null;

			while (GreatTwitterDownloader.isActive)
			{
				while (GreatTwitterDownloader.isActive && (imageInfo = downloadQueue.pollFromQueue()) == null)
					waitForTask();
				if (!GreatTwitterDownloader.isActive)
					break;

				String extension = imageInfo.url.substring(imageInfo.url.lastIndexOf(".") + 1);
				String filename = imageInfo.filename + "." + extension;
				filename = filename.replace("[\\\\/><:\"|?*]", "A");

				while (new File(filename).exists())
				{
					filename = filename.substring(0, filename.lastIndexOf('.')) + "A" + filename.substring(filename.lastIndexOf('.'));
					System.out.println("Duplicated Filename !");
				}

				URL url = new URL(imageInfo.url);

				URLConnection urlConnection = url.openConnection();
				urlConnection.setConnectTimeout(downloadTimeout);
				urlConnection.setReadTimeout(downloadTimeout);

				File downloadFile = new File(filename);

				InputStream inputStream = urlConnection.getInputStream();
				FileOutputStream fileOutputStream = new FileOutputStream(downloadFile);

				byte[] fileByte = new byte[1];
				while (inputStream.read(fileByte) != -1)
					fileOutputStream.write(fileByte);

				inputStream.close();
				fileOutputStream.close();

				if (duplicatedImageChecker.isExistImage(downloadFile, imageInfo.parserIndex))
					downloadFile.delete();
			}
		}
		catch (IOException e) { e.printStackTrace(); }
		catch (InterruptedException e) { e.printStackTrace(); }
	}
}
