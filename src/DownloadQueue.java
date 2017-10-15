import java.util.LinkedList;
import java.util.Queue;


public class DownloadQueue
{
	private static DownloadQueue instance;

	private final Object lockForQueue;
	private Queue <ImageInfo> downloadQueue;


	private DownloadQueue()
	{
		lockForQueue = new Object();
		downloadQueue = new LinkedList();
	}

	public synchronized static DownloadQueue getInstance()
	{
		if(instance == null)
			instance = new DownloadQueue();
		return instance;
	}

	public int size() { return downloadQueue.size(); }


	public ImageInfo pollFromQueue()
	{
		synchronized (lockForQueue)
		{
			return downloadQueue.poll();
		}
	}

	public void add(ImageInfo imageInfo)
	{
		synchronized (lockForQueue)
		{
			downloadQueue.offer(imageInfo);
		}
	}
}
