package dataview.models;

public class ProvanceNode {
	public String activityname;
	public double exetime;
	
	public ProvanceNode(String activityname,double exetime){
		this.activityname = activityname;
		this.exetime = exetime;
	}
	
	public ProvanceNode(String activityname){
		this.activityname = activityname;
	}
}
