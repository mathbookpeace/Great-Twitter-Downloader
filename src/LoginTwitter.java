import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Set;

/**
 * Created by mathbookpeace on 2017/10/1.
 */
class LoginTwitter extends Thread
{
	static public Set <Cookie> twitterCookie = null;

	public LoginTwitter()
	{
	}

	public void run ()
	{
		WebDriver webDriver = new ChromeDriver();
		webDriver.get("https://twitter.com/login");
		while(webDriver.findElements(By.className("js-signout-button")).size() == 0);

		twitterCookie = webDriver.manage().getCookies();
		webDriver.quit();
	}
}
