import javafx.beans.property.SimpleDoubleProperty;

/**
 * Created by mathbookpeace on 2017/9/20.
 */

public class ThreadCounter
{
	private final int threadLimit = 40;
	private int threadCount = 0;

	private final Object lockForThreadCounter;

	public ThreadCounter ()
	{
		lockForThreadCounter = new Object();
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
