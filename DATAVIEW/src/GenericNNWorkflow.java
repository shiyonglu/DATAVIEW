
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.teamlog.TimeUnit;

import dataview.models.*;

/**
 * The class is extended from the Workflow and be used to create a workflow instance from web bench in Mediator
 * author: Junwen Liu 
 */
public class GenericNNWorkflow extends NNWorkflow {
	Document spec = null;
	String location = null;
	String token = null;
	
	/**
	 * The constructor is used to initialize the user's person content location and the Document object generated from 
	 * a runnable workflow mxGraph information  
	 */
	public GenericNNWorkflow(Document sepc,String location, String token){
		super("GeneticNNWorkflow", "This is the generic NNworkflow used to create a NNWorkflow from web bench");	
		this.spec = sepc;
		this.location = location;
		this.token = token;
	}
	
	public void downloadFileFromDropBox(String filename, String inputfilepath) {
		if(!token.isEmpty()){
				DbxRequestConfig config = new DbxRequestConfig("en_US");
				DbxClientV2 client = new DbxClientV2(config, token);
				DbxDownloader<FileMetadata> dl = null;
				try {
				dl = client.files().download("/DATAVIEW-INPUT/"+filename);
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				FileOutputStream fOut = null;
			try {
				fOut = new FileOutputStream(inputfilepath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				try {
				dl.download(fOut);
			} catch (DbxException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
	}
	
	/**
	 * An overriding method to design a workflow from Document object from mxGraph
	 */
	
	public void design()
	{
		List<String> tasksName = new ArrayList<String>();
		List<String> tasksId = new ArrayList<String>();
		Map<String, String> tasksMap = new HashMap<String, String>();
		
		// add the input file 
		  NodeList input2task = spec.getElementsByTagName("inputDP2PortMapping");	
		  wins = new Object[input2task.getLength()];
	 	   for (int i = 0; i < input2task.getLength(); i ++){
	 		   Element mapping = (Element) input2task.item(i);
	 		   String from = mapping.getAttribute("from");
	 		   String filename = from.substring(from.lastIndexOf("/")+1);
	 		   wins[i] = filename;  
	 		   
	 		   // download the input file from dropbox
	 		  String inputfilepath =  location + "\\" + filename;
	 		  downloadFileFromDropBox(filename, inputfilepath);
	 		   
	 		  // download TrainerDLLs from dropbox
	 		  inputfilepath = location + "\\TrainerDLLs\\";
	 		  File directory = new File(inputfilepath);
	 		  if(!directory.exists()) {
	 			  directory.mkdir();
	 			  downloadFileFromDropBox("jsoncpp.dll", inputfilepath + "jsoncpp.dll");
		 		  downloadFileFromDropBox("maintest.dll", inputfilepath + "maintest.dll");
		 		  try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	 		  }
	 	   }
	 	   
	 	
	 	// add the output file
		 	 NodeList task2output = spec.getElementsByTagName("outputDP2PortMapping");	
		 	 wouts = new Object[task2output.getLength()];
		 	 for (int i = 0; i < task2output.getLength(); i ++){
		 		   Element mapping = (Element) task2output.item(i);
		 		   String to = mapping.getAttribute("to");
		 		   String outputfile = to + ".txt";
		 		   wouts[i] = outputfile;     
		 }
		
		NodeList wfInstances = spec.getElementsByTagName("workflowInstance");
	 	NodeList wfTasks = spec.getElementsByTagName("workflow");
	 	
	 	System.out.println("In the design method " + location);
	 	//get number of workflow tasks
	 	for (int idx = 1; idx <= wfInstances.getLength(); idx++) {
	 		tasksName.add(wfTasks.item(idx-1).getTextContent());
		 	Node InstancesNode = wfInstances.item(idx-1);
		 	tasksId.add(InstancesNode.getAttributes().item(0).getNodeValue());
		 	//add id:name key-value pair in tasksMap 
		 	tasksMap.put(InstancesNode.getAttributes().item(0).getNodeValue(), wfTasks.item(idx-1).getTextContent());
	 	}
	 	
	 	
	 	Map<String, String> channelMap = new HashMap<String, String>();
	 	
	 	NodeList task2task = spec.getElementsByTagName("dataChannel");
	 	for (int i = 0; i < task2task.getLength(); i ++){
	 		Element mapping = (Element) task2task.item(i);
	 		String from = mapping.getAttribute("from");
	 		String to = mapping.getAttribute("to");
	 		String fromTaskId = from.substring(0,from.indexOf("."));
	 		String toTaskId =  to.substring(0,to.indexOf("."));
//	 		String fromTaskName = tasksMap.get(fromTaskId);
//	 		String toTaskName = tasksMap.get(toTaskId);
	 		channelMap.put(fromTaskId, toTaskId);
	 	}
	 	
	 	//get the first NNTask in all dataChannel by filtering the task only appear in key but not in value
	 	Set<String> keys = new HashSet<String>(channelMap.keySet());
	 	Set<String> values = new HashSet<String>(channelMap.values());
	 	keys.removeAll(values);
	 	String firstNNTask = new ArrayList<>(keys).get(0);
	 	String firstNNTaskName = tasksMap.get(firstNNTask);
	 	
	 	NNTask[] layers = new NNTask[tasksName.size()];
	 	
	 	layers[0] = addNNTask(firstNNTaskName, location);
	 	String currentNNTask = firstNNTask;
	 	int index = 1;
	 	while(channelMap.get(currentNNTask) != null) {
	 		layers[index] = addNNTask(tasksMap.get(channelMap.get(currentNNTask)), location);
	 		currentNNTask = channelMap.get(currentNNTask);
	 		index++;
	 	}
	 	
	 	Sequential(layers);
	 	 
	}
	
}
