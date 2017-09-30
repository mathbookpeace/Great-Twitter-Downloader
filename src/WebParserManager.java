
/**
 * Created by mathbookpeace on 2017/9/28.
 */
class WebParserManager extends Thread
{
	private final int NumberOfParser = 4;

	public WebParserManager ()
	{
	}

	public void run ()
	{
		for(int i = 0 ; i < NumberOfParser ; ++i)
		{
			WebParser webParser = new WebParser();
			webParser.start();
		}
	}
}
