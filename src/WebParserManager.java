
/**
 * Created by mathbookpeace on 2017/9/28.
 */
class WebParserManager extends Thread
{
	private final int NumberOfParser = 1;
	//private boolean isActive = true;

	public WebParserManager ()
	{
	}

	//public void dispose () { isActive = false; }

	public void run ()
	{
		for(int i = 0 ; i < NumberOfParser && GreatTwitterDownloader.isActive ; ++i)
		{
			WebParser webParser = new WebParser();
			webParser.start();
		}
	}
}
