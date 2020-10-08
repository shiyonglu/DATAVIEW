import dataview.models.DATAVIEW_Table;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;
/**
 * Relational Algebra set difference operator as a task. 
 * @author austin
 *
 */
public class RASetDifference extends Task
{
	public RASetDifference() 
	{
		super("RASetDifference", "This is the relational algebra operator set difference. It will take two tables and output one table ");
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_Table, "This is first table");
		ins[1] = new InputPort("in1", Port.DATAVIEW_Table, "This is the second table");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_Table, "This is the output");
	}

	@Override
	public void run() 
	{
		DATAVIEW_Table initalTable = (DATAVIEW_Table) ins[0].read();
		DATAVIEW_Table other = (DATAVIEW_Table) ins[1].read();
		initalTable.setDifference(other);
		System.out.println(initalTable.toString());
		outs[0].write(initalTable);
	}
}
