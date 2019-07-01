import dataview.models.DATAVIEW_BigFile;
import dataview.models.Task;
import dataview.models.Workflow;

public class Diagnosis extends Workflow {

	public Diagnosis() {
		super("DiagnosisRecommendation", " This workflow is for doctor to make some recommendation for a patient");
		wins = new Object[2];
		wouts = new Object[1];
		wins[0] = new DATAVIEW_BigFile("originalInput.txt");
		//wins[1] = new DATAVIEW_BigFile("parameter.txt");
		wins[1] = new Double(0.3);
		wouts[0] =new DATAVIEW_BigFile("DGoutput0.txt");
		
	}
	public void design()
	{
		
		
        // create and add all the tasks
		
		Task T1 = addTask("Extraction");
		Task T2 = addTask("Partitioner");
		Task T3 = addTask("SplitDataOnePort");
		Task T4 = addTask("Algorithm1");
		Task T5 = addTask("Algorithm2");
		Task T6 = addTask("Evaluation");
		
		
		
		// add edges
		
		addEdge(0, T1, 0);
	
		addEdge(T1, 0, T2, 0);
		
		addEdge(T2,0,T3,0);
		addEdge(T2,1,T5,2);
		addEdge(T2,1,T6,1);
		
		addEdge(T3,0,T4,0);
		addEdge(T3,0,T5,0);
		addEdge(T3,1,T4,1);
		
		addEdge(1,T4,2);
		addEdge(T4,0,T5,1);
		
		addEdge(1,T5,3);
		addEdge(T5,0,T6,0);
		

	    addEdge(T6, 0, 0);	
	       
	}
		
}
