import dataview.models.DATAVIEW_Table;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;
/**
 * Relational Algebra left join operator as a task
 * @author Austin Abro
 *
 */
public class RALeftJoin extends Task
{
	public RALeftJoin() 
	{
		super("RALeftJoin", "This is the left join operator. It takes two tables and a string with a condition statement");
		ins = new InputPort[3];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_Table, "This is the first table. All tuples from this will be included");
		ins[1] = new InputPort("in1", Port.DATAVIEW_Table, "This is second table. Only the rows that satisfy the condition will be in the result");
		ins[2] = new InputPort("in2", Port.DATAVIEW_String, "This is the conditional statement");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_Table, "This is the outputed table");
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
		ConditionParser.RAOperator operator = ConditionParser.RAOperator.leftOuterJoin;
		ConditionParser parser = new ConditionParser(selector,initalTable,operator,table1.getHeader().length);
		DATAVIEW_Table table = parser.parseOr(true);
		System.out.println(table.toString());
		outs[0].write(table);
		
	}
}
