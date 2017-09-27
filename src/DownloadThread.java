import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by mathbookpeace on 2017/9/20.
 */
public class DownloadThread extends Thread
{
	ImageInfo imageInfo;
	private DuplicatedImageChecker duplicatedImageChecker;
	private ThreadCounter threadCounter;

	final int downloadTimeout = 30000;

	public DownloadThread(ImageInfo imageInfo)
	{
		this.imageInfo = imageInfo;

		duplicatedImageChecker = DuplicatedImageChecker.getInstance();
		threadCounter = ThreadCounter.getInstance();

		threadCounter.updateThreadCount(1);
	}


	public void run()
	{
		try
		{
			String extension = imageInfo.url.substring( imageInfo.url.lastIndexOf(".") + 1 );
			String filename = imageInfo.filename + "." + extension;
			filename = filename.replace("[\\\\/><:\"|?*]" , "A");

			while(new File(filename).exists())
			{
				filename = filename.substring(0 , filename.lastIndexOf('.')) + "A" + filename.substring(filename.lastIndexOf('.'));
				System.out.println("Duplicated Filename !");
			}

			URL url = new URL(imageInfo.url);

			URLConnection urlConnection = url.openConnection();
			urlConnection.setConnectTimeout(downloadTimeout);
			urlConnection.setReadTimeout(downloadTimeout);

			File downloadFile = new File(filename);

			BufferedImage bufferedImage = ImageIO.read(urlConnection.getInputStream());
			ImageIO.write(bufferedImage, extension , downloadFile);

			if (duplicatedImageChecker.isExistImage(downloadFile , imageInfo.parserIndex))
				downloadFile.delete();

			threadCounter.updateThreadCount(-1);
		}
		catch (MalformedURLException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}
}
