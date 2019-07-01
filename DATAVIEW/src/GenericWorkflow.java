
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataview.models.*;

/**
 * The class is extended from the Workflow and be used to create a workflow instance from web bench in Medator 
 */
public class GenericWorkflow extends Workflow {
	Document spec = null;
	String location = null;
	
	/**
	 * The constructor is used to initialize the user's person content location and the Document object generated from 
	 * a runnable workflow mxGraph information  
	 */
	public GenericWorkflow(Document sepc,String location){
		super("GeneticWorkflow", "This is the generic workflow used to create a workflow from web bench");	
		this.spec = sepc;
		this.location = location;
	}
	
	/**
	 * An overriding method to design a workflow from Document object from mxGraph
	 */
	
	public void design()
	{
		List<String> tasksName = new ArrayList<String>();
		List<String> tasksId = new ArrayList<String>();
		
		NodeList wfInstances = spec.getElementsByTagName("workflowInstance");
	 	NodeList wfTasks = spec.getElementsByTagName("workflow");
	 	
	 	System.out.println("In the design method " + location);
	 	//get number of workflow tasks
	 	for (int idx = 1; idx <= wfInstances.getLength(); idx++) {
	 		tasksName.add(wfTasks.item(idx-1).getTextContent());
		 	Node InstancesNode = wfInstances.item(idx-1);
		 	tasksId.add(InstancesNode.getAttributes().item(0).getNodeValue());
	 	}
	 	
		// create and add all the tasks
	 	List<Task> tasks = new ArrayList<Task>();
		
		for(int i = 0; i < tasksName.size(); i ++){
			Task t = addTask(tasksName.get(i),location);
			tasks.add(t);
		}	
		// add the input file connection with task
		  NodeList input2task = spec.getElementsByTagName("inputDP2PortMapping");	
		  wins = new Object[input2task.getLength()];
	 	   for (int i = 0; i < input2task.getLength(); i ++){
	 		   Element mapping = (Element) input2task.item(i);
	 		   String from = mapping.getAttribute("from");
	 		   String to = mapping.getAttribute("to");
	 		   String filename = from.substring(from.lastIndexOf("/")+1);
	 		   String taskid = to.substring(0,to.indexOf("."));
	 		   String portid = to.substring(to.length()-1);
	 		   int index = tasksId.indexOf(taskid);
	 		   wins[i] = new DATAVIEW_BigFile(filename);
	 		   addEdge(i, tasks.get(index) , Integer.parseInt(portid));      
	 	   }
	 	   	
		// add the tasks connection
	 	  NodeList task2task = spec.getElementsByTagName("dataChannel");	
	 	 for (int i = 0; i < task2task.getLength(); i ++){
	 		   Element mapping = (Element) task2task.item(i);
	 		   String from = mapping.getAttribute("from");
	 		   String to = mapping.getAttribute("to");
	 		   String fromtaskid = from.substring(0,from.indexOf("."));
	 		   String fromportid = from.substring(from.length()-1);
	 		   int fromindex = tasksId.indexOf(fromtaskid);
	 		   
	 		   String totaskid =  to.substring(0,to.indexOf("."));
	 		   String toportid = to.substring(to.length()-1);
	 		   int toindex = tasksId.indexOf(totaskid);
	 		  
	 		   addEdge(tasks.get(fromindex), Integer.parseInt(fromportid) ,tasks.get(toindex) ,Integer.parseInt(toportid));      
	 	   }
	 	 
	 	 // add the output files connection
	 	 NodeList task2output = spec.getElementsByTagName("outputDP2PortMapping");	
	 	 wouts = new Object[task2output.getLength()];
	 	 for (int i = 0; i < task2output.getLength(); i ++){
	 		   Element mapping = (Element) task2output.item(i);
	 		   String from = mapping.getAttribute("from");
	 		   String to = mapping.getAttribute("to");
	 		  String fromtaskid = from.substring(0,from.indexOf("."));
	 		   String fromportid = from.substring(from.length()-1);
	 		   int fromindex = tasksId.indexOf(fromtaskid);
	 		   String outputfile = to + ".txt";
	 		   wouts[i] = new DATAVIEW_BigFile(outputfile);
	 		   addEdge(tasks.get(fromindex) , Integer.parseInt(fromportid), i);      
	 	   }
	 	 
	}
	
}
