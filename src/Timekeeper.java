import java.time.*;
import java.time.temporal.ChronoUnit;

public class Timekeeper 
{
	double period;
	LocalDate start;
	String category;
	long sheetId;
	String item;
	String assignedTo;
	String status;
	long rowId;
	double totalReminder;
	
	public Timekeeper(double period, LocalDate start, String category, String item, String assignedTo, String status, long rowId, long sheetId, double totalReminder) 
	{
		this.period = period;
		this.start = start;
		this.category = category;
		this.item = item;
		this.assignedTo = assignedTo;
		this.status = status;
		this.sheetId = sheetId;
		this.rowId = rowId;
		this.totalReminder = totalReminder;
	}
	
	public boolean checkTime() 
	{
		// get current time
		LocalDate cur = LocalDate.now();
		long val = ChronoUnit.DAYS.between(start, cur);
		
		if (val % period == 0)
			return true;
		
		return false;
	}
}