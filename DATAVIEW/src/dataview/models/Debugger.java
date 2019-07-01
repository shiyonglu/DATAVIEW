package dataview.models;

public class Debugger {
	private Logger mylogger;
	
	Debugger(String logfile){
		mylogger = new Logger(logfile);
		mylogger.setLevel(Logger.LOGALL);
	}

	public void setLevel(int newlevel)
	{
		mylogger.setLevel(newlevel);
	}
	
	public void setDisplay(boolean on) 
	{ 
		mylogger.setDisplay(on); 
	}
	
	// during debugging, let's check if an object is a null pointer 
	// level is 0 as null pointer might cause crash
	public void logNullPointer(String objname, Object o) {
		if(o == null) {
			mylogger.append(0, objname + " is a null pointer");
		}
	}
	
	/* log all exception of Java here, level 1 */
	public void logException(Exception e) {
		mylogger.append(1, "Exception: " + e.getMessage());
	}
	
	/* according to the logic of the program, if this line is called, then there is some logical error in the program.
	 * level 2, DATAVIEW will not crash, but it might not run correctly or with less performance
	 * this is usually caused by the wrong if-else statement.
	 * the msg must be unique (includes information such as  which source file, class, and method and which line 
	 * in the source code) in the whole program so that dubugging can find out which part of the program has this logical error.
	 */
	public void logErrorMessage(String msg) {
		mylogger.append(2, "Error message: " + msg);
	}
	
	// check the value of a variable, level is 3, we are just curious about its value
	public void logObjectValue(String objname, Object o) {
		if(o == null) {
			mylogger.append(0, objname+" is a null pointer");
		}
		else {			
			mylogger.append(3, "The value for variable/object:"+objname+" is " + o.toString());
		}
	}
	
	// check if a given condition whether a condition is false, if false, then we need to log the condition.
	public void logFalseCondition(String constr, boolean con) {
		if(con == false) {
			mylogger.append(1, constr);
		}

	}	
	
	public void logSuccessfulMessage(String msg) {
		mylogger.append(3, "We reached here:  " + msg);
	}
	
	public void logTestATask(String taskName, String [] inputFileNames, String [] outputFileNames)
	{
		Task t1 = null;
		
		
		// instantiate a new task, use the empty constructor since TaskExecutor will invoke each task exactly in the same way.
		try {
			Class<?> taskclass = Class.forName(taskName);	
			t1 =  (Task) taskclass.newInstance();
		} catch(ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
		
		// from the database we know how many inputports and outputports this task has.
		Dataview.debugger.logFalseCondition("The number of input file names is not equal to the nubmer of input ports of this task: "+taskName,  inputFileNames.length == t1.ins.length);
		Dataview.debugger.logFalseCondition("The number of output file names is not equal to the nubmer of output ports of this task: "+taskName,  outputFileNames.length == t1.outs.length);
	
		for(int i=0; i<inputFileNames.length; i++)
			t1.ins[i].setLocation(inputFileNames[i]);
		
		for(int i=0; i<inputFileNames.length; i++)
			System.out.println("filenanme*****: " + t1.ins[i].getFileName());
		
		
		for(int i=0; i<outputFileNames.length; i++)
			t1.outs[i].setLocation(outputFileNames[i]);
		
		
		t1.run();
		
		System.out.println("Task: "+ taskName + "'s  execution is completed, check out the output files for the results.");
	}
}	
	

