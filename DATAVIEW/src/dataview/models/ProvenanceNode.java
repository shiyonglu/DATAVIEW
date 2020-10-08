package dataview.models;

public class ProvenanceNode {
	public String activityname;
	public double exetime;
	public String vmtype;
	
	
	public ProvenanceNode(String activityname,double exetime){
		this.activityname = activityname;
		this.exetime = exetime;
	}
	
	public ProvenanceNode(String activityname){
		this.activityname = activityname;
	}
	public ProvenanceNode(String activityname, double exetime, String vmtype){
		this.activityname = activityname;
		this.exetime = exetime;
		this.vmtype = vmtype;
	}
	
}
