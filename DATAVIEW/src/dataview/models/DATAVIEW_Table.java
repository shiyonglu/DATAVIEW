package dataview.models;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
/* 
	 * This table is implemented based on the assumption that a lot of calculations and updates will be performed on this table
.
	 *  A table can be considered as a matrix in which each element has a type of String.
	 *  
	 *  We try to emulate the semantics of a relational table: we might delete/insert/update a row or a cell when necessary.
	 * 
	 * Shiyong Lu
	 * July 10, 2018
	 *
	 **/
import java.util.regex.Pattern;
/**
 * Dataview table is meant to mimic the structure of a table in SQL
 * This was made so Relational Algebra operators could be held on it. 
 * @author Austin Abro
 *
 */
public class DATAVIEW_Table {
	static final String illegalHeaderCharsPrint = "! = < > : (space) | & ( ) \"";
	static final String illegalHeaderChars = "!=<>:|&()\" ";
	private String[] header; //Holds the names of each column
	private HashSet<List<String>> elements; // each entry into the vector is an array of rows
			
	 /** 
	 * Class constructor with header unknown.
	 */
	public DATAVIEW_Table(int numCol) // n is the number of columns
	{
		this.elements = new HashSet<List<String>>(); 
		this.header = new String[numCol]; 
	}
	/** 
	 * Class constructor with header.
	 */
	public DATAVIEW_Table(String[] header) // n is the number of columns
	{
		this.elements = new HashSet<List<String>>();
		this.setHeader(header); 
	}
	/** 
	 * gets the number of rows or tuples in the table
	 */
	public int getNumOfRows()
	{
		return elements.size();
	}
			
	/** 
	 * Gets the number of columns aka attributes
	 */
	public int getNumOfColumns()
	{
		return header.length;
	}
	
	public boolean hasHeader(String colName) 
	{
		for(int i = 0; i < this.header.length; i++) 
		{
			if (colName.contentEquals(this.header[i])) 
				return true;
		}
		return false;
	}
	
	/**
	 * Gets the position of a column
	 * @param colName the name of the column we are getting
	 * @return It's position in an array
	 * @throws IllegalArgumentException
	 */
	public int getColPosition(String colName) throws IllegalArgumentException
	{
		for(int i = 0; i < this.header.length; i++) 
		{
			
			if (colName.contentEquals(this.header[i])) 
				return i;
		}
		throw new IllegalArgumentException("Column " + colName +  " is not in the table");
		
	}		
	/**
	 * Checks if the character is an operator
	 * Used in the conditional parser to get the operand to use
	 * Used to make sure users don't put in an operator in their header
	 * @param ch
	 * @return
	 */
	public static boolean isOperator(int ch) 
	{
		if(ch == '<' || ch == '=' || ch == '>' || ch == '!')
			return true;
		else
			return false;
	}
	/**
	 * These are the illegal characters to put in a header. 
	 * Usually conflict with a spilt function or the conditional parser. 
	 * @param ch
	 * @return
	 */
	public static boolean fairCharHeader(int ch) 
	{
		for(int i = 0; i < illegalHeaderChars.length(); i++) 
		{
			if(ch == illegalHeaderChars.charAt(i))
				return false;
		}
			return true;
	}
	
	/**
	 * Adds a newrow to the table
	 * @param newrow List<String>
	 */
	public void appendRow(List<String> newrow)
	{
		elements.add(newrow);
	}
	/**
	 * changes the elements to a new set
	 * @param set
	 */
	public void setElements(HashSet<List<String>> set) 
	{
		this.elements = set;
	}
	
	/**
	 * Useful when you must loop over every element
	 * @return Entire set of elements. Set is a list of strings
	 */
	public HashSet<List<String>> getElements() 
	{
		return this.elements;
	}
	
	public HashSet<List<String>> getDeepCopyElements() 
	{
		HashSet<List<String>> copy = new HashSet<List<String>>();
		for(List<String> tempList: this.elements) 
		{
			String[] row = new String[tempList.size()];
			for(int i = 0; i < row.length; i++) 
				row[i] = tempList.get(i);		
			copy.add(Arrays.asList(row));
		}
		return copy;
	}
	
	/**
	 * Performs the projection operator.
	 * @param columns The String which will be the new header and the attributes we must get from the table 
	 * @throws IllegalArgumentException if the columns[] has an attribute which is not in the table
	 */
	public void project(String[] columns) throws IllegalArgumentException
	{
		int[] colPositions = new int[columns.length];
		for(int i = 0; i < colPositions.length; i++)
			colPositions[i] = getColPosition(columns[i]);
		HashSet<List<String>> set = new HashSet<List<String>>();
		for(List<String> tempList : this.elements) 
		{
			String[] temp = new String[colPositions.length];
			for (int i = 0; i < colPositions.length; i++) 
				temp[i] = tempList.get(colPositions[i]);
			set.add(Arrays.asList(temp));
		}
		this.setHeader(columns);
		setElements(set);
	}
	
	public static void logAndExit(String msg) 
	{
		System.out.println(msg);
		Dataview.debugger.logErrorMessage(msg);
		System.exit(0);
	}
	
	public static void logAndExit(Exception e) 
	{
		e.printStackTrace();
		Dataview.debugger.logException(e);
		System.exit(0);
	}
	
	/** 
	 * Creates the header for the table. 
	 *  If the user tries to add the same column twice an error message will be output and the program will exit.
	 *  If a user tries to add a character that is not allowed as checked by the fair Char header function an error message will show and the program will exit
	 *  If an empty name is input for the header the program will exit.  
	 *  @param header
	 */
	public void setHeader(String [] header) 
	{	
		for(int  i = 0; i < header.length; i++) 
		{
			if(header[i].contentEquals("")) 
				logAndExit("cannot have an empty name for an atribute"); 	
			for(int j = 0; j < header[i].length(); j++) 
			{
				if(!fairCharHeader(header[i].charAt(j))) 
					logAndExit("You have an illegal character in a header name. The Illegal characters are :" + illegalHeaderCharsPrint );
			}
			for(int j = 0; j < header.length; j++) 
			{
				if(header[i].contentEquals(header[j]) && j != i) 
					logAndExit("cannot create a header which has two attributes with the same name."
							+ " Consider using the rename operator while preforming a join or cartesian product");
			}
		}
		this.header = header;
	}
	/**
	 * 
	 * @return DATAVIEW_Table header
	 */
	public String [] getHeader() 
	{
		return this.header;
	}
	 /**
	  * Checks if two headers are the same in any order
	  * @param table1Header
	  * @param table2Header
	  * @return Returns true if the headers are the same in any order
	  * @throws IllegalArgumentException if the tables don't have the same number of columns. We do this instead of returning false so we can have a better error message
	  */
	public static boolean hasSameColumns(String[] table1Header,String[] table2Header) throws IllegalArgumentException 
	{	
		
		
		if(table1Header.length != table2Header.length)
			throw new IllegalArgumentException("Tables must have the same number of columns");
		return  Arrays.asList(table1Header).containsAll(Arrays.asList(table2Header));
	}
	
	/**
	 * Aligns the columns of the table which called this function with the table w
	 * @param aligner used to align the header
	 */
	public void alignColumns(DATAVIEW_Table aligner)
	{
		//This function alignes the columns of a table with another table assuming they have the same header
		//This function should never be used when align columns hasn't been used yet this is just to be safe 
		try 
		{
			if(!hasSameColumns(this.header,aligner.header)) 
				throw new IllegalArgumentException("Column names must be equal. You may use rename operator to accomplish this");
			if (aligner.getHeader().equals(this.getHeader()))
				return;
			String tempString;
			int switchPos = 0;
			for(int i = 0; i < aligner.header.length; i++) 
			{
				for (int thisCol = 0; thisCol < this.header.length; thisCol++) 
				{
					if(aligner.header[i].contentEquals(this.header[thisCol]))
						switchPos = thisCol;
				}
				this.header[switchPos] = this.header[i];
				this.header[i] = aligner.header[i];
				for(List<String> elements : this.elements) 
				{
					tempString = elements.get(switchPos);
					elements.set(switchPos, elements.get(i));
					elements.set(i, tempString);
				}
			}
		}
		catch(IllegalArgumentException e) 
		{
			logAndExit(e);
		}
		return;
		
	}
	
	/**
	 * aligns the columns so the lists are all in the proper order
	 * This function creates a union of the all the elements in a set
	 * @param other table won't be changed but the one calling the function will be changed.
	 */
	public void union(DATAVIEW_Table other) 
	{
		other.alignColumns(this);
		this.elements.addAll(other.elements);
	}
	/** 
	 * aligns the columns so the lists are all in the proper order
	 * Calculates the intersection of the tables 
	 * @param other table won't be changed but the one calling the function will be changed.
	 */
	public void intersection(DATAVIEW_Table other) 
	{
		other.alignColumns(this);
		this.elements.retainAll(other.elements);
	}
	
	/** 
	 * aligns the columns so the lists are all in the proper order
	 * Calculates the intersection of the tables 
	 * @param other table won't be changed but the table calling the function will be changed.
	 */
	public void setDifference(DATAVIEW_Table other) 
	{
		other.alignColumns(this);
		this.elements.removeAll(other.elements);
	}
	

	/**
	 * Creates a new table and calculates the cartesian product
	 * Strategy is to combine both headers then loop through every tuple in the first table with every tuple in the second table
	 * @param table1
	 * @param table2
	 * @return a new table
	 */
	public static DATAVIEW_Table cartesianProduct(DATAVIEW_Table table1,DATAVIEW_Table table2) 
	{
		int newNumOfCol = table1.getNumOfColumns() + table2.getNumOfColumns();
		String[] newHeader = new String[newNumOfCol];
		String[] table1Header = table1.getHeader();
		String[] table2Header = table2.getHeader();
		int i;
		int colPos1;
		//Get both headers to set the new header of the table.
		
		for(i = 0; i < table1Header.length; i++)
			newHeader[i] = table1Header[i];
		for(int j = 0; j < table2Header.length; j++)
			newHeader[i + j] = table2Header[j];
		DATAVIEW_Table newTable = new DATAVIEW_Table(newHeader);
		//Go through both tables and for each entry in table1 create a new entry in new table with every single entry. 
		for(List<String> elements1 : table1.getElements()) 
		{	
			String[] table1Row = new String[newNumOfCol];
			for(colPos1 = 0; colPos1 < table1.getNumOfColumns(); colPos1++)
				table1Row[colPos1] = elements1.get(colPos1);
			
			for(List<String> elements2 : table2.getElements()) 
			{
				String[] newRow = table1Row.clone();	
				for(int colPos2 = 0; colPos2 < table2.getNumOfColumns(); colPos2++) 
					newRow[colPos1 + colPos2] = elements2.get(colPos2);
				newTable.appendRow(Arrays.asList(newRow));
			} 
		}
		return newTable;
	}
	
	/**
	 * Creates a copy of a table with a shallow copy of the elements and deep copy of the header. 
	 * @return the new table
	 */
	public DATAVIEW_Table copy() 
	{
		DATAVIEW_Table clone = new DATAVIEW_Table(this.header.clone());
		clone.elements.addAll(this.elements);
		return clone;
	}

    @Override
    public String toString() 
	{
		String str = "";
		for(int i = 0; i < header.length; i++) 
		{
			if(i == 0)
				str += header[0];
			else
				str += "," + header[i];
		}
		str += "\n";
		for(List<String> row: elements) {
			str = str + row.get(0); // append column 0
			for(int j=1; j<this.header.length; j++) 
			{
				str += "," + row.get(j);
			}
				
			str = str + "\n";
		}
		
		return str;
	}

		
}
