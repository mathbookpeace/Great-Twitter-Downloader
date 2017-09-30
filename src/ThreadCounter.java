/**
 * Created by mathbookpeace on 2017/9/20.
 */

public class ThreadCounter
{
	private static ThreadCounter instance = null;

	private final int threadLimit = 40;
	private int threadCount = 0;

	private final Object lockForThreadCounter;

	private ThreadCounter ()
	{
		lockForThreadCounter = new Object();
	}

	public synchronized static ThreadCounter getInstance()
	{
		if(instance == null)
			instance = new ThreadCounter();
		return instance;
	}

	public void updateThreadCount(int addThreadCount)
	{
		synchronized (lockForThreadCounter)
		{
			threadCount += addThreadCount;
		}
	}

	public boolean isReachedLimit() { return threadCount >= threadLimit; }
}
