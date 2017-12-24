/**
 * Created by mathbookpeace on 2017/9/20.
 */

public class ImageInfo
{
	String keywordUTF8;
	String url;
	String filename;
	byte parserIndex;

	public ImageInfo(String url , String filename , byte parserIndex)
	{
		this.keywordUTF8  = "";
		this.url = url;
		this.filename = filename;
		this.parserIndex = parserIndex;
	}

	public ImageInfo(String keywordUTF8 , String url , String filename , byte parserIndex)
	{
		this.keywordUTF8  =  keywordUTF8;
		this.url = url;
		this.filename = filename;
		this.parserIndex = parserIndex;
	}
}
