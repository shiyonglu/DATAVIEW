import dataview.models.DATAVIEW_Table;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;
/**
 * Relational alegbra right join operator as a task
 * @author Austin Abro
 *
 */
public class RARightJoin extends Task
{
	public RARightJoin() 
	{
		super("RARightJOin", "This is the the relational algenbra operator Right Join. It takes two tables and a conditional statement. It outputs a table ");
		ins = new InputPort[3];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_Table, "This is the left table. Only the rows that satisfy the condition will be present in the result");
		ins[1] = new InputPort("in1", Port.DATAVIEW_Table, "This is the right table. All of the rows in this table will be present in the result");
		ins[2] = new InputPort("in2", Port.DATAVIEW_String, "This is the column operator and condition we are selecting");
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
		ConditionParser.RAOperator operator = ConditionParser.RAOperator.rightOuterJoin;
		ConditionParser parser = new ConditionParser(selector,initalTable,operator,table1.getHeader().length);
		DATAVIEW_Table table = parser.parseOr(true);
		System.out.println(table.toString());
		outs[0].write(table);
		
	}
}
