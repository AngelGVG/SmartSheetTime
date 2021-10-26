import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetFactory;
import com.smartsheet.api.models.MultiRowEmail;
import com.smartsheet.api.models.Recipient;
import com.smartsheet.api.models.RecipientEmail;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.RowEmail;
import com.smartsheet.api.models.SheetEmail;
import com.smartsheet.api.models.enums.SheetEmailFormat;

public class sendEmail 
{

	public static boolean reminder(Timekeeper time) 
	{
	
		RecipientEmail recipientEmail = new RecipientEmail().setEmail(time.assignedTo);

		List<Recipient> recipientList = Arrays.asList(recipientEmail);
		
		List<Long> ids = new ArrayList<Long>();
		ids.add(time.rowId);

		MultiRowEmail multiRowEmail = new MultiRowEmail.AddMultiRowEmailBuilder()
				  .setSendTo(recipientList)
				  .setSubject("Reminder")
				  .setMessage("You need to work on this task")
				  .setCcMe(false)
				  .setRowIds(ids)
				  .setIncludeAttachments(false)
				  .setIncludeDiscussions(false)
				  .build();
		
//		RowEmail multiRowEmail = new RowEmail.AddRowEmailBuilder()
//				  .setSendTo(recipientList)
//				  .setSubject("Reminder")
//				  .setMessage("You need to work on this task")
//				  .setCcMe(false)
//				  .setIncludeAttachments(false)
//				  .setIncludeDiscussions(false)
//				  .build();

		Smartsheet smartsheet = SmartsheetFactory.createDefaultClient();
		try {
			smartsheet.sheetResources().rowResources().sendRows(time.sheetId, multiRowEmail);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}