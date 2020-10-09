import dataview.models.DATAVIEW_Table;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;
/**
 * Relational algebra select operator as a task
 * @author austin
 *
 */
public class RASelect extends Task
{
	public RASelect() 
	{
		super("RASelector", "This is the the relational algenbra operator selector. It has two inputs and 1 output ");
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_Table, "This is the table which we are selecting from");
		ins[1] = new InputPort("in1", Port.DATAVIEW_String, "This is the column operator and condition we are selecting");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_Table, "This is the output");
	}

	@Override
	public void run()
	{
		//The format will in the following format:
		//!*((condition operator value)* (&\|)*) 
		//Parenthesis must follow negation if used.
		//Get table from file
		DATAVIEW_Table initalTable = (DATAVIEW_Table) ins[0].read();
		String selector = (String) ins[1].read();
		ConditionParser.RAOperator operator = ConditionParser.RAOperator.select;
		ConditionParser parser = new ConditionParser(selector,initalTable,operator,0);
		DATAVIEW_Table table = parser.parseOr(true);
		System.out.println(table.toString());
		outs[0].write(table);
		
	}
}
