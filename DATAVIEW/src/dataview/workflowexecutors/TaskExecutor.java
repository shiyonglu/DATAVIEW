package dataview.workflowexecutors;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import com.dropbox.core.DbxDownloader;
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


/**	The task executor will bind the data products with input ports and output ports
 *  It will call a specific task to read data from the input port, do data processing and write data to corresponding outputs.
 *  
 *  Each input port of a task is like an incoming mailbox. The task executor will put data products of all input ports,then the
 *  task will take the data product from the input port, process it and then write the result as data products deposited at an output 
 *  port, which servers as an outgoing mailbox. The task executor will move the data products at the output port to the VM running 
 *  downstream task.
 */
public class TaskExecutor {
	ServerSocket providerSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	Socket connection;
	//   Change the name to datamovement.
	public class DatamovementCallback {
		private JSONObject outdc;
		public DatamovementCallback(JSONObject dc) {
			outdc = dc;
		}
		public void onThreadFinished(double duration) {
			outdc.put("transTime", new JSONValue(Double.toString(duration)));
		}
	}
	/** If a output will transfer to the task's children tasks, each transfer will execute in one thread. Multiple final output will transfer
	 	to the user's dropbox folder in a parallel manner. 
	*/
	public class TransformThread extends Thread {
		private DatamovementCallback threadCallback;
		private Runnable runable;
		/**
		 * The constructor for each thread will take a Runnable object and callback object
		 * @param task: has a override run() method will move data from one VM instance to another VM instance.
		 * @param callback: has the method to record the data transfer time between different VMs. 
		 */
		public TransformThread(Runnable task, DatamovementCallback callback) {
			runable = task;
			threadCallback = callback;		
		}
		@Override
		public void run() {
			long start = System.nanoTime();
			runable.run();
			if (null != threadCallback) {
				threadCallback.onThreadFinished((double)(System.nanoTime() - start) / 1_000_000_000.0);
			}
		}
	}
	
	public TaskExecutor() throws ClassNotFoundException, SQLException {
		try {
			providerSocket = new ServerSocket(2004, 10);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			do {
				Dataview.debugger
						.logSuccessfulMessage(InetAddress.getLocalHost().getHostName() + " is waiting for connection");
				connection = providerSocket.accept();
				Dataview.debugger
						.logSuccessfulMessage("New connection accepted " +
								connection.getInetAddress() + ":" +connection.getPort() );
				out = new ObjectOutputStream(connection.getOutputStream());
				in  = new ObjectInputStream(connection.getInputStream());
				try {
					Message message = (Message) in.readObject();
					// the task specification will be record in p.
					JSONParser p = new JSONParser(message.getB());
					// the dropbox token information will be record in token.
					String token = message.getA();
					Dataview.debugger.logSuccessfulMessage("receive the task specification:");
					Dataview.debugger.logSuccessfulMessage(message.getB());
					JSONObject taskSpec = p.parseJSONObject();
					String taskName = taskSpec.get("taskName").toString().replace("\"", "");
					String taskID = taskSpec.get("taskInstanceID").toString().replace("\"", "");
					JSONArray indcs = taskSpec.get("incomingDataChannels").toJSONArray();
					final JSONArray outdcs = taskSpec.get("outgoingDataChannels").toJSONArray();
					/** Each port will be mapped with an unique txt file storing all the data.
					 * 	The input files names and output files names are already given.
					 * 	The intermediate file name is created based on the taskID and the output port id in 
					 * 	the incomingDataChannels in the task specification. 
					 */
					SortedMap<String, String> inputportAndFile = new TreeMap<String, String>();
					SortedMap<String, String>  outputportAndFile = new TreeMap<String, String>();
					for(int i = 0; i< indcs.size(); i++){
						JSONObject indc = indcs.get(i).toJSONObject();
						if(indc.get("srcTask").isEmpty()){
							String inputfile = indc.get("srcFilename").toString().replace("\"", ""); 
							if(!token.isEmpty()){
								DbxRequestConfig config = new DbxRequestConfig("en_US");
								DbxClientV2 client = new DbxClientV2(config, token);
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
					Dataview.debugger.logObjectValue(" the inputportAndFile value is ", inputportAndFile);
					Dataview.debugger.logObjectValue(" the outputportAndFile value is ", outputportAndFile);			
					Task t = null;
					if (new File("/home/ubuntu/" + taskName + ".jar").exists()) {
						try {
							File f = new File("/home/ubuntu/" + taskName + ".jar");
							URL url = f.toURI().toURL();
							URL[] urls = new URL[] { url };
							URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
							Class<URLClassLoader> classLoaderClass = URLClassLoader.class;
							Method method = classLoaderClass.getDeclaredMethod("addURL", new Class[] { URL.class });
							method.setAccessible(true);
							method.invoke(systemClassLoader, urls);
							Class<?> taskclass = Class.forName(taskName);
							t = (Task) taskclass.newInstance();
						
						} catch (Exception e) {
							Dataview.debugger.logException(e);
							e.printStackTrace();
						}
						
					}
					else{
						File file = new File("/home/ubuntu/"); 

		                //convert the file to URL format
						URL url = file.toURI().toURL(); 
						URL[] urls = new URL[]{url}; 
					
		                //load this folder into Class loader
						ClassLoader cl = new URLClassLoader(urls); 
						
						try{
							Class<?> taskclass = Class.forName(taskName,true,cl);
							t = (Task) taskclass.newInstance();
						}
						catch (Exception e) {
							Dataview.debugger.logException(e);
							e.printStackTrace();
						}
						
					}
					for (int i = 0; i < t.ins.length; i++) {
						t.ins[i].setLocation(inputportAndFile.get(i+""));
						
					}
					for (int i = 0; i < t.outs.length; i++) {
						t.outs[i].setLocation(outputportAndFile.get(i+""));
					}

					Dataview.debugger.logSuccessfulMessage("Task "+ t.taskName + " start to run");
					long startTime = System.nanoTime();
					t.run();
					long endTime = System.nanoTime();
					double duration = (double)(endTime - startTime) / 1_000_000_000.0;	
					taskSpec.put("execTime", new JSONValue(Double.toString(duration)));
					Dataview.debugger.logSuccessfulMessage("Task "+t.taskName + " is finished");
					
					// move the output file to the crossponding VM isntances
					ArrayList<TransformThread> threads = new ArrayList<TransformThread>();
					for(int i = 0; i < outdcs.size(); i++){
						JSONObject outdc = outdcs.get(i).toJSONObject();
						if(!outdc.get("destIP").toString().replaceAll("\"", "").
								equals(taskSpec.get("myIP").toString().replaceAll("\"", "")) && !outdc.get("destIP").isEmpty() ){
							final String taskInstanceID = taskSpec.get("taskInstanceID").toString();
							final String outputPortIndex = outdc.get("myOutputPortIndex").toString();
							final String destIP = outdc.get("destIP").toString();
							Runnable task = new Runnable() {
								@Override
								public void run() {
									try {
										MoveDataToCloud.getDataReady(taskInstanceID.replaceAll("\"", "")+"_"+
												outputPortIndex.replaceAll("\"", "")+".txt", 
												destIP.replaceAll("\"", ""));
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							};
							DatamovementCallback callback = new DatamovementCallback(outdc);
							TransformThread thread = new TransformThread(task, callback);
							threads.add(thread);
							thread.start();
						}else if(outdc.get("destTask").isEmpty()){
							if(!token.isEmpty()){
								final String tokenForThread = token;
								final String destFilename = outdc.get("destFilename").toString();
								final String taskIDForThread = taskID;
								
								Runnable task = new Runnable() {
									@Override
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
								TransformThread thread = new TransformThread(task, null);
								threads.add(thread);
								thread.start();
							}
						}else {
							outdc.put("transTime", new JSONValue(Double.toString(0.0)));
						}
						
					}
					// the main thread will wait until all the threads are finished.
					for (TransformThread thread : threads) {
						thread.join();
					}
					
					Dataview.debugger.logSuccessfulMessage("Here is the task specification "+ taskSpec);
					out.writeObject(taskSpec.toString());
					out.flush();
						
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (true);
		} catch (IOException ioException) {
			Dataview.debugger.logException(ioException);
			ioException.printStackTrace();
		} finally {
			// 4: Closing connection
			try {
				in.close();
				out.close();
				connection.close();
				providerSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			} 
		}
	}
	

}
