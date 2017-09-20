public class DownloadThreadManager extends Thread
{
	DownloadQueue downloadQueue;
	ThreadCounter threadCounter;

//----------------------------------------------------------------------------------------------------------------------------------------

	public DownloadThreadManager(DownloadQueue downloadQueue)
	{
		this.downloadQueue = downloadQueue;
		threadCounter = new ThreadCounter();
	}


	public void run()
	{
		try
		{
			while(true)
			{
				if(downloadQueue.size() > 0)
				{
					while(threadCounter.isReachedLimit())
						Thread.sleep(500);

					threadCounter.updateThreadCount(1);
					new DownloadThread(downloadQueue.pollFromQueue() , threadCounter).start();

					Thread.sleep(40);
				}
				else
					Thread.sleep(500);
			}
		}
		catch (InterruptedException e) { e.printStackTrace(); }
	}
}