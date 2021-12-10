package dataview.nnexecutors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

import dataview.models.JSONValue;

	/**
	 * The NNWorkfowExecutor is the base class will read the json output saved from previous trained models, it will get previous trained model's architectural design, hyperparameters, and new iput dataset and save them as specs in JSON format
	 * These specs will be used in one particular child class, which specify the infrastructual settings in one of particular child classes 
	 * @author: Junwen Liu
	 * */

	public class NNWorkflowExecutor_base {
		protected String inputFileName;
		protected String jsonFileName;
		//get nnworkflow specifications
		protected JSONObject obj;
		protected JSONObject newobj;
		//protected String output;
		protected String specification;
		//protected current dir, local and web are different.
		protected String fileLocation;
		
		//constructor with a file name as input
		public NNWorkflowExecutor_base(String inputFileName, String jsonFileName)
		{
			this.inputFileName = inputFileName;
			this.jsonFileName = jsonFileName;
			this.fileLocation = System.getProperty("user.dir") + File.separator + "WebContent" +File.separator + "workflowTaskDir";
			/*
			 * Path root = Paths.get(".").normalize().toAbsolutePath();
			 * System.out.println("this is the current path: " + root.toString());
			 * this.fileLocation = root + File.separator + "WebContent" +File.separator +
			 * "workflowTaskDir";
			 */
			jsonFileParsing();
		}
		
		//constructor with workflow w as input and a location for web version
			public NNWorkflowExecutor_base(String inputFileName, String jsonFileName, String location)
			{
				this.inputFileName = inputFileName;
				this.jsonFileName = jsonFileName;
				this.fileLocation = location;
				jsonFileParsing();
			}
		
		public void jsonFileParsing() {
			obj = new JSONObject();
			newobj = new JSONObject();
			
			//read the json file and part it in json object
			//sample json file:
			//{ "DeviceInfo" : "CUDA Device0: GeForce RTX 2080 SUPER7.5: Global memory: 8192mb Shared memory: 48kb Constant memory: 64kb Block registers: 65536 Warp size: 32 Threads per block: 1024 Max block dimensions: [ 1024, 1024, 64 ] Max grid dimensions: [ 2147483647, 65535, 65535 ]", "LayerArc" : { "0" : "0,5,3", "1" : "1", "2" : "0,3,1", "3" : "2" }, "SavedModel" : { "0" : { "bias" : "[44.12920380-70.3660965011.17895031]", "weight" : "[[2.44433546, -2.16660404, 4.90515614], [-2.16514421, -0.49593592, 0.22421788], [1.38725889, -0.33631948, 1.02884686], [0.71579784, -0.51459634, 0.55676436], [0.01803080, -2.92734289, 1.12091708]]" }, "2" : { "bias" : "[23.86134911]", "weight" : "[3.34796143, -5.95831585, 2.21814156]" } }, "TestingAccuracy" : 0.81415927410125732, "TrainingCostAtEpoch" : { "0" : 0.539081871509552, "100" : 0.40221291780471802, "200" : 0.42893671989440918, "300" : 0.42477750778198242, "400" : 0.37060731649398804, "500" : 0.37419360876083374, "600" : 0.34428369998931885, "700" : 0.32800042629241943, "800" : 0.31015551090240479, "900" : 0.31076043844223022 } }
		      try {
		    	String content = null;
				StringBuilder contentBuilder = new StringBuilder();
				BufferedReader br = new BufferedReader(new FileReader(this.jsonFileName));
			    String sCurrentLine;
			    while ((sCurrentLine = br.readLine()) != null){
			        	contentBuilder.append(sCurrentLine).append("\n");
			    }
			    
			    br.close();
				content = contentBuilder.toString();
				obj = new JSONObject(content);
				
			
		         JSONObject LayerArc = obj.getJSONObject("LayerArc");
		         JSONObject SavedModel = obj.getJSONObject("SavedModel");
	
		         System.out.println("LayerArc: " + LayerArc.toString());
		         System.out.println("SavedModel: " + SavedModel.toString());
		         
		       //get the architecture design, saved model and input dataset repo and save them in the specificaiton.
		         newobj.put("wIpt", new JSONValue(this.inputFileName));
		         newobj.put("LayerArc", LayerArc);
		         newobj.put("SavedModel", SavedModel);
		      } catch(Exception e) {
		         e.printStackTrace();
		      }
					
				specification = newobj.toString();
				specification = specification.replaceAll("\\\\\\\"", "");
				specification = specification.replaceAll("[\r\n]+", "");
				specification = specification.replaceAll("\\s", "");
				//{"wIpt":"\"C:\\\\Users\\\\junwen\\\\Desktop\\\\Eclispse_repositories\\\\DATAVIEW-2.2.1\\\\WebContent\\\\workflowTaskDir\\\\Breast_cancer_data.csv\"","SavedModel":{"0":{"bias":"[44.12920380,-70.36609650,11.17895031]","weight":"[[2.44433546, -2.16660404, 4.90515614], [-2.16514421, -0.49593592, 0.22421788], [1.38725889, -0.33631948, 1.02884686], [0.71579784, -0.51459634, 0.55676436], [0.01803080, -2.92734289, 1.12091708]]"},"2":{"bias":"[23.86134911]","weight":"[3.34796143, -5.95831585, 2.21814156]"}},"LayerArc":{"0":"0,5,3","1":"1","2":"0,3,1","3":"2"}}
		}
			
		//following run method will be implemented by every NNWorkflow trainer
		public String Execute()
		{
			return "";
		}
	}
