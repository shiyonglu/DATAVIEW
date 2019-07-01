import dataview.models.*; 
import java.awt.BorderLayout;
import java.awt.Dimension;


import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.dropbox.core.v1.DbxEntry.File;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class WorkflowVisualization extends JFrame
{
	private GraphControl graphControl;

	public void drawWorkflowGraph(Workflow w)
	{
	   // Creates graph with model
		mxGraph graph = new mxGraph();
		Dataview.debugger.logNullPointer("graph", graph);
		
		Object parent = graph.getDefaultParent();
		int numnode1 = w.wins.length;
		int numnode2 = w.getNumOfTasks();
		int numnode3 = w.wouts.length;
		
		
		int numnode = numnode1+numnode2+numnode3;

		graph.getModel().beginUpdate();
		try
		{
					
			Object[] nodes = new Object[numnode];
			//Object[] edges = new Object[edgeCount];
			
			

			// draw srcFilename nodes
			for (int i = 0; i < numnode1; i++)
			{	
				if(w.wins[i].getClass().equals(DATAVIEW_BigFile.class)){
					String inputname = ((DATAVIEW_BigFile) w.wins[i]).getFilename();
					System.out.println("*************"+inputname);
					 nodes[i] = graph.insertVertex(parent, null, ""+inputname , 0, 0, 60,
								20);
				}
				
				// nodes[i] = graph.insertVertex(parent, null, ""+ w.wins[i], 0, 0, 60, 20);
			}

			// draw task nodes
			for (int i = 0; i < numnode2; i++)
			{
				 nodes[i+numnode1] = graph.insertVertex(parent, null, ""+w.getTask(i), 0, 0, 60,
						20);
			}
			


			// draw destFilename nodes
			for (int i = 0; i < numnode3; i++)
			{
				if(w.wouts[i].getClass().equals(DATAVIEW_BigFile.class)){
					String outputname = ((DATAVIEW_BigFile) w.wouts[i]).getFilename();
					 nodes[i+numnode1+numnode2] = graph.insertVertex(parent, null, ""+outputname , 0, 0, 60,
								20);
				}
				// nodes[i+numnode1+numnode2] = graph.insertVertex(parent, null, ""+w.wouts[i], 0, 0, 60,	20);
			}

			
			
			for(WorkflowEdge e: w.getEdges()) {
				if (e.winIndex != -1) {   // source node is an input file
					int i = e.winIndex;
				    int j = w.getIndexOfTask(e.destTask);
					 graph.insertEdge(parent, null, null,
							nodes[i], nodes[j+numnode1]);
				}
				else if(e.woutIndex != -1) { // destination node is an output file
					int i = w.getIndexOfTask(e.srcTask);
				    int j = e.woutIndex;
					 graph.insertEdge(parent, null, null,
							nodes[i+numnode1], nodes[j+numnode1+numnode2]);
				}
				else { // both the source node and the destination node are tasks
					int i = w.getIndexOfTask(e.srcTask);
				    int j = w.getIndexOfTask(e.destTask);
					 graph.insertEdge(parent, null, null,
							nodes[i+numnode1], nodes[j+numnode1]);				
				}
			}
				
					
		

			 // mxCircleLayout layout = new mxCircleLayout(graph);
			 //  mxFastOrganicLayout layout = new mxFastOrganicLayout(graph);
			//  layout.setForceConstant(100);
			 mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
			
			 layout.execute(parent);
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		graph.getView().setScale(1.3);

		// Creates a control in a scrollpane
		graphControl = new GraphControl(graph);
		JScrollPane scrollPane = new JScrollPane(graphControl);
		scrollPane.setAutoscrolls(true);

		// Puts the control into the frame
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		setSize(new Dimension(1320, 900));
		
		// mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
		
		// layout.execute(graph);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1500, 700);
		setVisible(true);

	}		

	/*public static void maink(String[] args)
	{
		WorkflowVisualization frame = new WorkflowVisualization();		
		KMeans_workflow w = new KMeans_workflow();			
		w.design();
		frame.drawWorkflowGraph(w);
		
	}*/	
	public static void main(String[] args)
	{
		WorkflowVisualization frame = new WorkflowVisualization();		


		//Task t = new Task1();
		//System.out.println("Task1 spec: \n"+ t.getTaskSpecification());

		//ReassignCluster_task rt = new ReassignCluster_task();
		//System.out.println("ReassignCluster_task spec: \n"+ rt.getTaskSpecification());
		
		WordCount_workflow w = new WordCount_workflow();
		//WordCount_workflow w = new WordCount_workflow();
		w.design();
		frame.drawWorkflowGraph(w);
		JSONObject ob = w.getWorkflowSpecification();
		System.out.println("Workflow spec:\n" + ob);
		//JSONObject edge1 = ob.get("edges").toJSONArray().get(0).toJSONObject();
		
		//System.out.println("destTaskInstanceID:" + edge1.get("destTaskInstanceID"));
	}
	
	
	/*public static void maintt(String[] args)
	{
		WorkflowVisualization frame = new WorkflowVisualization();		

		
		// WorkflowGenerator wg = new WorkflowGenerator(7, 5);
		WorkflowGenerator2 wg = new WorkflowGenerator2(5, 3, 6, 4);
		
		
		Workflow w;
		try {
			w = wg.generate();							
			System.out.println("Generate a workflow:\n " + w.toString());
			
			frame.drawWorkflowGraph(w);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	
		
		// file-based implementation
		// WorkflowPlanner wp = new WorkflowPlanner(w);	 // to do
		// GlobalSchedule gs = wp.plan(); // to do
		// WorkflowExectuor we = new WorkflowExecutor(tasklibdir, workflowlibdir, gs);  // done
		// we.run();
		
		Integer I1 = new Integer(101);
		Integer I2 = null; 
		
		if(I1 > 100) {
			Dataview.debugger.logObjectValue("I1", I1);
			Dataview.debugger.logObjectValue("I2", I2);
			Dataview.debugger.logErrorMessage("I1 cannot be greater than 100, sth is wrong here at " + "WorkflowVisualization.main(), line 155.");
		}
		
		Dataview.debugger.logNullPointer("I2", I2);		
	}*/
}
