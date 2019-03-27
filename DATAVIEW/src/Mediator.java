


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
import dataview.workflowexecutors.WorkflowExecutor;
import dataview.workflowexecutors.WorkflowExecutor_Beta;

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
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws Exception, ServletException, IOException {
		response.setContentType("text/plain");
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
			loadDropboxKey(request.getParameter("userId"), request.getParameter("appKey"),
					request.getParameter("secretKey"), request.getParameter("token"), response);
		// Run a workflow with Amazon EC2	
		} else if (action.equals("provisionVMsInEC2AndRunWorkflows")) {
			System.out.println("provision vms request recieved from webench ");
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
		}
		else {
			System.out.println("undefined operation!!!!!!!!!!!!!");
		}

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
			ReadAndWrite.write(tableLocation + "users.table", userID, accessKey, secretKey);
		} 
		
	}
	
	/*
	 * Create a folder named with the user ID (unique) to store user's task files and workflow mxgraph files
	 */
	
	public void initializeUserFolder(HttpServletRequest request,HttpServletResponse response){
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


	public void loadDropboxKey(String userId, String appKey, String secretKey, String token,
			HttpServletResponse response) throws Exception {
		System.out.println("token---->" + token);
		
		if (token != null && token != "") {
			ReadAndWrite.write(tableLocation + "users.table", userId, token);
		} else {
			token = ReadAndWrite.read(tableLocation + "users.table", userId, 6);
		}
	}

	
	public void provisionVMsInEC2AndRunWorkflows(String userID, String name, String accessKey, String secretKey,
			HttpServletResponse response) throws Exception {
		System.out.println("Inside vm provisioner java side...");
		String localFileAbsolutePath = fileLocation + File.separator + name;
		DATAVIEW_BigFile bf = new DATAVIEW_BigFile(localFileAbsolutePath);
		Document diagram = Utility.XMLParser.getDocument(bf.toString());
		Document spec = MXGraphToSWLTranslator.translateExperiment(name, diagram);
		
		GenericWorkflow GW = new GenericWorkflow(spec,fileLocation);
		GW.design();
		//WorkflowPlanner wp = new WorkflowPlanner_Naive2(GW);
		WorkflowPlanner wp = new WorkflowPlanner_T_Cluster(GW);
		GlobalSchedule gsch = wp.plan();
		
		token = ReadAndWrite.read(tableLocation + "users.table", strUser,6);
		String accesskey = ReadAndWrite.read(tableLocation + "users.table", strUser,7);
		String secretkey = ReadAndWrite.read(tableLocation + "users.table", strUser,8);
		WorkflowExecutor we = new WorkflowExecutor_Beta(fileLocation + File.separator, fileLocation + File.separator, 
				gsch, token, accesskey, secretkey);
		
		we.execute();
		PrintWriter out = response.getWriter();
		out.println("SUCCESS");
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
		String localFileAbsolutePath = fileLocation + File.separator + filename;	
		if (!new File(localFileAbsolutePath).exists()) {
			token = ReadAndWrite.read(tableLocation + "users.table", strUser, 6);
			DbxRequestConfig config = new DbxRequestConfig("en_US");
			DbxClientV2 client = new DbxClientV2(config, token);
			String dropBoxFilePath = dataName;
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
		File file =new File(localFileAbsolutePath);
		PrintWriter out = response.getWriter();
		if(file.length()<1024){
			DATAVIEW_BigFile bf = new DATAVIEW_BigFile(localFileAbsolutePath);
			out.println(bf.toString());
		}else{
			out.println("TOO MUCH DATA Please Go To "+ dataName);
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
