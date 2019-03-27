package dataview.models;

/**
 *  Create some global objects for the whole system to use, such as debugger
 *  now people can call Dataview.debugger.
 */

public class Dataview {
	
	public static final Debugger debugger = new Debugger("dataview.log");
	public static final Debugger result = new Debugger("result.txt");
}
