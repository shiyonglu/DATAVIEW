import dataview.models.DATAVIEW_Table;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;
/**
 * Relational Algebra intersection operator as a  task
 * @author Austin Abro
 *
 */
public class RAIntersection extends Task
{
	public RAIntersection() 
	{
		super("RAIntersection", "This is the the relational algebra operator intersection. It has 2 inputs and 1 output ");
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_Table, "This is the first table");
		ins[1] = new InputPort("in1", Port.DATAVIEW_Table, "This is the second table");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_Table, "This is the output table");
	}

	@Override
	public void run() 
	{
		DATAVIEW_Table initalTable = (DATAVIEW_Table) ins[0].read();
		DATAVIEW_Table other = (DATAVIEW_Table) ins[1].read();
		initalTable.intersection(other);
		System.out.println(initalTable.toString());
		outs[0].write(initalTable);
	}
}
