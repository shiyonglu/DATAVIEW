
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import Utility.MXGraphToSWLTranslator;
import dataview.models.*;
import dataview.planners.WorkflowPlanner;
import dataview.planners.WorkflowPlanner_Naive2;
import dataview.planners.WorkflowPlanner_T_Cluster;
import dataview.workflowexecutors.VMProvisionerAWS;
import dataview.workflowexecutors.WorkflowExecutor;
import dataview.workflowexecutors.WorkflowExecutor_Beta;
import dataview.workflowexecutors.WorkflowExecutor_Local;


/**
 * This is the main servlet that receives and responds to requests from Webbench.
 * 
 */
public class Mediator extends HttpServlet {
	/**
	 *        strUser: user id 
	 *   fileLocation: task source files location for each user
	 *  tableLocation: the file location of the user table stored all the user registration information
	 *          token: dropbox token of each user
	 *      accessKey: AWS EC2 access key 
	 *      secretKey: AWS EC2 secret key
	 *  outputMapping: map the output port index to the corresponding file name
	 */
	private static String strUser = "";
	private static final long serialVersionUID = 1L;
	private static String fileLocation = "";
	private static String tableLocation = "";
	private static String token;
	private static String accessKey;
	private static String secretKey;
	private static HashMap<String, String> outputMapping = new HashMap<String, String>();;
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws Exception, ServletException, IOException {
		response.setContentType("text/plain");
		String path = getServletContext().getRealPath(request.getServletPath()).replace("Mediator", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator;
		Dataview.setDebugger(path+"dataview.log");
		Dataview.debugger.setDisplay(true);
		String action = request.getParameter("action");
		// Initialize a folder for each user
		if(action.equals("initializeUserFolder")){
			System.out.println("Initialize Each User Storage Space");
			initializeUserFolder(request,response);
		// Save the composed workflow mxgrah information in Dropbox
		}else if (action.equals("saveAs")) {
			saveAs(request.getParameter("name"), request.getParameter("diagram"), response);
		// Write the Dropbox token into local file	
		} else if (action.equals("loadDropboxKey")) {
			loadDropboxKey(request.getParameter("userId"), request.getParameter("token"), response);
		// Run a workflow with Amazon EC2	
		} else if (action.equals("provisionVMsInEC2AndRunWorkflows")) {
			Dataview.debugger.logSuccessfulMessage("provision vms request recieved from webench ");
			provisionVMsInEC2AndRunWorkflows(request.getParameter("userID"), request.getParameter("name"), 
					request.getParameter("accessKey"), request.getParameter("secretKey"), response);
		// Modify a composed workflow and save in Dropbox
		} else if (action.equals("overwriteAndSave")) {
			overwriteAndSave(request.getParameter("name"), request.getParameter("diagram"), response);
		// Get the mxgraph of a workflow 
		} else if (action.equals("getWorkflowDiagram")) {
			getWorkflowDiagram(request.getParameter("wfPath"), response);
		// Get the dropbox token information	
		} else if (action.equals("getDropboxDetails")) {
			getDropboxDetails(request.getParameter("userId"), response);
		// Get the input port numbers and output numbers of a task	
		} else if (action.equals("getPortsNumber")) {
			getPortsNumber(request.getParameter("filename"), response);
		// Retrieve the output information from web bench
		} else if (action.equals("getData")){
			getData(request.getParameter("dataName"),response);
		// Store the AWS EC2 accesskey and secretkey
		} else if (action.equals("loadCloudSettings")){
			loadCloudSettings(request.getParameter("userID"),request.getParameter("accessKey"),request.getParameter("secretKey"));
		// Retrieve the confidential information of AWS	
		} else if (action.equals("getCloudSettingDetails")){
			getCloudSettingsDetail(request.getParameter("userId"),response);
		} else if (action.equals("stopVMs")){
			System.out.println("This is the VM termination section");
			stopVMs(request.getParameter("userID"),response);
		}  else if (action.equals("serverRunWorkflows")){
			System.out.println("Run Workflow in local");
			serverRunWorkflows(request.getParameter("userID"),request.getParameter("name"),response);
		}  else if (action.equals("createTree")){
			createTree(request.getParameter("index"),request.getParameter("dropboxToken"), response);
		}
		
		else {
			System.out.println("undefined operation!!!!!!!!!!!!!");
		}

	}
	
	public void createTree(String FileName,String dropboxToken,HttpServletResponse response){
		JSONArray treeNode = new JSONArray(); 
		String parentFileName = "";
		if (!FileName.equals("dropbox")){
			parentFileName = FileName;
		}
		try{
			treeNode = dropboxRetrieve(parentFileName,dropboxToken);
			 System.out.println(treeNode);
			 response.setContentType("application/json");
			 response.getWriter().print(treeNode); 
		} catch (ListFolderErrorException e) {
			e.printStackTrace();
		} catch (DbxException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}	
	}
	 public JSONArray dropboxRetrieve(String parentFolder, String dropboxToken) throws ListFolderErrorException, DbxException{
		 JSONArray files = new JSONArray();
		 DbxRequestConfig config = new DbxRequestConfig("en_US");
		 DbxClientV2 client = new DbxClientV2(config, dropboxToken);
		 ListFolderResult result = null;
		 if(parentFolder.equals("")){
			 result = client.files().listFolder(parentFolder);
		 }else{
			 if (client.files().getMetadata(parentFolder) instanceof FolderMetadata){
				 result = client.files().listFolder(parentFolder);
			 }else{
				 return null;
			 }
		 }
		 while (true) {
				for (Metadata metadata : result.getEntries()) {
					String filePath = metadata.getPathDisplay();
					Boolean isParent = true;
					if(metadata instanceof FileMetadata){
						isParent = false;
					}
					String name = "";
					if (filePath.contains(".class") || filePath.contains(".jar")|| filePath.contains(".spec")) {
						name = filePath.substring(filePath.lastIndexOf("/")+1, filePath.indexOf("."));
						
					} else {
						name = filePath.substring(filePath.lastIndexOf("/")+1);
					}
					
					dataview.models.JSONObject node = new dataview.models.JSONObject();
					if(parentFolder.equals("")){
						node.put("id", new JSONValue(filePath));
						node.put("pId", new JSONValue("dropbox"));
						node.put("text", new JSONValue(name));
						node.put("isParent", new JSONValue(Boolean.toString(isParent)));
					}else{
						node.put("id", new JSONValue(filePath));
						node.put("pId", new JSONValue(parentFolder));
						node.put("text", new JSONValue(name));
						node.put("isParent", new JSONValue(Boolean.toString(isParent)));
					}
					//fileNames.add(node);	
					dataview.models.JSONValue jv = new dataview.models.JSONValue(node);
					files.add(jv);
				}

				if (!result.getHasMore()) {
					break;
				}

				result = client.files().listFolderContinue(result.getCursor());
			}

		 return files;
	 }
	
	
	/**
	 * Terminate all the available and pending VM instances.
	 * @param userId
	 * @param response
	 */
	public void stopVMs(String userId,HttpServletResponse response) {
		accessKey = ReadAndWrite.read(tableLocation + "users.table", userId,7);
		secretKey = ReadAndWrite.read(tableLocation + "users.table", userId,8);
		if(accessKey.isEmpty()||secretKey.isEmpty()){
			PrintWriter out = null;
			try {
				out = response.getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
			out.println("key is empty");
		} else{
			VMProvisionerAWS.initializeProvisioner(accessKey, secretKey,"dataview1","Dataview_key","ami-064ab7adf0e30b152");
			ArrayList<String> vms = null;
			try {
				vms = VMProvisionerAWS.getAvailableAndPendingInstIds();
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (String vmid:vms){
				VMProvisionerAWS.terminateInstance(vmid);
			}
			VMProvisionerAWS.deleteKeyPair("Dataview_key");
		}
	}
	
	/**
	 * retrieve the accessKey and secreKey information.
	 * @param userId
	 * @param response
	 * @throws JSONException
	 * @throws IOException
	 */
	public void getCloudSettingsDetail(String userId,HttpServletResponse response) throws JSONException, IOException {
		accessKey = ReadAndWrite.read(tableLocation + "users.table", userId,7);
		secretKey = ReadAndWrite.read(tableLocation + "users.table", userId,8);
		JSONObject json = new JSONObject();
		json.put("accessKey", accessKey);
		json.put("secretKey", secretKey);
		JSONObject result = new JSONObject();
		result.put("cloudSettinglist", json);
		System.out.println(result.toString(4));
		PrintWriter out = response.getWriter();
		out.println(json.toString(4));	
	}
	
	/**
	 * Write the accessKey and secretKey into the table.
	 * @param userID
	 * @param accessKey
	 * @param secretKey
	 * @throws UnsupportedEncodingException
	 */
	public void loadCloudSettings(String userID, String accessKey, String secretKey) throws UnsupportedEncodingException {
		if (accessKey != null && accessKey != ""&& secretKey!=null && secretKey != null ) {
			ReadAndWrite.write(tableLocation + "users.table", userID, accessKey, secretKey, 7, 8 );
		} 
		
	}
	
	/**
	 * Create a folder named with the user ID (unique) to store user's task files and workflow mxgraph files
	 */
	public void initializeUserFolder(HttpServletRequest request,HttpServletResponse response){
		System.out.println("In the initializaUserFolder "+ outputMapping);
		fileLocation = getServletContext().getRealPath(request.getServletPath()).replace("Mediator", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator + strUser;
		tableLocation = getServletContext().getRealPath(request.getServletPath()).replace("Mediator", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator;
		System.out.println(tableLocation);
		File file = new File(fileLocation);
		System.out.println(fileLocation);
		 if (!file.exists()) {
	            if (file.mkdir()) {
	                System.out.println("Directory is created!");
	            } else {
	                System.out.println("Failed to create directory!");
	            }
	        }else{
	        	System.out.println("The Directory is already exist");
	        }
		
	}
	
	/**
	 * write the maxgraph information of a workflow to each user's local file space, then upload to the Dropbox
	 * @param name
	 * @param diagramStr
	 * @param response
	 * @throws Exception
	 */
	public void saveAs(String name, String diagramStr, HttpServletResponse response) throws Exception {
		token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
		DbxRequestConfig config = new DbxRequestConfig("en_US");
		DbxClientV2 client = new DbxClientV2(config, token);
		String localFileAbsolutePath = fileLocation + File.separator + name;
		String dropboxPath = "/DATAVIEW/Workflows/" + name;
		if (!new File(localFileAbsolutePath).exists()) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(localFileAbsolutePath));
			writer.write(diagramStr);
			writer.close();
		}
		InputStream in = new FileInputStream(localFileAbsolutePath);
		client.files().uploadBuilder(dropboxPath).withMode(WriteMode.ADD).uploadAndFinish(in);
	}
	
	/**
	 * If a use entries the dropbox token information from webbench, the token will be write to the file, other token will be assigned from the file.
	 * @param userId
	 * @param token
	 * @param response
	 * @throws Exception
	 */
	public void loadDropboxKey(String userId, String token,
			HttpServletResponse response) throws Exception {
		if (token != null && token != "") {
			ReadAndWrite.write(tableLocation + "users.table", userId, token, 6);
		} else {
			token = ReadAndWrite.read(tableLocation + "users.table", userId, 6);
		}
	}

	/**
	 * Run the current workflow with the local workflow executor. 
	 * @param userId
	 * @param name
	 * @param response
	 * @throws Exception
	 */
	public void serverRunWorkflows(String userId, String name, HttpServletResponse response ) throws Exception{
		String localFileAbsolutePath = fileLocation + File.separator + name;
		DATAVIEW_BigFile bf = new DATAVIEW_BigFile(localFileAbsolutePath);
		Document diagram = Utility.XMLParser.getDocument(bf.toString());
		Document spec = MXGraphToSWLTranslator.translateExperiment(name, diagram);
		GenericWorkflow GW = new GenericWorkflow(spec,fileLocation);
		GW.design();
		WorkflowPlanner wp = new WorkflowPlanner_T_Cluster(GW);
		GlobalSchedule gsch = wp.plan();
		for(int i = 0 ; i<gsch.length(); i++){
			LocalSchedule lsch =  gsch.getLocalSchedule(i);
			for(int j = 0; j < lsch.length(); j++){
				TaskSchedule tsch = lsch.getTaskSchedule(j);
				dataview.models.JSONObject taskscheduleJson = tsch.getSpecification();
				JSONArray outdcs = taskscheduleJson.get("outgoingDataChannels").toJSONArray();
				for(int k = 0; k < outdcs.size(); k++){
					dataview.models.JSONObject outdc = outdcs.get(k).toJSONObject();
					if(!outdc.get("wout").isEmpty()){
						String outputindex = outdc.get("wout").toString().replace("\"", "");
						if(gsch.getWorkflow().wouts[Integer.parseInt(outputindex)].getClass().equals(DATAVIEW_BigFile.class)){
							String outputindexfilename = ((DATAVIEW_BigFile)gsch.getWorkflow().wouts[Integer.parseInt(outputindex)]).getFilename();
							outputMapping.put(outputindexfilename, outputindexfilename+ taskscheduleJson.get("taskInstanceID").toString().replace("\"", ""));
						}
					}
				}
			}	
		}
		token = ReadAndWrite.read(tableLocation + "users.table", strUser,6);
		WorkflowExecutor we = new WorkflowExecutor_Local(fileLocation+ File.separator,fileLocation+ File.separator,token, gsch);
		we.execute();	
	}
	
	/** design a workflow based on the workflow mxgraph information. T_cluster planner is used to generate a workflow planner, on which the execute method of beta executor is called.
	 * 
	 * @param userID
	 * @param name
	 * @param accessKey
	 * @param secretKey
	 * @param response
	 * @throws Exception
	 */
	public void provisionVMsInEC2AndRunWorkflows(String userID, String name, String accessKey, String secretKey,
			HttpServletResponse response) throws Exception {
		Dataview.debugger.logSuccessfulMessage("Inside vm provisioner java side...");
		String localFileAbsolutePath = fileLocation + File.separator + name;
		DATAVIEW_BigFile bf = new DATAVIEW_BigFile(localFileAbsolutePath);
		//Dataview.debugger.logSuccessfulMessage(bf.toString());
		Document diagram = Utility.XMLParser.getDocument(bf.toString());
		//Dataview.debugger.logSuccessfulMessage("The diagram value is "+ Utility.XMLParser.nodeToString(diagram));
		Document spec = MXGraphToSWLTranslator.translateExperiment(name, diagram);
		//Dataview.debugger.logSuccessfulMessage("The spec value is"+ Utility.XMLParser.nodeToString(spec));
		//Dataview.debugger.logObjectValue("fileLocation is ", fileLocation);
		GenericWorkflow GW = new GenericWorkflow(spec,fileLocation);
		GW.design();
		Dataview.debugger.logObjectValue("the workflow object is ", GW.getWorkflowSpecification());
		WorkflowPlanner wp = new WorkflowPlanner_T_Cluster(GW);
		GlobalSchedule gsch = wp.plan();
		Dataview.debugger.logObjectValue("the global schedule ", gsch.getSpecification());
		System.out.println("the global schedule " +  gsch.getSpecification());
		for(int i = 0 ; i<gsch.length(); i++){
			LocalSchedule lsch =  gsch.getLocalSchedule(i);
			for(int j = 0; j < lsch.length(); j++){
				TaskSchedule tsch = lsch.getTaskSchedule(j);
				dataview.models.JSONObject taskscheduleJson = tsch.getSpecification();
				JSONArray outdcs = taskscheduleJson.get("outgoingDataChannels").toJSONArray();
				for(int k = 0; k < outdcs.size(); k++){
					dataview.models.JSONObject outdc = outdcs.get(k).toJSONObject();
					if(!outdc.get("wout").isEmpty()){
						String outputindex = outdc.get("wout").toString().replace("\"", "");
						if(gsch.getWorkflow().wouts[Integer.parseInt(outputindex)].getClass().equals(DATAVIEW_BigFile.class)){
							String outputindexfilename = ((DATAVIEW_BigFile)gsch.getWorkflow().wouts[Integer.parseInt(outputindex)]).getFilename();
							outputMapping.put(outputindexfilename, outputindexfilename+ taskscheduleJson.get("taskInstanceID").toString().replace("\"", ""));
						}
					}
				}
				
			}	
		}
		System.out.println("In the provisionVMsInEC2AndRunWorkflows " + outputMapping);
		token = ReadAndWrite.read(tableLocation + "users.table", strUser,6);
		String accesskey = ReadAndWrite.read(tableLocation + "users.table", strUser,7);
		String secretkey = ReadAndWrite.read(tableLocation + "users.table", strUser,8);
		WorkflowExecutor we = new WorkflowExecutor_Beta(fileLocation + File.separator, fileLocation + File.separator, 
				gsch, token, accesskey, secretkey);
		Dataview.debugger.logSuccessfulMessage("The workflowExecutor constructor is created");
		we.execute();
		PrintWriter out = response.getWriter();
		out.println("Workflow Running Successfully");
	}
	/**
	 * If the mxgraph of a workflow is changed, the file located in the Dropbox should be deleted and a new file should be created and uploaded to Dropbox.
	 * @param name
	 * @param diagramStr
	 * @param response
	 * @throws Exception
	 */
	public void overwriteAndSave(String name, String diagramStr, HttpServletResponse response) throws Exception {
		token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
		DbxRequestConfig config = new DbxRequestConfig("en_US");
		DbxClientV2 client = new DbxClientV2(config, token);
		String localFileAbsolutePath = fileLocation + File.separator + name;
		String dropboxPath = "/DATAVIEW/Workflows/" + name;
		FileMetadata deleteFile = (FileMetadata) client.files().delete(dropboxPath);
		if (!new File(localFileAbsolutePath).exists()) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(localFileAbsolutePath));
			writer.write(diagramStr);
			writer.close();
		}else{
			new File(localFileAbsolutePath).delete();
			BufferedWriter writer = new BufferedWriter(new FileWriter(localFileAbsolutePath));
			writer.write(diagramStr);
			writer.close();
		}
		InputStream in = new FileInputStream(localFileAbsolutePath);
		client.files().uploadBuilder(dropboxPath).withMode(WriteMode.ADD).uploadAndFinish(in);
	
	}

	/**
	 * Download the workflow mxgraph from Dropbox 
	 * @param filename
	 * @param response
	 * @throws Exception
	 */
	public void getWorkflowDiagram(String filename, HttpServletResponse response) throws Exception {
		String workflowfilename = filename.substring(filename.lastIndexOf("/")+1);
		String localFileAbsolutePath = fileLocation + File.separator + workflowfilename;
		if (!new File(localFileAbsolutePath).exists()) {
			token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
			DbxRequestConfig config = new DbxRequestConfig("en_US");
			DbxClientV2 client = new DbxClientV2(config, token);
			String dropBoxFilePath = filename;
			DbxDownloader<FileMetadata> dl = null;
			try {
				dl = client.files().download(dropBoxFilePath);
			} catch (DownloadErrorException e) {
				String str = e.getMessage();
				if (str.contains("\"path\":\"not_found\"")) {
					PrintWriter out = response.getWriter();
					out.println("the specification file is not exist");
					return;
				}
			}
			FileOutputStream fOut = new FileOutputStream(localFileAbsolutePath);
			Dataview.debugger.logSuccessfulMessage("Downloading .... " + dropBoxFilePath);
			dl.download(fOut);
			Dataview.debugger.logSuccessfulMessage("Downloading .... finished");
		}
		DATAVIEW_BigFile bf = new DATAVIEW_BigFile(localFileAbsolutePath);
		PrintWriter out = response.getWriter();
		out.println(bf.toString());
	}

	/**
	 * Retrieve the Dropbox token information and will be used to show the tree elements in the webbench.
	 * @param userID
	 * @param response
	 * @throws Exception
	 */
	public static void getDropboxDetails(String userID, HttpServletResponse response) throws Exception {
		token = ReadAndWrite.read(tableLocation + "users.table", userID,6);
		JSONObject json = new JSONObject();
		json.put("token", token);
		JSONObject result = new JSONObject();
		result.put("dropboxlist", json);
		System.out.println(result.toString(4));
		PrintWriter out = response.getWriter();
		out.println(json.toString(4));
	}
	/**
	 * Download the task source file (class or jar format) and retrieve the task specification information and will be used to retrieve the input ports number and output ports number
	 * @param filename
	 * @param response
	 * @throws Exception
	 */
	public static void getPortsNumber(String filename, HttpServletResponse response) throws Exception {
		String task = filename.substring(filename.lastIndexOf("/")+1);
		String taskName = task.substring(0, task.lastIndexOf("."));
		String localFileAbsolutePath = fileLocation + File.separator + task;		
		String Location;
		if (!new File(localFileAbsolutePath).exists()) {
			token = ReadAndWrite.read(tableLocation + "users.table", strUser,6);
			DbxRequestConfig config = new DbxRequestConfig("en_US");
			DbxClientV2 client = new DbxClientV2(config, token);
			String dropBoxFilePath = filename;
			DbxDownloader<FileMetadata> dl = null;
			try {
				dl = client.files().download(dropBoxFilePath);
			} catch (DownloadErrorException e) {
				String str = e.getMessage();
				if (str.contains("\"path\":\"not_found\"")) {
					PrintWriter out = response.getWriter();
					out.println("the specification file is not exist");
					return;
				}
			}
			FileOutputStream fOut = new FileOutputStream(localFileAbsolutePath);
			System.out.println("Downloading .... " + dropBoxFilePath);
			dl.download(fOut);
			System.out.println("Downloading .... finished");
		}
		if(new File( fileLocation + File.separator + taskName + ".jar").exists()){
			Location = fileLocation + File.separator + taskName + ".jar";
		}
		else{
			Location = fileLocation;
		}
		File clazzPath = new File(Location);
		Task newtask = null; 		
		try {
			URL url = null;
			url = clazzPath.toURI().toURL();
			URL[] urls = new URL[] { url };
			Thread.currentThread().setContextClassLoader(new URLClassLoader(urls,Thread.currentThread().getContextClassLoader()));
			ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
			Class<?> taskclass = Class.forName(taskName, true, currentClassLoader);
			newtask = (Task) taskclass.getDeclaredConstructor().newInstance();
			Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
		} catch (NoSuchMethodException | SecurityException |  IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
		PrintWriter out = response.getWriter();
		out.println(newtask.getTaskSpecification());
	}
	
	/**
	 * Download the final output files of a workflow from Dropbox and show the content in the webbench if the size is less than 1024.
	 * @param dataName
	 * @param response
	 * @throws Exception
	 */
public static void getData(String dataName, HttpServletResponse response) throws Exception {
		String filename = dataName.substring(dataName.lastIndexOf("/")+1);
		System.out.println("This is the file name: before "+ filename);
		if (!new File(fileLocation + File.separator + filename).exists()){
			filename = (String) outputMapping.get(filename);
			System.out.println("This is the file name: "+ filename);
		}
		String localFileAbsolutePath = fileLocation + File.separator + filename;	
		System.out.println(localFileAbsolutePath);
		if (!new File(localFileAbsolutePath).exists()) {
			token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
			DbxRequestConfig config = new DbxRequestConfig("en_US");
			DbxClientV2 client = new DbxClientV2(config, token);
			String dropBoxFilePath = dataName.substring(0,dataName.lastIndexOf("/")+1)+filename;
			System.out.println(dropBoxFilePath);
			DbxDownloader<FileMetadata> dl = null;
			try {
				dl = client.files().download(dropBoxFilePath);
			} catch (DownloadErrorException e) {
				String str = e.getMessage();
				if (str.contains("\"path\":\"not_found\"")) {
					PrintWriter out = response.getWriter();
					out.println("the output file is not exist");
					return;
				}
			}
			FileOutputStream fOut = new FileOutputStream(localFileAbsolutePath);
			System.out.println("Downloading .... " + dropBoxFilePath);
			dl.download(fOut);
			System.out.println("Downloading .... finished");
		}
		File file =new File(localFileAbsolutePath);
		PrintWriter out = response.getWriter();
		if(file.length()<1024){
			DATAVIEW_BigFile bf = new DATAVIEW_BigFile(localFileAbsolutePath);
			out.println(bf.toString());
		}else{
			out.println("TOO MUCH DATA Please Go To "+ dataName.substring(0,dataName.lastIndexOf("/"))+filename);
		}
	}
	

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Mediator() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			HttpSession session = request.getSession(true);
			strUser = session.getAttribute("UserID").toString();
			processRequest(request, response);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			
			HttpSession session = request.getSession(true);
			strUser = session.getAttribute("UserID").toString();
			processRequest(request, response);
			

		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	
}

 class TrippleDes {

    private static final String UNICODE_FORMAT = "UTF8";
    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private KeySpec ks;
    private SecretKeyFactory skf;
    private Cipher cipher;
    byte[] arrayBytes;
    private String myEncryptionKey;
    private String myEncryptionScheme;
    SecretKey key;

    public TrippleDes() throws Exception {
        myEncryptionKey = "ThisIsSpartaThisIsSparta";
        myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;
        arrayBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
        ks = new DESedeKeySpec(arrayBytes);
        skf = SecretKeyFactory.getInstance(myEncryptionScheme);
        cipher = Cipher.getInstance(myEncryptionScheme);
        key = skf.generateSecret(ks);
    }


    public String encrypt(String unencryptedString) {
        String encryptedString = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
            byte[] encryptedText = cipher.doFinal(plainText);
            encryptedString = new String(Base64.encodeBase64(encryptedText));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedString;
    }


    public String decrypt(String encryptedString) {
        String decryptedText=null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedText = Base64.decodeBase64(encryptedString);
            byte[] plainText = cipher.doFinal(encryptedText);
            decryptedText= new String(plainText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedText;
    }
}
