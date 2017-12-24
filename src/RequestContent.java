import java.time.LocalDate;

/**
 * Created by mathbookpeace on 2017/10/20.
 */
class RequestContent
{
	public String searchKeyword;
	public LocalDate sinceDate , untilDate;

	public RequestContent (String searchKeyword , LocalDate sinceDate , LocalDate untilDate)
	{
		this.searchKeyword = searchKeyword;
		this.sinceDate = sinceDate;
		this.untilDate = untilDate;
	}
}
