import dataview.models.DATAVIEW_Table;
import dataview.models.InputPort;
import dataview.models.OutputPort;
import dataview.models.Port;
import dataview.models.Task;
/**
 * Relational algebra division operator as a task 
 * @author Austin Abro
 *
 */
public class RADivision extends Task
{
	public RADivision() 
	{
		super("RADivision", "This is the the relational algebra operator for Division");
		ins = new InputPort[2];
		outs = new OutputPort[1];
		ins[0] = new InputPort("in0", Port.DATAVIEW_Table, "This is dividend table");
		ins[1] = new InputPort("in1", Port.DATAVIEW_Table, "This is divisor table ");
		outs[0] = new OutputPort("out0", Port.DATAVIEW_Table, "This is the output");
	}

	@Override
	public void run() 
	{
		DATAVIEW_Table dividend = (DATAVIEW_Table) ins[0].read();
		DATAVIEW_Table divisor = (DATAVIEW_Table) ins[1].read();
		String[] dividendHeader = dividend.getHeader();
		String[] divisorHeader = divisor.getHeader();
		String[] newTableHeader = new String[dividendHeader.length-divisorHeader.length];
		int newHeaderPos = 0;
		for(int colPosDividend = 0; colPosDividend < dividendHeader.length; colPosDividend++)
		{
			boolean nameFound  = false;
			for(int colPosDivisor = 0; colPosDivisor < divisorHeader.length; colPosDivisor++) 
			{
				if(dividendHeader[colPosDividend].contentEquals(divisorHeader[colPosDivisor])) 
					nameFound = true;
			}
			if(!nameFound && newHeaderPos < newTableHeader.length) 
			{
				newTableHeader[newHeaderPos] = dividendHeader[colPosDividend];
				newHeaderPos++;
			}
		}
		DATAVIEW_Table answer = dividend.copy();
		answer.project(newTableHeader);
		DATAVIEW_Table toMultiply = answer.copy();
		DATAVIEW_Table product = DATAVIEW_Table.cartesianProduct(toMultiply, divisor);
		product.setDifference(dividend);
		product.project(newTableHeader);
		answer.setDifference(product);
		System.out.println(answer.toString());
		outs[0].write(answer);
	}
}
