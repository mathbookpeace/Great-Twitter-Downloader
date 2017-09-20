import javafx.beans.property.SimpleDoubleProperty;

public class DateCounter
{
	private double totalDate = 0;
	private double completedDate = 0;

	private final Object lockForProgress;
	private SimpleDoubleProperty simpleDoubleProperty = null;

	public DateCounter()
	{
		lockForProgress = new Object();
	}

	public void SetSimpleDoubleProperty(SimpleDoubleProperty simpleDoubleProperty)
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
