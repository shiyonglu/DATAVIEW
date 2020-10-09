import java.util.Arrays;

import dataview.models.DATAVIEW_Table;
import dataview.models.Dataview;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;
/**
 * Relational Algebra project operator as a task
 * @author Austin Abro
 *
 */
public class RAProject extends Task
{
	public RAProject() 
	{
		super("RAProjector Project", "This is the the relational algenbra operator projection. It has 2 inputs and 1 output ");
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_Table, "This is the table which we are using to get the projection");
		ins[1] = new InputPort("in1", Port.DATAVIEW_String, "This is the column we are selecting");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_Table, "This is the output");
	}

	@Override
	public void run() 
	{
		//FORMAT: colName1,colName2,...,ColNameN
		//Get table from file
		DATAVIEW_Table initalTable = (DATAVIEW_Table) ins[0].read();
		String projector = (String) ins[1].read();
		String[] projectors = projector.replace("\n", "").split(",");
		System.out.println(Arrays.toString(projectors));
		try 
		{
			initalTable.project(projectors);
			System.out.println(initalTable.toString());
			outs[0].write(initalTable);
		}
		catch(IllegalArgumentException e) 
		{
			DATAVIEW_Table.logAndExit(e);
		}
	}
}
