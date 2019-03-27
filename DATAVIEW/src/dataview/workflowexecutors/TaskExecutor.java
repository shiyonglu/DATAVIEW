package dataview.workflowexecutors;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
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


/*	The task executor will bind the data products with input ports and output ports
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
	
	//List<String> updatefiles;
	
	TaskExecutor() throws ClassNotFoundException, SQLException {
		try {
			providerSocket = new ServerSocket(2004, 10);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void run() {
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
					JSONParser p = new JSONParser(message.getB());
					String token = message.getA();
					Dataview.debugger.logSuccessfulMessage("receive the task specification:");
					Dataview.debugger.logSuccessfulMessage(message.getB());
					JSONObject taskSpec = p.parseJSONObject();
					String taskName = taskSpec.get("taskName").toString().replace("\"", "");
					String taskID = taskSpec.get("taskInstanceID").toString().replace("\"", "");
					JSONArray indcs = taskSpec.get("incomingDataChannels").toJSONArray();
					JSONArray outdcs = taskSpec.get("outgoingDataChannels").toJSONArray();
					// map each port to a file name
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
						} catch (Exception e) {
							Dataview.debugger.logException(e);
							e.printStackTrace();
						}
					}
					try {
						Class<?> taskclass = Class.forName(taskName);
						t = (Task) taskclass.newInstance();

					} catch (ClassNotFoundException e) {

						e.printStackTrace();
					} catch (InstantiationException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
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
					long duration = (endTime - startTime);
					JSONObject json = new JSONObject();
					json.put(taskID, new JSONValue(Long.toString(duration)));
					Dataview.debugger.logSuccessfulMessage("Task "+t.taskName + " is finished");
					
					// move the output file to the crossponding VM isntances
					for(int i = 0; i < outdcs.size(); i++){
						JSONObject outdc = outdcs.get(i).toJSONObject();
						if(!outdc.get("destIP").toString().replaceAll("\"", "").
								equals(taskSpec.get("myIP").toString().replaceAll("\"", "")) && !outdc.get("destIP").isEmpty() ){
							MoveDataToCloud.getDataReady(taskSpec.get("taskInstanceID").toString().replaceAll("\"", "")+"_"+
									outdc.get("myOutputPortIndex").toString().replaceAll("\"", "")+".txt", outdc.get("destIP").toString().replaceAll("\"", ""));
						}else if(outdc.get("destTask").isEmpty()){
							if(!token.isEmpty()){
								// update the final output to Dropbox 
								DbxRequestConfig config = new DbxRequestConfig("en_US");
								DbxClientV2 client = new DbxClientV2(config, token);
								String localFileAbsolutePath = outdc.get("destFilename").toString().replaceAll("\"", "");
								String dropboxPath = "/DATAVIEW-OUTPUT/" + localFileAbsolutePath;
								InputStream in = new FileInputStream(localFileAbsolutePath);
								client.files().uploadBuilder(dropboxPath).withMode(WriteMode.ADD).uploadAndFinish(in);
							}
						}
					}
					
					out.writeObject(taskID);
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
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		TaskExecutor msgserver = new TaskExecutor();
		msgserver.run();

	}

}


