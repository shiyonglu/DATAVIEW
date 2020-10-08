package dataview.models;

/**
 * The JSONParser class is used to covert text to a JSONValue, which is used between the Workflow Executor and task Executor. 
 * @author changxinbai
 *
 */

public class JSONParser {
		private String str;
		private int length = 0;
		private int pos;
		private int ch;
		
		public JSONParser(String str){
			this.str = str;
			length = str.length();
			pos = -1;     // position of the current char for parsing
			nextChar();
		}
		
		
		/*
		 * Return the next character if it exists, otherwise return -1;
		 */
		void nextChar()
		{
			ch = (++pos < length? str.charAt(pos):-1);
		}
		
		int lookAhead()
		{
			while(pos < length-1) {
				if(ch == '{' || ch == '"' || ch == '[') break;
				nextChar();
			}
			if(pos < length)
			    return ch;
			else 
				return -1;
		}
		
		/* if the next character is as expected, then skip it and return true, otherwise, return false */
		boolean eat(int chartoeat){
			while(ch ==' ' || ch == '\r' || ch == '\n') nextChar();
			if(ch == chartoeat) {
				nextChar();
				return true;
			}
			else {
				//System.out.println("Expected "+(char)chartoeat+" but found "+(char)ch);
				//Dataview.debugger.logErrorMessage("Expected "+(char)chartoeat+" but found "+(char)ch);
				return false;
			}
		}
		
		
		/* We expect to see a string here. */
		private String parseString() {
			//System.out.println("Parse a string now");
		    if(!eat('"')){
		    	Dataview.debugger.logErrorMessage("A JSON string should start with a quote.");
		    	return null;
		    }
		    	
			int startPos = this.pos;
			while(ch != '"' && pos < length-1 ) nextChar();
			
			if(ch != '"'){ 
		    	Dataview.debugger.logErrorMessage("A JSON string should end with a quote.");
		    	return null;
		    }
			
			String result = str.substring(startPos, pos);
			eat('"'); // skip the quote
			
			//System.out.println("got a string: " + result);
					
		    return result;				
		} // end parseString
			
		
		/* this is the main entry */
		public JSONObject parseJSONObject() {
			String key; 
			JSONValue value;
			JSONObject obj = new JSONObject();
		
			//System.out.println("parse a JSONObject");
			
			
		    if(!eat('{')){
		    	Dataview.debugger.logErrorMessage("A JSONObject should start with a {.");
		    	return null;
		    }

		    
			
			while((key=parseString()) != null){
				if(!eat(':')) {
					//System.out.println("okokokok");
					//System.out.println("pos:"+pos);
					Dataview.debugger.logErrorMessage("A key-value pair must be separated by :");;
					break;
				}
		
			   value = parseJSONValue();
			   
			   obj.put(key, value);
			   if(!eat(',')) break; // do we another key-value pair?
			}
			
		    if(!eat('}')){
		    	Dataview.debugger.logErrorMessage("A JSONObject should end with a }.");
		    	//return null;
		    }

			//System.out.println("Got a jason object:\n" + obj);
			return obj;				
		} 

		/* this is the main entry */
		public JSONArray parseJSONValueArray() {
			JSONArray array = new JSONArray();
			JSONValue value = null;
		
			//System.out.println("parseJSONValueArray....");
		    if(!eat('[')){
		    	Dataview.debugger.logErrorMessage("A JSON array should start with a [.");
		    	return null;
		    }

		    if(eat(']')){ // we found an empty array
		    	return array;
		    }
			
			while((value = parseJSONValue()) != null){
				//System.out.println("Found a array value*********\n:"+ value);
			   array.add(value);
			   if(!eat(',')) break; // do we another key-value pair?
			}
			
		    if(!eat(']')){
		    	Dataview.debugger.logErrorMessage("A JSONObject should end with a ].");
		    	return null;
		    }

		    //System.out.println("got an array:\n" + array);
			return array;				
		} 

		
		
		/* if the next char is a double quote, it is a string, or if the next char is a {, then it 
		 * is an JSONObject, or if the next char is a [, then it is an array, otherwise invalude JSONValue 
		 */
		private JSONValue parseJSONValue()
		{
			//System.out.println("333333333333333");
			 if(lookAhead() == '"'){ // then we know it is a String
				    //System.out.println("case one:");
					return new JSONValue(parseString());
			 }
			 else if(lookAhead() == '{') {
				 //System.out.println("case two:");
				 return new JSONValue(parseJSONObject());
			 }
			 else if(lookAhead() == '[') {
				 //System.out.println("case three");
			 	 return new JSONValue(parseJSONValueArray());
			 }
			 else {
				 //System.out.println("case four:");
				 Dataview.debugger.logErrorMessage("Invalid JSONValue, see character:" + ch);
				 return null;
			 }		 
		}
}
