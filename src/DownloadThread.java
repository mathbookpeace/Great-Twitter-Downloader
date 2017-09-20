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
	ImageInfo fileData;
	ThreadCounter threadCounter;

	final int downloadTimeout = 30000;

	public DownloadThread(ImageInfo fileData , ThreadCounter threadCounter)
	{
		this.fileData = fileData;
		this.threadCounter = threadCounter;
	}


	public void run()
	{
		try
		{
			String extension = fileData.url.substring( fileData.url.lastIndexOf(".") + 1 );
			String filename = fileData.filename + "." + extension;
			filename = filename.replace("\\/><:\"|?*" , "A");

			while(new File(filename).exists())
			{
				filename = filename.substring(0 , filename.lastIndexOf('.')) + "A" + filename.substring(filename.lastIndexOf('.'));
				System.out.println("Duplicated Filename !");
			}

			URL url = new URL(fileData.url);

			URLConnection urlConnection = url.openConnection();
			urlConnection.setConnectTimeout(downloadTimeout);
			urlConnection.setReadTimeout(downloadTimeout);

			BufferedImage bufferedImage = ImageIO.read(urlConnection.getInputStream());
			ImageIO.write(bufferedImage, extension, new File(filename));

			threadCounter.updateThreadCount(-1);
		}
		catch (MalformedURLException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}
}
