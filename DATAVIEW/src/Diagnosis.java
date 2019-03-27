import dataview.models.Task;
import dataview.models.Workflow;

public class Diagnosis extends Workflow {

	public Diagnosis() {
		super("Diagnosis Recommendation", " This workflow is for doctor to make some recommendation for a patient");
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
		addEdge("originalInput.txt", T1, 0);
		addEdge(T1, 0, T2, 0);
		
		addEdge(T2,0,T3,0);
		addEdge(T2,1,T5,2);
		addEdge(T2,1,T6,1);
		
		addEdge(T3,0,T4,0);
		addEdge(T3,0,T5,0);
		addEdge(T3,1,T4,1);
		
		addEdge("parameter.txt",T4,2);
		addEdge(T4,0,T5,1);
		
		addEdge("parameter.txt",T5,3);
		addEdge(T5,0,T6,0);
		

	    addEdge(T6, 0, "output0.txt");	    
	}
		
}
