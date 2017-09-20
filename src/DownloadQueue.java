import java.util.LinkedList;
import java.util.Queue;


public class DownloadQueue
{
	private final Object lockForQueue;
	private Queue <ImageInfo> downloadQueue;


	public DownloadQueue()
	{
		lockForQueue = new Object();
		downloadQueue = new LinkedList();
	}

	public int size() { return downloadQueue.size(); }


	public ImageInfo pollFromQueue()
	{
		synchronized (lockForQueue)
		{
			return downloadQueue.poll();
		}
	}

	public void pushToQueue(ImageInfo imageInfo)
	{
		synchronized (lockForQueue)
		{
			downloadQueue.offer(imageInfo);
		}
	}
}
