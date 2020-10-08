package dataview.models;
import java.io.BufferedWriter;
import dataview.models.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* We are using the PROV-DM model:
 * https://www.w3.org/TR/prov-dm/
 * W3C Recommendation 30 April 2013
 * 
 *  The PROV-DM model has thee kinds of nodes: entities (model data products), activities (model workflow tasks), and agents (model 
 *  users who execute the workflow tasks, and seven kinds of edges: used, wasGeneratedBy, wasDerivedFrom, wasInformedBy, 
 *  wasAssociatedWith, wasAttibutedTo, and ActedOnBehalfOf.
 *  
 * 
 */
public class ProvenanceGraph {
	public String workflowName;  // which workflow is executed, each workflowName is a unique identifier in the whole DATAVIEW cycle
	public String workflowRunID; // a unique id to identify this particular workflow run, a unique identifier in the whole DATAVIEW cycle
	public List<String> myEntities; // all data products for this workflow run, each has a unique identifier in the whole DATAVIEW cycle
	public List<ProvenanceNode> myActivities; // all task runs for this workflow run, each has a unique identifer3 in the whole DATAVIEW cycle
	public List<String> myAgents;     // all users for this workflow run, probably only one user unless it is a collaborative workflow
	public List<ProvenanceEdge> myEdges;
	private String provname;
	
	public ProvenanceGraph(String workflowName, String workflowRunID){
		this.workflowName = workflowName;
		this.workflowRunID = workflowRunID;
		this.myEntities = new ArrayList<String>();
		this.myActivities = new ArrayList<ProvenanceNode>();
		this.myAgents  = new ArrayList<String>();
		this.myEdges  = new ArrayList<ProvenanceEdge>();
		this.provname = workflowName+"_"+workflowRunID+".prov";
	}

	// a new data product (a new file) is generated
	public void addEntity(String en)
	{
		myEntities.add(en);
	}

	// a new task run is initiated
	public void addActivity(ProvenanceNode act)
	{
		myActivities.add(act);
	}
	
	// a new user is running a task
	public void addAgent(String ag)
	{
		myAgents.add(ag);
	}

	// transfer edge 
	public void addEdge_Transfer(String src, String dest, double transfertime){
		
	}
	
	
	// edge type 1:  
	public void addEdge_Used(String dest, String src, int inputPortIndex)
	{
		if(getIndexOfEntity(dest) == -1) myEntities.add(dest);
		if(getIndexOfActivity(src) == -1) myActivities.add(new ProvenanceNode(src));
		myEdges.add(new ProvenanceEdge("Used", dest,  src,  inputPortIndex));
	}
	
	
	
	// edge type 2: 
	public void addEdge_WasGeneratedBy(String dest, String src, int outputPortIndex)
	{
		if(getIndexOfActivity(dest) == -1) myActivities.add(new ProvenanceNode(dest));
		if(getIndexOfEntity(src) == -1) myEntities.add(src);


		myEdges.add(new ProvenanceEdge("WasGeneratedBy", dest,  src,  outputPortIndex));
	}

	// edge type 3: 
	/* this can be inferred from Used. */
	public void addEdge_WasDerivedFrom(String dest, String src)
	{
		if(getIndexOfEntity(dest) == -1) myEntities.add(dest);
		if(getIndexOfEntity(src) == -1) myEntities.add(src);

		myEdges.add(new ProvenanceEdge("WasDerivedFrom", dest,  src));
	}

	// edge type 4:
	public void addEdge_WasInformedBy(String dest, String src)
	{
		if(getIndexOfActivity(src) == -1) myActivities.add(new ProvenanceNode(src));
		if(getIndexOfActivity(dest) == -1) myActivities.add(new ProvenanceNode(dest));
		
		myEdges.add(new ProvenanceEdge("WasInformedBy", dest,  src));
	}

	// edge type 5:
	public void addEdge_wasAssociatedWith(String dest, String src)
	{
		if(getIndexOfAgent(dest) == -1) myAgents.add(dest);
		if(getIndexOfActivity(src) == -1) myActivities.add(new ProvenanceNode(src));
		
		myEdges.add(new ProvenanceEdge("wasAssociatedWith", dest,  src));
	}
	
	// edge type 6:
	public void addEdge_WasAttributedTo(String dest, String src)
	{
		if(getIndexOfAgent(dest) == -1) myAgents.add(dest);
		if(getIndexOfEntity(src) == -1) myEntities.add(src);
		
		myEdges.add(new ProvenanceEdge("WasAttributedTo", dest, src));
	}

	// edge type 7:
	public void addEdge_ActedOnBehalfOf(String dest, String src)
	{
		if(getIndexOfAgent(src) == -1) myAgents.add(src);
		if(getIndexOfAgent(dest) == -1) myAgents.add(dest);
		
		myEdges.add(new ProvenanceEdge("ActedOnBehalfOf", dest, src));
	}
	// workflow edge type:
	public void addEdge_TransTime(String src, String dest, int inputPortIndex, Double transTime, Double outputDatasize)
	{
		myEdges.add(new ProvenanceEdge("Transfer", src, dest, inputPortIndex, transTime, outputDatasize));
	}
	
	
	
	
	
	public int getNumOfNodes()
	{
		return myEntities.size()+myActivities.size()+myAgents.size();
	}
	
	public int getIndexOfEntity(String en)
	{
		for(int i= 0; i <myEntities.size(); i++)
			if(myEntities.get(i).equals(en)) return i;
		
		return -1;
	}

	public int getIndexOfActivity(String ac)
	{
		for(int i= 0; i <myActivities.size(); i++)
			if(myActivities.get(i).equals(ac)) return i;
		
		return -1;
	}
	
	public int getIndexOfAgent(String ag)
	{
		for(int i= 0; i <myAgents.size(); i++)
			if(myAgents.get(i).equals(ag)) return i;
		
		return -1;
	}

    @Override
    public String toString() 
	{
		String str = "";
		for(ProvenanceNode n:myActivities){
			str = str + n.activityname + " was executed within " + n.exetime + " on " + n.vmtype +"\n";
		}
		
		for(ProvenanceEdge e: myEdges) {
			if(e.edgeType.equals("Used")) 
				str = str + e.destNode + " <=Used " + e.srcNode + "." + e.inputPort+"\n";
			else if(e.edgeType.equals("WasGeneratedBy")) 
				str = str + e.destNode+"."+e.outputPort+ " <=WasGeneratedBy " + e.srcNode + "\n";
			else if(e.edgeType.equals("Transfer"))
				str = str + e.destNode+ "."+ e.outputPort + " with the output data size " + e.outputdatasize + " MB "+ " <=WasTransferTo " + e.srcNode + " within " + e.transTime+ "(S)"+  "\n";
			else				
				str = str + e.destNode+ " <="+e.edgeType+ " " + e.srcNode + "\n";
			
		}				
		return str;		
	}

	
	
	/* logProvenanceGraph will write the whole provenance graph to the file */	
    public void record()
    {
		FileWriter fw = null;
		BufferedWriter bw = null;

		
		File file = new File(provname);
		
		try {
			if(!file.exists()) file.createNewFile();
			
			fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
			
			bw.write(this.toString());

			bw.close();
			fw.close();
		} catch(IOException e) {
			Dataview.debugger.logException(e); // DATAVIEW can continue to rerun with exception
			e.printStackTrace();
		}   
    }
    
    public void record(String path)
    {
		FileWriter fw = null;
		BufferedWriter bw = null;

		
		File file = new File(path + provname);
		
		try {
			if(!file.exists()) file.createNewFile();
			
			fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
			
			bw.write(this.toString());

			bw.close();
			fw.close();
		} catch(IOException e) {
			Dataview.debugger.logException(e); // DATAVIEW can continue to rerun with exception
			e.printStackTrace();
		}   
    }
    
}


