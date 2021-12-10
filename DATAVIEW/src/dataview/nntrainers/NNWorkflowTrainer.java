package dataview.nntrainers;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dataview.models.JSONObject;
import dataview.models.JSONValue;
import dataview.models.NNTask;
import dataview.models.NNWorkflow;

/**
 * The NNWorkfowTrainer is the base class will parse the NNWorkflow specs to GPU side recognizable specs in JSON format
 * These specs will be used in one particular child class, which specify the infrastructual settings in one of particular child classes 
 * @author: Junwen Liu
 * */

public class NNWorkflowTrainer {
	public static final int NNWorkflowTrainer_Classic = 0;
	
	protected NNWorkflow w;
	
	//get nnworkflow specifications
	protected JSONObject obj;
	//protected String output;
	protected String specification;
	//protected current dir, local and web are different.
	protected String fileLocation;
	protected int numOfBatches;
	protected int numOfEpochs;
	
	//constructor with workflow w as input
	public NNWorkflowTrainer(NNWorkflow w, int numOfBatches, int numOfEpochs)
	{
		this.w = w;
		this.numOfBatches = numOfBatches;
		this.numOfEpochs = numOfEpochs;
		this.fileLocation = System.getProperty("user.dir") + File.separator + "WebContent" +File.separator + "workflowTaskDir";
		initialParsing();
	}
	
	//constructor with workflow w as input and a location for web version
		public NNWorkflowTrainer(NNWorkflow w, String location, int numOfBatches, int numOfEpochs)
		{
			this.w = w;
			this.numOfBatches = numOfBatches;
			this.numOfEpochs = numOfEpochs;
			this.fileLocation = location;
			initialParsing();
		}
	
	public void initialParsing() {
		obj = new JSONObject();
		//get workflow inputfile name
		
		//System.out.println(w.getWorkflowInput(0).toString().replaceAll("\\\\", "\\\\\\\\"));
		//System.out.println((fileLocation + "Breast_cancer_data_normalized.csv").replaceAll("\\\\", "\\\\\\\\"));
		//obj.put("wIpt", new JSONValue(w.getWorkflowInput(0).toString().replaceAll("\\\\", "\\\\\\\\")));
		System.out.println(w.getWorkflowInput(0).toString());
		obj.put("wIpt", new JSONValue((fileLocation + "\\" + w.getWorkflowInput(0).toString()).replaceAll("\\\\", "\\\\\\\\")));
		obj.put("wOpt", new JSONValue((fileLocation + "\\" +  w.getWorkflowOutput(0).toString()).replaceAll("\\\\", "\\\\\\\\")));
//		obj.put("numOfBatches", new JSONValue(Integer.toString(w.getNumOfBatches())));
//		obj.put("numOfEpochs", new JSONValue(Integer.toString(w.getNumOfEpochs())));
		obj.put("numOfBatches", new JSONValue(Integer.toString(this.numOfBatches)));
		obj.put("numOfEpochs", new JSONValue(Integer.toString(this.numOfEpochs)));
				
		//get each specificartion json, which will be fed into CUDA GPU for running the NN
		int numOfLayers =  w.getNumOfTasks();	
		JSONObject layerSpec = new JSONObject();
				
		//get layers' specs from each task specs
		for(int i = 0; i< numOfLayers; i++) {
					
		NNTask t = (NNTask) w.getTask(i);
		//tell task's type from task name
		if(t.toString().startsWith("Linear")) {
						
			//linear layer, e.g. {"i": "0, 5, 3"}				
			layerSpec.put(String.valueOf(i), new JSONValue("0," + t.getTaskSpecification().get("inFeatures").toString().replace("\"", "") + "," + t.getTaskSpecification().get("outFeatures").toString().replace("\"", "")));
			}else if(t.toString().startsWith("ReLU")){
			//Relu layer, e.g. {"i": "1"}
			layerSpec.put(String.valueOf(i), new JSONValue("1"));
			}else if(t.toString().startsWith("Sigmoid")) {
			//SIgmoid layer, e.g. {"i": "2"}
			layerSpec.put(String.valueOf(i), new JSONValue("2"));
						
			}			
		}
		
		//visualize neuron network architecture
		try {
			Class cl = Class.forName("NueronNetVisulization");
			Constructor c = cl.getConstructor(String.class, JSONObject.class);
			Object obj = (Object) c.newInstance(w.workflowName, layerSpec);
			
			Method vis = cl.getDeclaredMethod("drawNeuronNetGraph");
			
			//vis.drawNeuronNetGraph();
			vis.invoke(obj);
			
		}catch (ClassNotFoundException e) { 
            e.printStackTrace(); 
        } catch (NoSuchMethodException e) { 
            e.printStackTrace(); 
        } catch (SecurityException e) { 
            e.printStackTrace(); 
        } catch (IllegalAccessException e) { 
            e.printStackTrace(); 
        } catch (IllegalArgumentException e) { 
            e.printStackTrace(); 
        } catch (InvocationTargetException e) { 
            e.printStackTrace(); 
        } catch (InstantiationException e) { 
            e.printStackTrace(); 
        } 

		
				
			obj.put("LayerArc", new JSONValue(layerSpec));
				
			specification = obj.toString();
			specification = specification.replaceAll("[\r\n]+", "");
			specification = specification.replaceAll("\\s", "");
	}
		
	//following run method will be implemented by every NNWorkflow trainer
	public String train()
	{
		return "";
	}
}
