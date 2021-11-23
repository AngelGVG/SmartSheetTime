// Add Maven library "com.smartsheet:smartsheet-sdk-java:2.2.3" to access Smartsheet Java SDK
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.SmartsheetFactory;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;
import com.smartsheet.api.oauth.Token;

import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.*;


public class RWSheet 
{
	static 
	{
		// These lines enable logging to the console
		System.setProperty("Smartsheet.trace.parts", "RequestBodySummary,ResponseBodySummary");
		System.setProperty("Smartsheet.trace.pretty", "true");
	}

	// The API identifies columns by Id, but it's more convenient to refer to column names
	private static HashMap<String, Long> columnMap = new HashMap<String, Long>();   // Map from friendly column name to column Id

	public static void main(final String[] args) 
	{
		try {
			try
			{
				Class.forName("com.mysql.cj.jdbc.Driver");

				// Establishing Connection
				Connection con = DriverManager.getConnection(
						"jdbc:mysql://159.89.42.80/timesheet_TimeKeepers", "timesheet_vergan01", "B5YJNBWRR&_&");

				if (con != null)            
				{
					System.out.println("Connected");   
					Statement s = con.createStatement();
					ResultSet results = s.executeQuery("Select * FROM Sheets;");
					ResultSetMetaData rsmd = results.getMetaData();
					int columnsNumber = rsmd.getColumnCount();
					ArrayList<String[]> sheets = new ArrayList<>();

					while (results.next()) {
						String[] set = new String[3];
						set[0] = results.getString(1);
						set[1] = results.getString(2);
						set[2] = results.getString(3);
						sheets.add(set);
					}
					Smartsheet smartsheet = SmartsheetFactory.createDefaultClient();

					//smartsheet.serverInfoResources().


					for(String[] set : sheets) 
					{
						smartsheet.setAccessToken(set[1]);
						//SmartsheetFactory.createDefaultClient();
						ArrayList<Timekeeper> users = new ArrayList<Timekeeper>();

						//change string to the id of the sheet you want to read
						long id = Long.parseLong(set[0]); //hardcoded
						Sheet reminder = smartsheet.sheetResources().getSheet(id, null, null, null, null, null, null, null, null, null);

						// put the columns in a hashmap for easy acesss
						for (Column column : reminder.getColumns()) {
							columnMap.put(column.getTitle(), column.getId());
						}

						List<Row> rows = reminder.getRows();
						ArrayList<Row> re = new ArrayList<>();

						for(Row r : rows) {
							Cell cell = getCellByColumnName(r, "Status");
							Object value = cell.getValue();

							String val = (String) value;
							if(val != null && val.equals("In Progress")) {
								System.out.println(r.getId());
								re.add(r);

								Cell periodCell = getCellByColumnName(r, "Reminder Frequency");
								double period = (Double) periodCell.getValue();

								Cell startCell = getCellByColumnName(r, "Assigned On");
								String startStr = (String) startCell.getValue();
								LocalDate start = LocalDate.parse(startStr);

								Cell categoryCell = getCellByColumnName(r, "Category");
								String category = (String) categoryCell.getValue();

								Cell itemCell = getCellByColumnName(r, "Follow-Up Item");
								String item = (String) itemCell.getValue();

								Cell assignedToCell = getCellByColumnName(r, "Assigned To");
								String assignedTo = (String) assignedToCell.getValue();

								Cell remindersCell = getCellByColumnName(r, "Reminders Sent");

								double reminders = 0;
								if(remindersCell.getValue() != null) {
									reminders = (Double) remindersCell.getValue();
								}

								Token tok = new Token();
								//smartsheet.tokenResources().refreshAccessToken();
							//	System.out.println(smartsheet.);
								Timekeeper current = new Timekeeper(period, start, category, item, assignedTo, "In Progress", r.getId(), id, reminders, set[1], set[2]);

								System.out.println("this is the total reminder " + current.totalReminder);
								users.add(current);		
							}
						}
						updateAllTimekeepers(users);
					}
				}
				else           
					System.out.println("Not Connected");

				con.close();
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
			// Initialize client. Gets API access token from SMARTSHEET_ACCESS_TOKEN variable
			// Token is set as environment variable in Eclipse settings

		} 
		catch (Exception ex) {
			System.out.println("Exception : " + ex.getMessage());
			ex.printStackTrace();
		}
	}


	public static void updateAllTimekeepers(ArrayList<Timekeeper> al) 
	{
		for (Timekeeper tk : al) {
			boolean check = tk.checkTime();
			if(check == true) {
				tk.totalReminder++;
				updateCells(tk);
				sendEmail.reminder(tk);
			}
			System.out.println(check);
		}
	}

	// Helper function to find cell in a row
	static Cell getCellByColumnName(Row row, String columnName)
	{
		Long colId = columnMap.get(columnName);

		return row.getCells().stream()
				.filter(cell -> colId.equals((Long) cell.getColumnId()))
				.findFirst()
				.orElse(null);
	}

	public static void updateCells(Timekeeper tk) 
	{
		Smartsheet smartsheet = SmartsheetFactory.createDefaultClient();

		smartsheet.setAccessToken(tk.userToken);

		try {
			Row r = smartsheet.sheetResources().rowResources().getRow(tk.sheetId, tk.rowId, null, null);
			Cell remindersCell = getCellByColumnName(r, "Reminders Sent");
			remindersCell.setValue(tk.totalReminder);
			List<Cell> cellsToUpdate = Arrays.asList(remindersCell);

			Row rowToUpdate = new Row();
			rowToUpdate.setId(tk.rowId);
			rowToUpdate.setCells(cellsToUpdate);

			smartsheet.sheetResources().rowResources().updateRows(tk.sheetId, Arrays.asList(rowToUpdate));
		} 
		catch (SmartsheetException e) {
			e.printStackTrace();
		}
	}
}