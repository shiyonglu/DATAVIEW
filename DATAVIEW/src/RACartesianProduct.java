import dataview.models.DATAVIEW_Table;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;
/**
 * Relational Algebra cartesian product operator
 * @author Austin Abro
 *
 */
public class RACartesianProduct extends Task
{
	public RACartesianProduct() 
	{
		super("RACartesianProdcut", "This is the the relational algebra for Cartesian Product. It takes in two tables and outputs one");
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_Table, "This is first table");
		ins[1] = new InputPort("in1", Port.DATAVIEW_Table, "This is second table ");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_Table, "This is the output");
	}

	@Override
	public void run() 
	{
		//Initializing variables
		DATAVIEW_Table table1 = (DATAVIEW_Table) ins[0].read();
		DATAVIEW_Table table2 = (DATAVIEW_Table) ins[1].read();
		DATAVIEW_Table newTable = DATAVIEW_Table.cartesianProduct(table1, table2);
		System.out.println(newTable.toString());
		outs[0].write(newTable);
	}
}
