import dataview.models.DATAVIEW_BigFile;
import dataview.models.Task;
import dataview.models.Workflow;

public class MR extends Workflow {

	public MR() {
		super("Map-reduce workflow", " This workflow is for testing the sync-file transfer");
		wins = new Object[1];
		wouts = new Object[1];
		wins[0] = new DATAVIEW_BigFile("MPInput.txt");
		wouts[0] = new DATAVIEW_BigFile("output0.txt");
	}
	public void design()
	{

        // create and add all the tasks
		
		Task T1 = addTask("T1");
		Task[] T2 = addTasks("T2",3);
		Task T3 = addTask("T3");
		
		
		
		
		// add edges
		addEdge(0, T1, 0);
		addEdge(T1, 0, T2[0], 0);
		addEdge(T1, 1, T2[1], 0);
		addEdge(T1, 2, T2[2], 0);
		addEdge(T2[0],0,T3,0);
		addEdge(T2[1],0,T3,1);
		addEdge(T2[2],0,T3,2);
	    addEdge(T3, 0, 0);	    
	}
		
}
