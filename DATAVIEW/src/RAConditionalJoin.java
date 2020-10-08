import dataview.models.DATAVIEW_Table;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;
/**
 * Relational Algebra Conditional Join as a task
 * @author Austin Abro
 *
 */
public class RAConditionalJoin extends Task
{
	public RAConditionalJoin() 
	{
		super("RAConditonalJoin", "This is the the relational algenbra operator conditonalJOin. It has two inputs and 1 output ");
		ins = new InputPort[3];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_Table, "This is the first table");
		ins[1] = new InputPort("in1", Port.DATAVIEW_Table, "This is the second table");
		ins[2] = new InputPort("in2", Port.DATAVIEW_String, "This is the conditional statement");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_Table, "This is the output");
	}

	@Override
	public void run()
	{
		//The format will in the following format:
		//!*((condition operator value)* (&\|)*) 
		//Parenthesis must follow negation if used.
		//Get table from file
		DATAVIEW_Table table1 = (DATAVIEW_Table) ins[0].read();
		DATAVIEW_Table table2 = (DATAVIEW_Table) ins[1].read();
		String selector = (String) ins[2].read();
		DATAVIEW_Table initalTable = DATAVIEW_Table.cartesianProduct(table1, table2);
		ConditionParser.RAOperator operator = ConditionParser.RAOperator.conditionalJoin;
		ConditionParser parser = new ConditionParser(selector,initalTable,operator,0);
		DATAVIEW_Table table = parser.parseOr(true);
		System.out.println(table.toString());
		outs[0].write(table);	
	}
}
