import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import dataview.models.DATAVIEW_Table;
import dataview.models.Dataview;

/**
 * Conditonal parser used to parse conditions and build tables 6 relational algebra operators
 * select,conditionalJoin,naturalJoin,leftOuterJoin,rightOuterJoin,fullOuterJoin
 * @author Austin Abro
 *
 */
public class ConditionParser {
	public enum RAOperator
	{select,conditionalJoin,naturalJoin,leftOuterJoin,rightOuterJoin,fullOuterJoin};
	public final String formatError = "Invalid string value: ";
	private String formula;
	private DATAVIEW_Table initalTable;
	private RAOperator useRAOperator; //All operators use this parser in a their own way so the enum will give the ability to differenate them. 
	private int outerJoinSeperator; //This will be where the operator is spilt up when we attempt an outerJoin. So if one table takes the table from 0-4 and the next from 5-8 this will be 5;
	private int pos;
	private int ch;
	HashMap <String,String> flipMap;
	
	ConditionParser(String formula, DATAVIEW_Table table, RAOperator RAoperator, int outerJoinSeperator)
	{
		this.formula = formula.replace(" ", "").replace("\n","");
		this.initalTable = table;
		this.pos = -1;
		nextChar();
		this.useRAOperator = RAoperator;
		this.outerJoinSeperator = outerJoinSeperator;
		initateMap();
	}
	/**
	 * Initates a hashMap. This map will be used to both flip the operator when the operator is negative 
	 * while also checking if the operator the user input was correct
	 */
	public void initateMap()
	{ 
		flipMap = new HashMap<String,String>();
		flipMap.put("<",">=");
		flipMap.put("<=",">");
		flipMap.put(">","<=");
		flipMap.put(">=","<");
		flipMap.put("==","!=");
		flipMap.put("!=","==");
	}
	/**
	 * Flips the operator using the hashMap initalized above
	 * @param operator
	 * @return
	 * @throws IllegalArgumentException
	 */
	public String flipOperator(String operator) throws IllegalArgumentException   
	{
		if(!flipMap.containsKey(operator))
			throw new IllegalArgumentException(formatError + "operator doesn't exist");
		return flipMap.get(operator);
	}
	
	/**
	 * NextChar. Useful for avoiding edge cases when you want to use formula.charAt but it will throw a string out of bounds exception
	 */
	void nextChar() {
        ch = (++pos < formula.length()) ? formula.charAt(pos) : -1;
    }
	/**
	 * Useful for & and | since they will always be used twice in a row for the and and or operators respectively
	 * @param charToEat
	 * @return true
	 */
	boolean doubleEat(int charToEat) 
	{ 
		if(ch == charToEat) {
			nextChar();
			if(ch == charToEat) {
				nextChar();
				return true;
			}
			else
				DATAVIEW_Table.logAndExit("the char " + (char)charToEat + " should always be used twice in a row");	
		}
		return false;
	}
	/**
	 * Checks if a character is there. If it is it returns true and goes to the next character otherwise returns false. 
	 * @param charToEat
	 * @return
	 */
	boolean eat(int charToEat) {
        if (ch == charToEat) {
            nextChar();
            return true;
        }
        return false;
    }
	/**
	 * Checks for a digit character so we can make sure the double is paraseable. 
	 * @param theChar
	 * @return
	 */
	public static boolean digitChar(int theChar){
		if(Character.isDigit(theChar) || theChar == '.' || theChar == '-')
			return true;
		else
			return false;
	}
	/**
	 * Gets a string which was meant to be a double value 
	 * @param startPos
	 * @return
	 * @throws IllegalArgumentException
	 */
	public String getValue(int startPos) throws IllegalArgumentException{
		String currentVal;
		while(digitChar(ch) || ch == '+') {nextChar(); }
		currentVal = formula.substring(startPos,pos);
		return currentVal;
	}
	/**
	 * Gets a string value
	 * @param startPos
	 * @return
	 * @throws IllegalArgumentException
	 */
	public String getString(int startPos) throws IllegalArgumentException {
		String currentVal; 
		while(ch != '"' && pos < formula.length()) {nextChar();}
		if(ch != '"')
			throw new IllegalArgumentException(formatError + "Surround your value with quotation marks");
		currentVal = formula.substring(startPos, pos);
		return currentVal;
	}
	/**
	 * Gets a header value. 
	 * @param startPos
	 * @return
	 */
	public String getAttribute(int startPos) {
		String currentCol;
		while(DATAVIEW_Table.fairCharHeader(ch) && pos < formula.length()) {nextChar();}
		currentCol = formula.substring(startPos, pos);
		return currentCol;
	}
	
	/**
	 * Simple function to either make the left or right side of a tuple null.
	 * This is used for the left or right join
	 * @param tempList
	 * @param loopStart
	 * @param loopEnd
	 */
	public void nullHalfRow(List<String> tempList, int loopStart, int loopEnd, DATAVIEW_Table table) 
	{
		for(int i = loopStart; i < loopEnd; i++) 
			tempList.set(i, null);
		table.appendRow(tempList);
	}
	
	/**
	 * Makes a left join or right join table
	 * @param currentCol
	 * @param operator
	 * @param col
	 * @param leftJoin
	 * @return  DATAVIEW_TABLE: the table to continue the recursion
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 */
	public DATAVIEW_Table makeOuterJoinTable(String currentCol, String operator, String col, boolean leftJoin) throws NumberFormatException, IllegalArgumentException
	{
		String[] header = initalTable.getHeader();
		DATAVIEW_Table table = new DATAVIEW_Table(header);
		int colPos = initalTable.getColPosition(currentCol);
		int otherPos = initalTable.getColPosition(col);
		int loopStart = leftJoin ? outerJoinSeperator : 0;
		int loopEnd = leftJoin ? header.length : outerJoinSeperator;
		HashSet<List<String>> fullList = initalTable.getDeepCopyElements();
		for(List<String> tempList: fullList) 
		{
			col = tempList.get(otherPos);
			String table1Val = tempList.get(colPos);
			if(operator.contentEquals("<")) 
			{
				if(Double.parseDouble(table1Val) < Double.parseDouble(col)) 
					table.appendRow(tempList);
				else 
					nullHalfRow(tempList, loopStart, loopEnd, table);
			}
			else if(operator.contentEquals("<=")) 
			{
				if(Double.parseDouble(table1Val) <= Double.parseDouble(col))
					table.appendRow(tempList);
				else 
					nullHalfRow(tempList, loopStart, loopEnd, table);
			}
			else if(operator.contentEquals(">")) 
			{
				if(Double.parseDouble(table1Val) > Double.parseDouble(col))
					table.appendRow(tempList);
				else 
					nullHalfRow(tempList, loopStart, loopEnd, table);
			}
			else if(operator.contentEquals(">=")) 
			{
				if(Double.parseDouble(table1Val) <= Double.parseDouble(col))
					table.appendRow(tempList);
				else 
					nullHalfRow(tempList, loopStart, loopEnd, table);	
			}
			else if(operator.contentEquals("==")) 
			{
				if(table1Val.contentEquals(col))
					table.appendRow(tempList);
				else 
					nullHalfRow(tempList, loopStart, loopEnd, table);
			}
			else if(operator.contentEquals("!=")) 
			{
				if(!table1Val.contentEquals(col))
					table.appendRow(tempList);
				else 
					nullHalfRow(tempList, loopStart, loopEnd, table);	
			}	
		}
		return table;
	}
	/**
	 * Makes the table that will be returned from a single selection statement
	 * Uses the Enums to make a slight change depending on the operator.
	 * Conditonal and natural join check two columns for the table. The table has both products since it was made with the Cartesian product
	 * @param currentCol
	 * @param operator
	 * @param colOrVal
	 * @return
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 */
	public DATAVIEW_Table makeTable(String currentCol, String operator, String colOrVal) throws NumberFormatException, IllegalArgumentException
	{
		DATAVIEW_Table table = new DATAVIEW_Table(initalTable.getHeader());
		int colPos = initalTable.getColPosition(currentCol);
		int otherPos = -1;
		if(useRAOperator != RAOperator.select)
			otherPos = initalTable.getColPosition(colOrVal);
		for(List<String> tempList: initalTable.getElements()) 
		{
			if(useRAOperator != RAOperator.select)
				colOrVal = tempList.get(otherPos);
			String table1Val = tempList.get(colPos);
			if(operator.contentEquals("<")) 
			{
				if(Double.parseDouble(table1Val) < Double.parseDouble(colOrVal))
					table.appendRow(tempList);	
			}
			else if(operator.contentEquals("<=")) 
			{
				if(Double.parseDouble(table1Val) <= Double.parseDouble(colOrVal))
					table.appendRow(tempList);
			}
			else if(operator.contentEquals(">")) 
			{
				if(Double.parseDouble(table1Val) > Double.parseDouble(colOrVal))
					table.appendRow(tempList);
			}
			else if(operator.contentEquals(">=")) 
			{
				if(Double.parseDouble(table1Val) >= Double.parseDouble(colOrVal))
					table.appendRow(tempList);
			}
			else if(operator.contentEquals("==")) 
			{
				if(table1Val.contentEquals(colOrVal))
					table.appendRow(tempList);
			}
			else if(operator.contentEquals("!=")) 
			{
				if(!table1Val.contentEquals(colOrVal))
					table.appendRow(tempList);
			}
		}
		return table;
	}
	
	public String getOperator(int startPos, boolean positive) 
	{	
		while(DATAVIEW_Table.isOperator(ch) && this.pos <= startPos + 2) {nextChar();}
		String operator =  formula.substring(startPos, pos);
		if(positive == false)
			operator = flipOperator(operator);	
		else if(!(flipMap.containsValue(operator)))
				throw new IllegalArgumentException(formatError + "Operator is incorrect");
		return operator;
	}
	
	public DATAVIEW_Table whereToSetup(String currentCol,String operator, String colOrVal) 
	{
		switch(useRAOperator) 
		{
		//First three case statements will go to the same spot in code. This will be what we have been doing so far
		//Next three case statements, the outer join will go to different spots in code and will be a new spot to deal with it. 
		case select:
		case conditionalJoin:
		case naturalJoin:
			return makeTable(currentCol,operator,colOrVal);
		case leftOuterJoin:
			return makeOuterJoinTable(currentCol,operator,colOrVal, true);
		case rightOuterJoin:
			return makeOuterJoinTable(currentCol,operator,colOrVal, false);
		case fullOuterJoin:
			DATAVIEW_Table table = makeOuterJoinTable(currentCol,operator,colOrVal, true);    
			table.union(makeOuterJoinTable(currentCol,operator,colOrVal, false));
			return table;
		default:
			Dataview.debugger.logErrorMessage("need to send in the correct val for Enum");
			System.exit(0);
			return null;
		}	
	}
		
	
	public DATAVIEW_Table parseOr(boolean positive) 
	{
		DATAVIEW_Table table = parseAnd(positive); //Before checking for union will take 
        while(true) { //Will always resolve after one loop since the second loop will not have eat('|')
            if(doubleEat('|')) 
            {
            	if(positive) //We are ultizing de-morgans law here
            		table.union(parseAnd(positive)); //union lower precedence
            	else
            		table.intersection(parseAnd(positive));
            } 
            else return table;
        }
    }

    public DATAVIEW_Table parseAnd(boolean positive) {
    	DATAVIEW_Table table = parseCondition(positive);
        while(true) { 
            if(doubleEat('&')) 
            {
            	if(positive)
            		table.intersection(parseCondition(positive)); //intersection higher precedence
            	else
            		table.union(parseCondition(positive));
            }
			else return table;
        }
    }	
    
	//Has the parameter of boolean lastPositive because We want to check whether or not  
	public DATAVIEW_Table parseCondition(boolean positive) 
	{
		//Basic Concept: Table_WS, DeMorgans law, Union = OR, Intersection = AND, recurse through starting from lower precendence calling higher precendence
		//Idea + flow through 
		//1. Start at ParseOr
		//2. parseOr call parseAnd through indirect recursion. First stack has parseOr at the start
		//3. parseAnd will call parseCondition and will wait for it to be resolved 
		//4. parseCondition will then initalize the table 
		//5. We will then look for exclaimation points and parenthese. An exclaimation point without a parentese is not allowed
		//6. Rest of parseCondition will execute and the proper selection will be applied to the column using the initalTable this class was initalized with
		//If table_ws.isPositive = false the table is negative and we add in all the rows we normally wouldn't
		//7. Goes back to parseAnd. If the table is still in negative state then we will do union instead of intersection. 
		//This is because of de-morgans law. If there is a negative before a parenthesis with more than two states I can make both tables negative 
		//and flip union and intersection
		//8. Same with ParseOr. Since they both call parseConditoin again it will continue to look for  
		//9. Returned to user
		DATAVIEW_Table table = new DATAVIEW_Table(initalTable.getHeader().clone());
		try {
			if(eat('!')) 
			{
				positive = !positive;
				if(eat('(')) 
				{
					table = parseOr(positive);
					eat(')'); 
				}
				else
					throw new IllegalArgumentException(formatError + ". Negation can only be used directly outside of parenthesis");
			}
			else if (eat('('))
			{
				table = parseOr(positive);
				eat(')');
			}
			else  
			{
				String currentCol = "";
				String operator = "";
				String colOrVal = "";
				int startPos = this.pos;
				if(DATAVIEW_Table.fairCharHeader(ch))
					currentCol = getAttribute(startPos);
				startPos = this.pos;
				if(DATAVIEW_Table.isOperator(ch))
					operator = getOperator(startPos, positive);
				else
					throw new IllegalArgumentException(formatError + "Operator is incorrect");
				startPos = this.pos;
				if(useRAOperator == RAOperator.select && (operator.contentEquals("==") || operator.contentEquals("!="))) 
				{
					if(eat('"')) 
					{ 
						startPos = this.pos;
						colOrVal = getString(startPos);
						eat('"');
					} 
					else if(digitChar(ch) || ch== '+') {
						if(eat('+'))
							startPos = this.pos;
						colOrVal = getValue(startPos);
					}
						
				}
				else if (useRAOperator == RAOperator.select && (operator.contentEquals("<=") || operator.contentEquals("<") 
						|| operator.contentEquals(">") || operator.contentEquals(">="))) 
				{
					colOrVal = getValue(startPos);
				}
				else if (DATAVIEW_Table.fairCharHeader(ch))
				{
						colOrVal = getAttribute(startPos);
				}
				else
					throw new IllegalArgumentException(formatError + "Make sure string comparisions have quations and you are using ");
				
				System.out.println(currentCol + " " + operator + " " + colOrVal);
				table = whereToSetup(currentCol,operator,colOrVal);
			}
			return table;
		}
		catch(IllegalArgumentException e) 
		{
			DATAVIEW_Table.logAndExit(e);
		}
		return null;
	}
}


