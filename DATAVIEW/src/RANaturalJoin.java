import java.util.ArrayList;
import dataview.models.DATAVIEW_Table;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;
/**
 * Relational Algebra Natural Join operator as a task
 * @author Austin Abro
 *
 */
public class RANaturalJoin extends Task
{
	public RANaturalJoin() 
	{
		super("RA Natural Join", "This is the realtional algebra natural join operator. It takes two tables");
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_Table, "This is the first table");
		ins[1] = new InputPort("in1", Port.DATAVIEW_Table, "This is the second table");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_Table, "This is the output");
	}

	@Override
	public void run()
	{
		//Two tables will be here and the string will be made from them. 
		final String extraGarbage = "1";
		DATAVIEW_Table table1 = (DATAVIEW_Table) ins[0].read();
		DATAVIEW_Table table2 = (DATAVIEW_Table) ins[1].read();
		String[] table1Header = table1.getHeader();
		String[] table2Header = table2.getHeader();
		ArrayList<String> removeCols = new ArrayList<String>();
		ArrayList<String> projectionVec = new ArrayList<String>();
		String selector = "";
		int count = 0;
		for(int i = 0; i < table1Header.length; i++) 
		{
			for(int j = 0; j < table2Header.length; j++) 
			{
				if(table1Header[i].contentEquals(table2Header[j])) 
				{
					
					table2Header[j] = table2Header[j] + extraGarbage;
					removeCols.add(table2Header[j]);
					if(count == 0)
						selector = selector + table1Header[i] + "==" + table2Header[j];
					else
						selector = selector + "&&" + table1Header[i] + "==" + table2Header[j]; 
					count++;
					
				}
			}
			projectionVec.add(table1Header[i]);
		}
		if(removeCols.size() == 0) 
			DATAVIEW_Table.logAndExit("To have a natural join you must have one common attributes between tables");
			
		for(int i = 0; i < table2Header.length; i++) 
		{
			boolean toRemove = false;
			for(int j = 0; j < removeCols.size(); j++) 
			{
				if(table2Header[i].contentEquals(removeCols.get(j)))
					toRemove = true;
			}
			if(!toRemove)
				projectionVec.add(table2Header[i]);
		}
		String[] project = new String[projectionVec.size()];
		for(int i = 0; i < project.length; i++)
			project[i] = projectionVec.get(i);
		DATAVIEW_Table productTable = DATAVIEW_Table.cartesianProduct(table1, table2);
		ConditionParser.RAOperator operator = ConditionParser.RAOperator.naturalJoin;
		ConditionParser parser = new ConditionParser(selector,productTable,operator,0);
		DATAVIEW_Table table = parser.parseOr(true);
		table.project(project);
		System.out.println(table.toString());
		outs[0].write(table);
	}
}
