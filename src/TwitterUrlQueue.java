import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by mathbookpeace on 2017/9/28.
 */
public class TwitterUrlQueue
{
	private static TwitterUrlQueue instance = null;

	private Queue <ImageInfo> urlQueue;
	private final Object queueLock;

	private TwitterUrlQueue ()
	{
		urlQueue = new LinkedList();
		queueLock = new Object();
	}

	public synchronized static TwitterUrlQueue getInstance()
	{
		if(instance == null)
			instance = new TwitterUrlQueue();
		return instance;
	}



	public void add (ImageInfo twitterUrlInfo)
	{
		synchronized (queueLock)
		{
			urlQueue.offer(twitterUrlInfo);
		}
	}

	public ImageInfo poll ()
	{
		synchronized (queueLock)
		{
			return urlQueue.poll();
		}
	}
}
