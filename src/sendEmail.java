import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.SmartsheetFactory;
import com.smartsheet.api.models.MultiRowEmail;
import com.smartsheet.api.models.Recipient;
import com.smartsheet.api.models.RecipientEmail;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.SheetEmail;
import com.smartsheet.api.models.enums.SheetEmailFormat;

public class sendEmail {

	public static boolean reminder(String email, ArrayList<Row> rows, long sheetId) {
	
		RecipientEmail recipientEmail = new RecipientEmail().setEmail(email);

		List<Recipient> recipientList = Arrays.asList(recipientEmail);
		
//		SheetEmail emailSpecification = new SheetEmail();
//		emailSpecification.setFormat(SheetEmailFormat.PDF);
//		emailSpecification.setFormatDetails(formatDetails)
//		  .setSendTo(recipientList)
//		  .setSubject("Reminder")
//		  .setMessage("You need to work on a task on this Smartsheet")
//		  .setCcMe(false);
		
		List<Long> ids = new ArrayList<Long>();
		for(Row r : rows) {
			ids.add(r.getId());
		}

		MultiRowEmail multiRowEmail = new MultiRowEmail.AddMultiRowEmailBuilder()
				  .setSendTo(recipientList)
				  .setSubject("Reminder")
				  .setMessage("You need to work on this task")
				  .setCcMe(false)
				  .setRowIds(ids)
				  .setIncludeAttachments(false)
				  .setIncludeDiscussions(false)
				  .build();

		Smartsheet smartsheet = SmartsheetFactory.createDefaultClient();
		try {
			smartsheet.sheetResources().rowResources().sendRows(sheetId, multiRowEmail);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
