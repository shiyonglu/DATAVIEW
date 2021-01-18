package dataview.workflowexecutors;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import dataview.models.Dataview;
import dataview.models.JSONArray;
import dataview.models.JSONObject;
import dataview.models.JSONParser;
import dataview.models.JSONValue;
import dataview.models.Task;


/**	A genetal introduction to TaskExecutor.
 * 	The task executor will bind the data products with input ports and output ports
 *  It will call a specific task to read data from the input port, do data processing and write data to corresponding outputs.
 *  
 *  Each input port of a task is like an incoming mailbox. The task executor will put data products at the input ports of a task,then the
 *  task will read the data products from the input ports, process them and then write the results as data products to the task's output 
 *  ports, which server as outgoing mailboxex. Afterwards, the task executor will move the data products at the output ports to the VMs running 
 *  child tasks. 
 *  
 *  
 */
public class TaskExecutor_Beta {
	ServerSocket providerSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	Socket connection;
	

	public TaskExecutor_Beta() throws ClassNotFoundException, SQLException {
		try {
			providerSocket = new ServerSocket(2004, 10);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Retrieve the input file name for each task based on the task specification information and combine it to the input port index. The input port index is the key value of inputportAndFile object and
	 *  the value is the file name. When the dropboxToken is not empty and the task has no parent task, the input files associated with the task should be downloaded from the Dropbox. 
	 * 
	 * @param taskSpec
	 * @param dropboxToken
	 * @return   
	 * @throws DbxException
	 * @throws IOException
	 */
	public SortedMap<String, String> mappingInputAndFile (JSONObject taskSpec,String dropboxToken) throws DbxException, IOException{
		JSONArray indcs = taskSpec.get("incomingDataChannels").toJSONArray();
		SortedMap<String, String> inputportAndFile = new TreeMap<String, String>();
		for(int i = 0; i< indcs.size(); i++){
			JSONObject indc = indcs.get(i).toJSONObject();
			if(indc.get("srcTask").isEmpty()){
				String inputfile = indc.get("srcFilename").toString().replace("\"", ""); 
				if(!dropboxToken.isEmpty()){
					DbxRequestConfig config = new DbxRequestConfig("en_US");
					DbxClientV2 client = new DbxClientV2(config, dropboxToken);
					DbxDownloader<FileMetadata> dl = null;
					try {
						dl = client.files().download("/DATAVIEW-INPUT/"+inputfile);
					} catch (DownloadErrorException e) {
						Dataview.debugger.logException(e);
					}
					FileOutputStream fOut = new FileOutputStream(inputfile);
					dl.download(fOut);
				}
				inputportAndFile.put(indc.get("myInputPortIndex").toString().replace("\"", ""),
						inputfile);
				
			}else{
				inputportAndFile.put(indc.get("myInputPortIndex").toString().replace("\"", ""), 
						indc.get("srcTask").toString().replace("\"", "")+"_"+ 
						indc.get("outputPortIndex").toString().replace("\"", "")+".txt");
			}
		}
		Dataview.debugger.logObjectValue(" the inputportAndFile value is ", inputportAndFile);
		return inputportAndFile;
	}
	
	/** Retrieve the output file name for each task based on the task specification information and combine it to the output port index.
	 * 
	 * @param taskSpec
	 * @param dropboxToken
	 * @return
	 * @throws DbxException
	 * @throws IOException
	 */
	public SortedMap<String, String> mappingOutputAndFile (JSONObject taskSpec,String dropboxToken) throws DbxException, IOException{
		JSONArray outdcs = taskSpec.get("outgoingDataChannels").toJSONArray();
		SortedMap<String, String>  outputportAndFile = new TreeMap<String, String>();
		for(int i = 0; i < outdcs.size(); i++){
			JSONObject outdc = outdcs.get(i).toJSONObject();
			if(outdc.get("destTask").isEmpty()){
				String file = outdc.get("destFilename").toString().replace("\"", "");
				outputportAndFile.put(outdc.get("myOutputPortIndex").toString().replace("\"", ""), 
						file);
			}else{
				outputportAndFile.put(outdc.get("myOutputPortIndex").toString().replace("\"", ""),
						taskSpec.get("taskInstanceID").toString().replaceAll("\"", "")+"_"+
				outdc.get("myOutputPortIndex").toString().replaceAll("\"", "")+".txt");
				
				
			}
		}
		Dataview.debugger.logObjectValue(" the outputportAndFile value is ", outputportAndFile);
		return outputportAndFile;
		
	}
	/** Load a class dynamically at runtime and create a Task instance. After setting the file names to the input ports and output ports, the run method is called to execute a task.
	 * 
	 * @param taskSpec
	 * @param inputportAndFile
	 * @param outputportAndFile
	 * @throws MalformedURLException
	 */
	public void execute(JSONObject taskSpec, SortedMap<String, String>  inputportAndFile,SortedMap<String, String>  outputportAndFile) throws MalformedURLException{
	String taskName = taskSpec.get("taskName").toString().replace("\"", "");	
	Task t = null;
		if (new File("/home/ubuntu/" + taskName + ".jar").exists()) {
			try {
				File f = new File("/home/ubuntu/" + taskName + ".jar");
				URL url = null;
				url = f.toURI().toURL();
				URL[] urls = new URL[] { url };
				Thread.currentThread().setContextClassLoader(new URLClassLoader(urls,Thread.currentThread().getContextClassLoader()));
				ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
				Class<?> taskclass = Class.forName(taskName, true, currentClassLoader);
				t = (Task) taskclass.getDeclaredConstructor().newInstance();
				Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
			} catch (Exception e) {
				Dataview.debugger.logException(e);
				e.printStackTrace();
			}
		}
		else{
			File file = new File("/home/ubuntu/"); 
			URL url = file.toURI().toURL(); 
			URL[] urls = new URL[]{url};
			ClassLoader cl = new URLClassLoader(urls);  //load this folder into Class loader
			try{
				Class<?> taskclass = Class.forName(taskName,true,cl);
				t = (Task) taskclass.newInstance();
			}
			catch (Exception e) {
				Dataview.debugger.logException(e);
				e.printStackTrace();
			}
			
		}
		
		//  bind the input file names to the input ports
		for (int i = 0; i < t.ins.length; i++) {
			t.ins[i].setLocation(inputportAndFile.get(i+""));
			
		}
		// bind the output file names to the output ports
		for (int i = 0; i < t.outs.length; i++) {
			t.outs[i].setLocation(outputportAndFile.get(i+""));
		}
		Dataview.debugger.logSuccessfulMessage("Task "+ t.taskName + " start to run");
		long startTime = System.nanoTime();
		t.run();
		long endTime = System.nanoTime();
		double duration = (double)(endTime - startTime) / 1000000000.0;	
		taskSpec.put("execTime", new JSONValue(Double.toString(duration)));
		Dataview.debugger.logSuccessfulMessage("Task "+t.taskName + " is finished");	
	}
	/** Move the output files from one task to its child tasks in parallel, if the task has child tasks. If the dropbox token is provided, the final output will move to
	 *  the Dropbox "/DATAVIEW-OUTPUT/" folder parallel.
	 * 
	 * @param taskSpec
	 * @param dropboxToken
	 * @throws InterruptedException
	 */
	public void dataMove(JSONObject taskSpec,String dropboxToken) throws InterruptedException{
		String taskID = taskSpec.get("taskInstanceID").toString().replace("\"", "");
		JSONArray outdcs = taskSpec.get("outgoingDataChannels").toJSONArray();
		//ArrayList<DataTrasnferThread> threads = new ArrayList<DataTrasnferThread>();
		ArrayList<Runnable> dataTransferThreads = new ArrayList<Runnable>();
		for(int i = 0; i < outdcs.size(); i++){
			final JSONObject outdc = outdcs.get(i).toJSONObject();
			if(!outdc.get("destIP").toString().replaceAll("\"", "").
					equals(taskSpec.get("myIP").toString().replaceAll("\"", "")) && !outdc.get("destIP").isEmpty() ){
				final String taskInstanceID = taskSpec.get("taskInstanceID").toString();
				final String outputPortIndex = outdc.get("myOutputPortIndex").toString();
				final String destIP = outdc.get("destIP").toString();
				Runnable vmToVM = new Runnable() {
					public void run() {
						try {
							long start = System.nanoTime();
							String fileName = taskInstanceID.replaceAll("\"", "")+"_"+
									outputPortIndex.replaceAll("\"", "")+".txt";
							MoveDataToCloud.getDataReady(fileName, 
									destIP.replaceAll("\"", ""));
							outdc.put("dataTransferTime", new JSONValue(Double.toString((double)(System.nanoTime() - start) / 1000000000.0)));
							long fileSize = Files.size(Paths.get("/home/ubuntu/" + fileName));
							outdc.put("outputDatasize", new JSONValue(Double.toString(fileSize)));
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				Thread transferVMtoVM = new Thread(vmToVM);
				//DataTrasnferThread thread = new DataTrasnferThread(task, outdc);
				dataTransferThreads.add(transferVMtoVM);
				transferVMtoVM.start();
			}else if(outdc.get("destTask").isEmpty()){ // if it is an exit task 						
				if(!dropboxToken.isEmpty()){                  // if the DropboxToken is present, then we send the workflow outputs to the Dropbox file system
					final String tokenForThread = dropboxToken;
					final String destFilename = outdc.get("destFilename").toString();
					final String taskIDForThread = taskID;
					Runnable vmToDropbox = new Runnable()  {
						public void run() {
							try {
								// update the final output to Dropbox 
								DbxRequestConfig config = new DbxRequestConfig("en_US");
								DbxClientV2 client = new DbxClientV2(config, tokenForThread);
								String localFileAbsolutePath = destFilename.replaceAll("\"", "");
								String dropboxPath = "/DATAVIEW-OUTPUT/" + localFileAbsolutePath+taskIDForThread;
								InputStream in = new FileInputStream(localFileAbsolutePath);
								client.files().uploadBuilder(dropboxPath).withMode(WriteMode.ADD).uploadAndFinish(in);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
					Thread transferVMtoDropbox = new Thread(vmToDropbox);
					dataTransferThreads.add(transferVMtoDropbox);
					transferVMtoDropbox.start();
				}

				
			}else {
				outdc.put("transTime", new JSONValue(Double.toString(0.0)));
				String taskInstanceID = taskSpec.get("taskInstanceID").toString();
				String outputPortIndex = outdc.get("myOutputPortIndex").toString();
				String fileName = taskInstanceID.replaceAll("\"", "")+"_"+
						outputPortIndex.replaceAll("\"", "")+".txt";
				try{
					long fileSize = Files.size(Paths.get("/home/ubuntu/" + fileName));
					outdc.put("outputDatasize", new JSONValue(Double.toString(fileSize)));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		}
		// the main thread will wait until all the threads are finished.
		for (Runnable dataTransferThread : dataTransferThreads) {
			((Thread) dataTransferThread).join();
		}
		
	}
	/** we use a do-while loop to receive one TaskScheudle specification at a time, we do not need to receive the code for the task because the code of the task
	 *  has already been transfered to this VM by the workflow executor right after VM provisioning.
	 * 
	 */
	public void run() {
		try {
			do {     
				
				// step 1: establish a TCP connection on port: 2004, and waits until a client connects to the server on 2004 port.
				Dataview.debugger
						.logSuccessfulMessage(InetAddress.getLocalHost().getHostName() + " is waiting for connection");
				connection = providerSocket.accept();
				Dataview.debugger
						.logSuccessfulMessage("New connection accepted " +
								connection.getInetAddress() + ":" +connection.getPort() );
				out = new ObjectOutputStream(connection.getOutputStream());
				in  = new ObjectInputStream(connection.getInputStream());
				try {
					
					// Step 2: receipt a task specification 
					Message message = (Message) in.readObject();
					// the task specification will be record in p.
					JSONParser p = new JSONParser(message.getB());
					// the dropbox token information will be record in dropboxToken.
					String dropboxToken = message.getA();
					
					// step 3: parse the task specification 
					Dataview.debugger.logSuccessfulMessage("receive the task specification:");
					Dataview.debugger.logSuccessfulMessage(message.getB());
					JSONObject taskSpec = p.parseJSONObject();
					
					

					/** Each port will be mapped to a unique txt file storing the data product for that input port.
					 * 	The input file names and output file names of the whole workflow are already available in the worklflow specification.
					 * These file names will be used for mapping for the inputports/outputports of those tasks which take them as input or output.   
					 * 
					 *  
					 * 	Other input/output file names of this task will be  created by this TaskExecutor based on the taskInstanceID and the output port id in 
					 * 	the incomingDataChannels in the task specification. 
					 * 
					 *  Step 4: Two mappings,inputportAndFile and outputportAndFile, are used to map file names to inputport and outputport indexes of the current task.
					 */
					
					SortedMap<String, String> inputportAndFile = mappingInputAndFile(taskSpec,dropboxToken);
					SortedMap<String, String> outputportAndFile = mappingOutputAndFile(taskSpec,dropboxToken);
					
					// step 5: instantiate a task object t of the current task class tj and then call the t.run() method
					
					execute(taskSpec,inputportAndFile,outputportAndFile);
					
			
					// step 6: Transfer all the data products produced by this task to the VMs of their child tasks in parallel
					
					dataMove(taskSpec,dropboxToken);
					
					
					// step 7: send back the execution status of the task back to the WorkflowExecutor and close the connection. 
					
					Dataview.debugger.logSuccessfulMessage("Here is the task specification "+ taskSpec);
					out.writeObject(taskSpec.toString());
					out.flush();	
					in.close();
					out.close();
					connection.close();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (true);
			
			
		} catch (IOException ioException) {
			Dataview.debugger.logException(ioException);
			ioException.printStackTrace();
		} 
		try {
			providerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
