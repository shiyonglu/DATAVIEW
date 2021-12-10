import dataview.models.*; 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.dropbox.core.v1.DbxEntry.File;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class NueronNetVisulization extends JFrame
{
	private GraphControl graphControl;
	private String title;
	private JSONObject map;
	
	public NueronNetVisulization(String title, JSONObject map) {
		super(title);
		this.title = title;
		this.map = map;
	}

	public void drawNeuronNetGraph()
	{
	   // Creates graph with model
		mxGraph graph = new mxGraph();
		Dataview.debugger.logNullPointer("graph", graph);
		
		Object parent = graph.getDefaultParent();
		
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.CYAN));
		style.put(mxConstants.STYLE_STROKEWIDTH, 1);
		style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(19, 118, 143)));
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		style.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_ELLIPSE);
		style.put(mxConstants.STYLE_FONTSIZE, 8);
		
		mxStylesheet stylesheet = graph.getStylesheet();
		stylesheet.putCellStyle("Linear", style);
		
		Hashtable<String, Object> style1 = new Hashtable<String, Object>();
		style1.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.GREEN));
		style1.put(mxConstants.STYLE_STROKEWIDTH, 1);
		style1.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(19, 118, 143)));
		style1.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		style1.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_ELLIPSE);
		style1.put(mxConstants.STYLE_FONTSIZE, 8);
		
		stylesheet.putCellStyle("Relu", style1);
		
		Hashtable<String, Object> style2 = new Hashtable<String, Object>();
		style2.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.MAGENTA));
		style2.put(mxConstants.STYLE_STROKEWIDTH, 1);
		style2.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(19, 118, 143)));
		style2.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		style2.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_ELLIPSE);
		style2.put(mxConstants.STYLE_FONTSIZE, 8);
		
		stylesheet.putCellStyle("Sigmoid", style2);
		
		 Hashtable<String, Object> edgeStyle = new Hashtable<String, Object>();
		 edgeStyle.put(mxConstants.STYLE_DIRECTION, mxConstants.NONE);
		 edgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.orthConnector);
		 edgeStyle.put(mxConstants.STYLE_ENTRY_X, 0.0);
		 edgeStyle.put(mxConstants.STYLE_ENTRY_Y, 0.5);

		 stylesheet.putCellStyle("EdgeStyle", edgeStyle);


		graph.getModel().beginUpdate();
		try
		{

			List<List<Object>> listOfNodes = new ArrayList<List<Object>>();
			//have a list to store the number of output neurons for each layer
			List<Integer> numOfOpt = new ArrayList<Integer>();
			//store layer types in an list
			List<Integer> layerType = new ArrayList<Integer>();
			
			int lastLayerOptNum=0;
			
			Iterator it = map.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry pair = (Map.Entry)it.next();
				//System.out.println(Stream.of(pair.getValue().toString().replace("\"", "").split(",")).map(Integer::valueOf).toArray(Integer[]::new));
				int[] specs = Stream.of(pair.getValue().toString().replace("\"", "").split(",")).mapToInt(Integer::parseInt).toArray();
				
				//initialize a list to store current layer's nodes 
				List<Object> nodesOfLayer = new ArrayList<Object>();
				switch(specs[0]) {
				case 0:
					int inputNum = specs[1];
					int outputNum = specs[2];
					//add input nodes of linear layer
					for(int i = 0; i<inputNum; i++) {
						nodesOfLayer.add(graph.insertVertex(parent, null, "Linear Input"+i , 0, 0, 60,
								60, "Linear"));
					}
					//add output nodes of linear layer
					for(int i = 0; i<outputNum; i++) {
						nodesOfLayer.add(graph.insertVertex(parent, null, "Linear Output"+i , 0, 0, 60,
								60, "Linear"));
					}
					//add nodes of linear layer to NN
					listOfNodes.add(nodesOfLayer);
					numOfOpt.add(outputNum);
					layerType.add(specs[0]);
					break;
				case 1:
					int Num = numOfOpt.get(numOfOpt.size()-1);
					//add nodes of relu layer
					for(int i = 0; i<Num; i++) {
						nodesOfLayer.add(graph.insertVertex(parent, null, "ReLu"+i , 0, 0, 60,
								60, "Relu"));
					}
					//add not of relu layer to NN
					listOfNodes.add(nodesOfLayer);
					numOfOpt.add(Num);
					layerType.add(specs[0]);
					break;
				case 2: 
					int nodeNum = numOfOpt.get(numOfOpt.size()-1);
					//add nodes of sigmoid layer
					for(int i = 0; i<nodeNum; i++) {
						nodesOfLayer.add(graph.insertVertex(parent, null, "Sigmoid"+i , 0, 0, 60,
								60, "Sigmoid"));
					}
					//add node of sigmoid layer to NN
					listOfNodes.add(nodesOfLayer);
					numOfOpt.add(nodeNum);
					layerType.add(specs[0]);
					break;
				}
			}
		
			//current layer
			int currentLayer = 0;
			//add edges between nodes
			for(Integer i: layerType) {
				int nodeSize = listOfNodes.get(currentLayer).size();
				switch(Integer.parseInt(i.toString())) {
				case 0:
					int outputNum = numOfOpt.get(currentLayer);
					int inputNum = nodeSize - outputNum;
					
					if(currentLayer != 0) {
						int lastLayerSize = listOfNodes.get(currentLayer-1).size();
						int lastLayerOpt = numOfOpt.get(currentLayer-1);
						
						//connect last layer's output to this layer's input                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
						for(int k = lastLayerSize-lastLayerOpt; k < lastLayerOpt; k++) {
							graph.insertEdge(parent, null, null, listOfNodes.get(currentLayer-1).get(k), listOfNodes.get(currentLayer).get(k-lastLayerSize + lastLayerOpt), "EdgeStyle");
						}
					}
					
					for(int m=0; m<inputNum; m++) {
						for(int n=0; n<outputNum; n++) {
							graph.insertEdge(parent, null, null, listOfNodes.get(currentLayer).get(m), listOfNodes.get(currentLayer).get(nodeSize-outputNum+n), "EdgeStyle");
						}
					}
					currentLayer++;
					break;
				case 1:
						if(currentLayer != 0) {
							int lastLayerSize = listOfNodes.get(currentLayer-1).size();
							int lastLayerOpt = numOfOpt.get(currentLayer-1);
							
							//connect last layer's output to this layer's nodes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
							for(int k = lastLayerSize-lastLayerOpt; k < lastLayerSize; k++) {
								graph.insertEdge(parent, null, null, listOfNodes.get(currentLayer-1).get(k), listOfNodes.get(currentLayer).get(k-lastLayerSize +lastLayerOpt), "EdgeStyle");
							}
						}
						currentLayer++;
						break;
				case 2:
						if(currentLayer != 0) {
							int lastLayerSize = listOfNodes.get(currentLayer-1).size();
							int lastLayerOpt = numOfOpt.get(currentLayer-1);
							
							//connect last layer's output to this layer's input                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
							for(int k = lastLayerSize-lastLayerOpt; k < lastLayerSize; k++) {
								graph.insertEdge(parent, null, null, listOfNodes.get(currentLayer-1).get(k), listOfNodes.get(currentLayer).get(k-lastLayerSize + lastLayerOpt), "EdgeStyle");
							}
						}
						currentLayer++;
						break;
				}
				
			}
				
					
		

			 // mxCircleLayout layout = new mxCircleLayout(graph);
//			   mxFastOrganicLayout layout = new mxFastOrganicLayout(graph);
			//  layout.setForceConstant(100);
			 mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
//			 mxCompactTreeLayout layout = new mxCompactTreeLayout(graph);
			 layout.setOrientation(SwingConstants.WEST);
			 layout.setFineTuning(true);

			
			 layout.execute(parent);
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		graph.getView().setScale(1.3);
		//graph.getView().setScale(2.1);

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
		
		JSONObject testmap = new JSONObject();
		testmap.put("0", new JSONValue("0,2,5"));
		testmap.put("1", new JSONValue("1"));
		testmap.put("2", new JSONValue("0,5,3"));
		testmap.put("3", new JSONValue("1"));
		testmap.put("4", new JSONValue("0,3,1"));
		testmap.put("5", new JSONValue("2"));

		NueronNetVisulization frame = new NueronNetVisulization("task1234", testmap);	
		frame.drawNeuronNetGraph();
		//Task t = new Task1();
		//System.out.println("Task1 spec: \n"+ t.getTaskSpecification());

		//ReassignCluster_task rt = new ReassignCluster_task();
		//System.out.println("ReassignCluster_task spec: \n"+ rt.getTaskSpecification());
		
//		WordCount_workflow w = new WordCount_workflow();
		//WordCount_workflow w = new WordCount_workflow();
//		w.design();
//		frame.drawWorkflowGraph(w);
//		JSONObject ob = w.getWorkflowSpecification();
//		System.out.println("Workflow spec:\n" + ob);
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
