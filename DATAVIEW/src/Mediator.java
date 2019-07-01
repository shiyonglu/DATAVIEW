
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
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import Utility.MXGraphToSWLTranslator;
import dataview.models.*;
import dataview.planners.WorkflowPlanner;
import dataview.planners.WorkflowPlanner_Naive2;
import dataview.planners.WorkflowPlanner_T_Cluster;
import dataview.workflowexecutors.VMProvisioner;
import dataview.workflowexecutors.WorkflowExecutor;
import dataview.workflowexecutors.WorkflowExecutor_Beta;
import dataview.workflowexecutors.WorkflowExecutor_Local;

/**
 * This is the main servlet that connects webbench frontend and 
 * backend via ajax calls.
 * 
 */

public class Mediator extends HttpServlet {
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
		System.out.println("In the mediator "+ path);
		Dataview.setDebugger(path+"dataview.log");
		Dataview.debugger.setDisplay(true);
		String action = request.getParameter("action");
		System.out.println("action: " + action);
		// Initialize a folder for each user
		if(action.equals("initializeUserFolder")){
			System.out.println("Initialize Each User Storage Space");
			initializeUserFolder(request,response);
		// Save the composed workflow mxgrah information in Dropbox
		}else if (action.equals("saveAs")) {
			System.out.println("save as recognized");
			saveAs(request.getParameter("name"), request.getParameter("diagram"), response);
		// Write the Dropbox Key into local file	
		} else if (action.equals("loadDropboxKey")) {
			System.out.println("load Dropbox Keys ");
			loadDropboxKey(request.getParameter("userId"), request.getParameter("token"), response);
		// Run a workflow with Amazon EC2	
		} else if (action.equals("provisionVMsInEC2AndRunWorkflows")) {
			System.out.println("provision vms request recieved from webench ");
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
		}
		else {
			System.out.println("undefined operation!!!!!!!!!!!!!");
		}

	}
	
	
	
	
	public void stopVMs(String userId,HttpServletResponse response) {
		accessKey = ReadAndWrite.read(tableLocation + "users.table", userId,7);
		secretKey = ReadAndWrite.read(tableLocation + "users.table", userId,8);
		if(accessKey.isEmpty()||secretKey.isEmpty()){
			PrintWriter out = null;
			try {
				out = response.getWriter();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			out.println("key is empty");
		} else{
			VMProvisioner.initializeProvisioner(accessKey, secretKey,"dataview1","Dataview_key","ami-064ab7adf0e30b152");
			ArrayList<String> vms = null;
			try {
				vms = VMProvisioner.getAvailableAndPendingInstIds();
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (String vmid:vms){
				VMProvisioner.terminateInstance(vmid);
			}
			VMProvisioner.deleteKeyPair("Dataview_key");
		}
		
		
		//System.out.println(vms);
		
	}
	
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

	public void loadCloudSettings(String userID, String accessKey, String secretKey) throws UnsupportedEncodingException {
		
		if (accessKey != null && accessKey != ""&& secretKey!=null && secretKey != null ) {
			ReadAndWrite.write(tableLocation + "users.table", userID, accessKey, secretKey, 7, 8 );
		} 
		
	}
	
	/*
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


	public void loadDropboxKey(String userId, String token,
			HttpServletResponse response) throws Exception {
		System.out.println("token---->" + token);
		
		if (token != null && token != "") {
			ReadAndWrite.write(tableLocation + "users.table", userId, token, 6);
		} else {
			token = ReadAndWrite.read(tableLocation + "users.table", userId, 6);
		}
	}

	
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
							//outdc.put("destFilename",new JSONValue(outputindexfilename));
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
	
	
	
	
	public void provisionVMsInEC2AndRunWorkflows(String userID, String name, String accessKey, String secretKey,
			HttpServletResponse response) throws Exception {
		Dataview.debugger.logSuccessfulMessage("Inside vm provisioner java side...");
		System.out.println("Inside vm provisioner java side...");
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
		//WorkflowPlanner wp = new WorkflowPlanner_Naive2(GW);
		
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
							//outdc.put("destFilename",new JSONValue(outputindexfilename));
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
	public void overwriteAndSave(String name, String diagramStr, HttpServletResponse response) throws Exception {
		System.out.println("=======overwriteandsave");
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
			System.out.println("Downloading .... " + dropBoxFilePath);
			dl.download(fOut);
			System.out.println("Downloading .... finished");
		}
		System.out.println(localFileAbsolutePath);
		DATAVIEW_BigFile bf = new DATAVIEW_BigFile(localFileAbsolutePath);
		PrintWriter out = response.getWriter();
		out.println(bf.toString());
		
	}


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
		Method method = null;
		
		URLClassLoader classLoader = null;
		try {
			method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			boolean accessible = method.isAccessible();
			if(accessible == false){
				method.setAccessible(true);
			}
			classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
			 try {
				method.invoke(classLoader, clazzPath.toURI().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Dataview.debugger.logException(e);
			}
			 
		} catch (NoSuchMethodException | SecurityException |  IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}finally {
			method.setAccessible(false);
		}
    	Task newtask = null; 
    	Class<?> taskclass;
		try {
			taskclass = Class.forName(taskName);
			newtask =  (Task) taskclass.newInstance();	
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (InstantiationException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (IllegalAccessException e) {
			System.out.println("Exception, possible reason: the constructor of class "+ taskName +" is not public.");
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
		PrintWriter out = response.getWriter();
		out.println(newtask.getTaskSpecification());
	}
	
	
public static void getData(String dataName, HttpServletResponse response) throws Exception {
		String filename = dataName.substring(dataName.lastIndexOf("/")+1);
		System.out.println("This is the file name: before "+ filename);
		filename = (String) outputMapping.get(filename);
		System.out.println("This is the file name: "+ filename);
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
		
		System.out.println(localFileAbsolutePath);
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
		System.out.println("doGet call");
		try {
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
