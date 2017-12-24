class DownloadThreadManager extends Thread
{
//----------------------------------------------------------------------------------------------------------------------------------------

	public DownloadThreadManager()
	{
	}


	public void run()
	{
		for (int i = 0 ; i < 40 ; ++i)
			new DownloadThread().start();
	}
}