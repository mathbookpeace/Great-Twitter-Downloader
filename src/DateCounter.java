import javafx.beans.property.SimpleDoubleProperty;

public class DateCounter
{
	private static DateCounter instance = null;

	private double totalDate = 0;
	private double completedDate = 0;

	private final Object lockForProgress;
	private SimpleDoubleProperty simpleDoubleProperty = null;

	private DateCounter()
	{
		lockForProgress = new Object();
	}

	public synchronized static DateCounter getInstance ()
	{
		if (instance == null)
			instance = new DateCounter();
		return instance;
	}



	public void setSimpleDoubleProperty(SimpleDoubleProperty simpleDoubleProperty)
	{
		this.simpleDoubleProperty = simpleDoubleProperty;
	}

	public void increaseCompletedDateBy1()
	{
		synchronized (lockForProgress)
		{
			++completedDate;

			if(simpleDoubleProperty != null)
				simpleDoubleProperty.set( (totalDate == 0) ? 0 : (completedDate / totalDate) );
		}
	}

	public void addTotalDate(double totalDate)
	{
		synchronized (lockForProgress)
		{
			this.totalDate += totalDate;

			if(simpleDoubleProperty != null)
				simpleDoubleProperty.set( (totalDate == 0) ? 0 : (completedDate / totalDate) );
		}
	}
}
