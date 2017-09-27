public class DownloadThreadManager extends Thread
{
	DownloadQueue downloadQueue;
	ThreadCounter threadCounter;

//----------------------------------------------------------------------------------------------------------------------------------------

	public DownloadThreadManager()
	{
		this.downloadQueue = DownloadQueue.getInstance();
		threadCounter = ThreadCounter.getInstance();
	}


	public void run()
	{
		try
		{
			while(GreatTwitterDownloader.isActive || downloadQueue.size() > 0)
			{
				if (downloadQueue.size() <= 0)
					Thread.sleep(500);
				else
				{
					while (threadCounter.isReachedLimit())
						Thread.sleep(500);
					new DownloadThread(downloadQueue.pollFromQueue()).start();
				}
			}
		}
		catch (InterruptedException e) { e.printStackTrace(); }
	}
}