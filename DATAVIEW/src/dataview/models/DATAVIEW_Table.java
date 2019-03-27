package dataview.models;
	/* 
	 * This table is implemented based on the assumption that a lot of calculations and updates will be performed on this table.
	 *  A table can be considered as a matrix in which each element has a type of String.
	 *  
	 *  We try to emulate the semantics of a relational table: we might delete/insert/update a row or a cell when necessary.
	 * 
	 * Shiyong Lu
	 * July 10, 2018
	 *
	 **/
import java.util.Vector;

public class DATAVIEW_Table {
	private int n; // number of columns, the number of features
	private int m; // number of rows, the number of examples
	private Vector<String []> elements; // vector's elements

	public DATAVIEW_Table(int n) // n is the number of columns
	{
		this.n = n;
		this.m =  0;
		this.elements = new Vector<String[]>(); 
	}
	
			
	public int getNumOfRows()
	{
		return m;
	}
			
	public int getNumOfColumns()
	{
		return n;
	}
			
	public String get(int i, int j) 
	{
		return elements.get(i)[j];
						
	}
			
	public void set(int i, int j, String val)
	{
		elements.get(i)[j] = val;
	}			
	
	public void appendRow(String [] newrow)
	{
		elements.add(newrow);
	}
	
	public String [] getRow(int i) // retrieve the ith row
	{
		return this.elements.get(i);
	}

    @Override
    public String toString() 
	{
		String str = "";
		
		for(String [] row: elements) {
			str = str + row[0]; // append column 0
			for(int j=1; j<n; j++)
				str += ":" + row[j];
			str = str + "\n";
		}
		
		return str;
	}				
}
