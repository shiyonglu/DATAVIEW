package dataview.workflowexecutors;
import java.lang.reflect.Method;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;

import dataview.models.DATAVIEW_BigFile;
import dataview.models.Dataview;
import dataview.models.GlobalSchedule;
import dataview.models.IncomingDataChannel;
import dataview.models.JSONArray;
import dataview.models.JSONObject;
import dataview.models.JSONValue;
import dataview.models.LocalSchedule;
import dataview.models.OutgoingDataChannel;
import dataview.models.ProvenanceGraph;
import dataview.models.ProvenanceNode;
import dataview.models.Task;
import dataview.models.TaskSchedule;
import dataview.models.Workflow;

/* 
 * A general introduction to WorkflowExecutor_Local version:
 * This workflowExecutor Local version will have each task running as individual threads locally
 * Each Task periodically checks its readiness until all its parents completed
 * InputPort and OutputPort of a task will be mapped with corresponding files through mappingInputAndFile and mappingOutputAndFile methods
 * Intermediate and final results will be saved as individual .txt files under root repository
 * A right-click on "Refresh" the project may be needed in order to see these files after execution
 */

public class WorkflowExecutor_Local extends WorkflowExecutor {
	public static String workflowTaskDir;
	public static String workflowLibdir;
	public ConcurrentHashMap<String, ConcurrentLinkedQueue<TaskRun>> relationMap = new ConcurrentHashMap<String, ConcurrentLinkedQueue<TaskRun>>();
	public static ConcurrentHashMap<String, String> OutputSet = new ConcurrentHashMap<String, String>();
	public static int taskNum = 0;
	public static long starTime;
	public Workflow w;
	public static String dropboxToken = "";

	/**
	 * Constructor of workflowExecutor_Local
	 * 
	 */
	public WorkflowExecutor_Local(String workflowTaskDir, String workflowLibDir, GlobalSchedule gsch) throws Exception {
		super(gsch);
		starTime = System.currentTimeMillis();
		for (int i = 0; i < gsch.length(); i++) {
			for (int j = 0; j < gsch.getLocalSchedule(i).length(); j++) {
				taskNum++;
			}
		}
		this.w = gsch.getWorkflow();
		this.workflowTaskDir = workflowTaskDir;
		this.workflowLibdir = workflowLibDir;
		System.out.println("the total number of tasks are " + taskNum);

	}
	
	

	public WorkflowExecutor_Local(String workflowTaskDir, String workflowLibDir, String token, GlobalSchedule gsch) {
		super(gsch);
		starTime = System.currentTimeMillis();
		for (int i = 0; i < gsch.length(); i++) {
			for (int j = 0; j < gsch.getLocalSchedule(i).length(); j++) {
				taskNum++;
			}
		}
		this.w = gsch.getWorkflow();
		this.workflowTaskDir = workflowTaskDir;
		this.workflowLibdir = workflowLibDir;
		this.dropboxToken = token;
	}



	/*
	 * execute method is used to execute the workflowExecutor_Local object
	 */
	public void execute() throws InterruptedException {
		TaskRun[] truns = new TaskRun[gsch.getNumberOfTasks()];
		int k = 0;

		// put all tasks in globalSchedule in taskArray truns[]
		for (int i = 0; i < gsch.length(); i++) {
			LocalSchedule lsch = gsch.getLocalSchedule(i);
			for (int j = 0; j < lsch.length(); j++) {
				TaskSchedule taskschedule = lsch.getTaskSchedule(j);
				TaskRun taskRunner = new TaskRun(taskschedule);
				truns[k] = taskRunner;
				k++;

				//Map each task and their children's relationship and store in relationMap
				for (String parentInstanceID : taskschedule.getParents()) {
					ConcurrentLinkedQueue<TaskRun> runChildren = relationMap.get(parentInstanceID);
					if (runChildren == null) {
						runChildren = new ConcurrentLinkedQueue<TaskRun>();
						relationMap.put(parentInstanceID, runChildren);
					}

					runChildren.add(taskRunner);
				}
			}

		}

		// starts all tasks at same time
		for (TaskRun tr : truns)
			tr.start();

		for (TaskRun tr : truns)
			tr.join();
	}

 
	/* 
	 * TaskRun extends thread class, use to handle executions for each task
	 * Every task will have its own Lock and condition
	 */
	class TaskRun extends Thread {
		private String taskRunID;
		private int predecessors;
		private TaskSchedule taskschdule;
		public Lock Lock;
		public Condition Ready;

		public TaskRun(TaskSchedule taskschedule) {
			predecessors = taskschedule.getParents().size();
			this.taskschdule = taskschedule;
			taskRunID = taskschedule.getTaskInstanceID();
			Lock = new ReentrantLock();
			Ready = Lock.newCondition();
		}

		public void onParentFinished() {
			--predecessors;
			this.wakeUp();
		}

		public boolean isReady() {
			return predecessors == 0;
		}

		public void wakeUp() {
			Lock.lock();
			Ready.signal();
			Lock.unlock();
		}

		public void onFinished(String taskRunID) {
			ConcurrentLinkedQueue<TaskRun> children = relationMap.get(taskRunID);
			if (children != null) {
				for (TaskRun runner : children) {
					runner.onParentFinished();
				}
			}
		}

		/* 
		 * Retrieve input file name for each task and bind inputPort index with filename as key-value pairs 
		 */
		public SortedMap<String, String> mappingInputAndFile(TaskSchedule taskschedule) throws IOException, DbxException {
			List<IncomingDataChannel> indcs = taskschdule.getIncomingDataChannels();
			SortedMap<String, String> inputportAndFile = new TreeMap<String, String>();
			for (int i = 0; i < indcs.size(); i++) {
				IncomingDataChannel indc = indcs.get(i);
				int index = indc.winIndex;
				if (indc.getSrcTask() == null) {
					//if SrcTask is empty, the input of workflow map to the file under workflowTaskDir
					if(w.wins[index].getClass().equals(DATAVIEW_BigFile.class)){
						String inputfile = ((DATAVIEW_BigFile)w.wins[index]).getFilename();
						String inputfilepath =  workflowTaskDir + inputfile;
						inputportAndFile.put(String.valueOf(indc.myInputPortIndex),inputfilepath);
						if(!dropboxToken.isEmpty()){
							DbxRequestConfig config = new DbxRequestConfig("en_US");
							DbxClientV2 client = new DbxClientV2(config, dropboxToken);
							DbxDownloader<FileMetadata> dl = null;
							try {
								dl = client.files().download("/DATAVIEW-INPUT/"+inputfile);
							} catch (DownloadErrorException e) {
								Dataview.debugger.logException(e);
							}
							FileOutputStream fOut = new FileOutputStream(inputfilepath);
							dl.download(fOut);
						}
						
					}else{
						String inputfile = w.workflowName + w.hashCode() +"@"+ index;
						String dataFileLocation = workflowTaskDir + inputfile;
						if(!new File(dataFileLocation).exists()){
							BufferedWriter	writer = new BufferedWriter(new FileWriter(dataFileLocation));
							System.out.println("Here is the input value " + w.wins[index].toString());
							writer.write(w.wins[index].toString());	
							writer.close();
						}
						inputportAndFile.put(String.valueOf(indc.myInputPortIndex),dataFileLocation);
					}

				} else {
					//otherwise inputPort map to intermediate file
					inputportAndFile.put(String.valueOf(indc.myInputPortIndex),workflowTaskDir + indc.srcTask.toString() + "_"
							+ String.valueOf(indc.outputPortIndex).replace("\"", "") + ".txt");
				}
			}
			Dataview.debugger.logObjectValue(" the inputportAndFile value is ", inputportAndFile);
			return inputportAndFile;
		}

		/* 
		 * Retrieve output file name for each task and bind outputPort index and filename as key-value pairs  
		 */
		public SortedMap<String, String> mappingOutputAndFile(TaskSchedule taskschedule) throws IOException {
			List<OutgoingDataChannel> outdcs = taskschdule.getOutgoingDataChannels();
			SortedMap<String, String> outputportAndFile = new TreeMap<String, String>();
			for (int i = 0; i < outdcs.size(); i++) {
				OutgoingDataChannel outdc = outdcs.get(i);
				int index = outdc.woutIndex;
				if (outdc.getDestTask() == null) {
					String file = workflowTaskDir+ ((DATAVIEW_BigFile)w.wouts[index]).getFilename() + taskschdule.getTaskInstanceID();
					outputportAndFile.put(String.valueOf(outdc.myOutputPortIndex).replace("\"", ""), file);
				} else {
					//otherwise OutputPort map to intermediate file
					outputportAndFile.put(String.valueOf(outdc.myOutputPortIndex),
							workflowTaskDir + taskschedule.getTaskInstanceID().toString() + "_"
									+ String.valueOf(outdc.myOutputPortIndex).replace("\"", "") + ".txt");

				}
			}
			Dataview.debugger.logObjectValue(" the outputportAndFile value is ", outputportAndFile);
			return outputportAndFile;

		}
		
		/* 
		 * execute method is used to execute the Task after inputPort and outputPort mappings are ready
		 */
		public void execute(TaskSchedule taskschedule, SortedMap<String, String> inputportAndFile,
				SortedMap<String, String> outputportAndFile) throws MalformedURLException {
			String taskName = taskschedule.getTaskName().replace("\"", "");
			Task t = null;
			File f ;
			//Load the taskName.class file
			if (new File(workflowTaskDir + taskName + ".jar").exists()) {
				f = new File(workflowTaskDir + taskName + ".jar");
			}else{
				f= new File(workflowTaskDir + taskName + ".class");
				//f =  new File(workflowTaskDir);
			}
			try {
				URL url = null;
				url = f.toURI().toURL();
				URL[] urls = new URL[] {url};
				Thread.currentThread().setContextClassLoader(new URLClassLoader(urls,Thread.currentThread().getContextClassLoader()));
				ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
				Class<URLClassLoader> classLoaderClass = URLClassLoader.class;
				Method method = classLoaderClass.getDeclaredMethod("addURL", new Class[] { URL.class });
				method.setAccessible(true);
				method.invoke(currentClassLoader, urls);
				Class<?> taskclass = Class.forName(taskName);
				t = (Task) taskclass.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				Dataview.debugger.logException(e);
				e.printStackTrace();
			}
			// bind the input file names to the input ports
			for (int i = 0; i < t.ins.length; i++) {
				t.ins[i].setLocation(inputportAndFile.get(i + ""));

			}
			// bind the output file names to the output ports
			for (int i = 0; i < t.outs.length; i++) {
				t.outs[i].setLocation(outputportAndFile.get(i + ""));
			}
			Dataview.debugger.logSuccessfulMessage("Task " + t.taskName + " start to run");
			//Call Task.run and record execution time
			long startTime = System.nanoTime();
			t.run();
			long endTime = System.nanoTime();
			double duration = (double) (endTime - startTime) / 1000000000.0;
			Dataview.debugger.logSuccessfulMessage("Task " + t.taskName + " is finished");
		}

		@Override
		public void run() {
			try {
				// Displaying the thread that is running
				//System.out.println("Thread " + Thread.currentThread().getId() + ":" + this.getName() + " is running");

				while (!this.isReady()) {
					Lock.lock();
					Ready.await();
					Lock.unlock();
				}
				// get taskfile, input data ready for task to execute
				/*
				String taskFileLocation = workflowTaskDir + this.taskschdule.getTaskName();
				if (!new File(taskFileLocation + ".class").exists() && !new File(taskFileLocation + ".jar").exists()) {
					System.out.println("THE TASK FILE " +  this.taskschdule.getTaskName() + " IS NOT AVAILABLE");
				}**/
				//map inputPortandFile and outputPortAndFile for this task
				SortedMap<String, String> inputportAndFile = mappingInputAndFile(this.taskschdule);
			
				SortedMap<String, String> outputportAndFile = mappingOutputAndFile(this.taskschdule);
				
				//execute the task
				execute(this.taskschdule, inputportAndFile, outputportAndFile);
				
				synchronized (this) {
					taskNum--;
					System.out.println("The task number is " + taskNum);
				}
				if (taskNum == 0) {
					long endTime = System.currentTimeMillis();
					System.out.println("The workflow execution time is " + (endTime - starTime));
				}
				onFinished(this.taskRunID);

			} catch (Exception e) {
				// Throwing an exception
				e.printStackTrace();
				System.out.println("Exception is caught: " + e);
			}
		}
	}
	
}
