package dataview.models;

	/** We are using the PROV-DM model:
	 * https://www.w3.org/TR/prov-dm/
	 * W3C Recommendation 30 April 2013
	 * 
	 *  The PROV-DM model has thee kinds of nodes: entities (model data products), activities (model workflow tasks), and agents (model 
	 *  users who execute the workflow tasks, and seven kinds of edges: used, wasGeneratedBy, wasDerivedFrom, wasInformedBy, 
	 *  wasAssociatedWith, wasAttibutedTo, and ActedOnBehalfOf.
	 *  
	 *  This is just a data structure to hold all information of an edge together.
	 */

public class ProvenanceEdge {
	// as provenance edge is backward, we list the destination node first on the left, this will be convention we follow for all 
	// methods regarding provenance edges.
	public String destNode;
	public String edgeType;
	public String srcNode;
	public String inputPort;
	public String outputPort;
	public double transTime;
	public double outputdatasize;
	
	public ProvenanceEdge(String edgeType,  String destNode,  String srcNode)
	{
		this.edgeType = edgeType;
		this.destNode = destNode;
		this.srcNode = srcNode;
		this.inputPort = null;
		this.outputPort = null;
	}
	
	// we want to capture the port information for "Used" and "WasGeneratedBy".
	public ProvenanceEdge(String edgeType,  String destNode, String srcNode,  int portIndex)
	{
		this.edgeType = edgeType;
		this.destNode = destNode;
		this.srcNode = srcNode;
		
		
		if(edgeType.equals("Used")){
			this.inputPort = "ins["+ portIndex+"]";
			this.outputPort = null;
		}
		else if (edgeType.equals("WasGeneratedBy")){
			this.outputPort = "outs["+ portIndex+"]";
			this.inputPort = null;			
		}
		else { // something is wrong we we enter here
			Dataview.debugger.logErrorMessage("File: ProveannceEdge.java, method: ProvnancEdge, Line: 53, wrong edge type: "+ edgeType);			
		}		
	}
	// transfer means data movement time
	public ProvenanceEdge(String edgeType,  String destNode, String srcNode,  int portIndex, Double transTime, Double outputDatasize)
	{
		this.edgeType = edgeType;
		this.destNode = destNode;
		this.srcNode = srcNode;
		this.transTime = transTime;
		this.outputdatasize = outputDatasize;
		
		if(edgeType.equals("Used")){
			this.inputPort = "ins["+ portIndex+"]";
			this.outputPort = null;
		}
		else if (edgeType.equals("WasGeneratedBy")){
			this.outputPort = "outs["+ portIndex+"]";
			this.inputPort = null;			
		}
		else if (edgeType.equals("Transfer")){
			this.outputPort = "outs["+ portIndex+"]";
			this.inputPort = null;	
			this.transTime = transTime;
			this.outputdatasize = outputDatasize;
		}
		
		
		else { // something is wrong we we enter here
			Dataview.debugger.logErrorMessage("File: ProveannceEdge.java, method: ProvnancEdge, Line: 53, wrong edge type: "+ edgeType);			
		}		
	}
	
	
}
