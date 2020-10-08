import dataview.models.DATAVIEW_Table;
import dataview.models.Dataview;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;
/**
 * Relational algebra rename operator as a task
 * @author Austin Abro
 *
 */
public class RARename extends Task
{
	public RARename() 
	{
		super("RARename", "This is the the relational algebra operator rename. It has 2 inputs and 1 output ");
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_Table, "This is the table we are renaming");
		ins[1] = new InputPort("in1", Port.DATAVIEW_String, "This is the columns we are renaming. Format: newName:oldName, newName:oldName");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_Table, "This is the output");
	}

	@Override
	public void run() 
	{
		final int newNamePos = 0;
		final int oldNamePos = 1;
		final String format = "Make sure to follow the format. newName:oldName,newName:oldName";
		//format for re-naming is: 	NewColName:OldColName,NewColName:OldColName
		//Get table from file
		DATAVIEW_Table initalTable = (DATAVIEW_Table) ins[0].read();
		String renames = (String) ins[1].read();
		//takes away the newline character and puts each category of renames into its old string into it's own string
		String[] rename = renames.replace("\n", "").replace(" ", "").split(",");
		//This function finds all the spots where the selector equals to column name
		
		try 
		{
			String[] header = initalTable.getHeader();
			for(int i = 0; i < rename.length; i++) 
			{
				String newName = rename[i].split(":")[newNamePos];
				String oldName = rename[i].split(":")[oldNamePos];
				boolean nameFound = false;
				for (int j = 0; j < initalTable.getNumOfColumns(); j++) 
				{
					if(oldName.contentEquals(header[j]))
					{
						nameFound = true;
						header[j] = newName;
					}
				}
				if(!nameFound)
					throw new IllegalArgumentException("Column " + oldName + " is not in the table. " + format);
			}
			initalTable.setHeader(header);
			System.out.println(initalTable.toString());
			outs[0].write(initalTable);
		}
		catch(IllegalArgumentException e) 
		{
			DATAVIEW_Table.logAndExit(e);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			Dataview.debugger.logErrorMessage("You need to use a colon" + format);
			DATAVIEW_Table.logAndExit(e);
		}
	
	}
}
