
package dataview.workflowexecutors;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import dataview.models.DATAVIEW_BigFile;
import dataview.models.Dataview;
import dataview.models.GlobalSchedule;
import dataview.models.IncomingDataChannel;
import dataview.models.JSONArray;
import dataview.models.JSONObject;
import dataview.models.JSONParser;
import dataview.models.JSONValue;
import dataview.models.LocalSchedule;
import dataview.models.ProvenanceNode;
import dataview.models.ProvenanceGraph;
import dataview.models.TaskSchedule;
import dataview.models.Workflow;


/**
 * The Beta workflow executor is supporting multi-thread task submission to the task executor correspondingly.
 * Two scenarios is supported in the workflow executor: workflow input is read from the Dropbox folder "DATAVIEW-INPUT"; 
 * the workflow input is read from workflowlibdir which is the project folder path. 
 *
 */
public class WorkflowExecutor_Beta extends WorkflowExecutor {
	public static String workflowTaskDir;
	public static  String workflowLibdir;
	public LocalScheduleRun[] lschRuns;
	public String workflowName;
	public Workflow w;
	
	
	// The key is the taskRunID, the value is the list of its child TaskRuns.
	public ConcurrentHashMap<String, ConcurrentLinkedQueue<TaskRun>> relationMap = new ConcurrentHashMap<>();
	public  int taskNum = 0; 
	public  long starTime;
	public String dropboxToken="";         
	public String accessKey;
	public String secretKey;
	public  List<JSONObject> taskSpecObj = new ArrayList<JSONObject>();

	/**
	 * The constructor is used to set the path of two folders and read the EC2 provisioning parameters from "config.properties"
	 * 
	 * @param workflowTaskDir
	 * @param workflowlibdir
	 * @param gsch
	 * @throws IOException 
	 */	
	public WorkflowExecutor_Beta(String workflowTaskDir, String workflowLibDir, GlobalSchedule gsch) throws Exception {
		super(gsch);
		workflowName = gsch.getWorkflow().workflowName; 
		starTime = System.currentTimeMillis();
		taskNum = gsch.getNumberOfTasks();
		this.workflowTaskDir = workflowTaskDir;
		this.workflowLibdir =  workflowLibDir;
		VMProvisioner.parametersetting(workflowLibDir);
		init();
		this.w = gsch.getWorkflow();
		System.out.println("the total number of tasks are "+ taskNum);
	}
	
	/**The constructor initialize the file download and upload parameter: dropbox token and VM provisioning paramters: access key and secrete key
	 * 
	 * @param workflowTaskDir
	 * @param workflowlibdir
	 * @param gsch
	 * @param dropboxToken
	 * @param accessKey
	 * @param secretKey
	 * @throws Exception
	 */	
	public WorkflowExecutor_Beta(String workflowTaskDir, String workflowLibDir, GlobalSchedule gsch,String dropboxToken,String accessKey, String secretKey) throws Exception {
		super(gsch);
		starTime = System.currentTimeMillis();
		taskNum = gsch.getNumberOfTasks();
		this.w = gsch.getWorkflow();
		this.workflowTaskDir = workflowTaskDir;
		this.workflowLibdir = workflowLibDir;
		VMProvisioner.initializeProvisioner(accessKey, secretKey,"dataview1","Dataview_key","ami-064ab7adf0e30b152");
		this.dropboxToken = dropboxToken;
		init();
	}
	/**
	 * The init() method calculates how many VM instances we need for each VM type, based on that 
	 * we provision in batch numerous of VM instances for each  VM type using one call. We assume provisioning VM in batch 
	 * is more efficient than provisioning each VM individually.  
	 * 	  
	 *
	 * @throws Exception
	 */
	private void init() throws Exception{
		// We introduce the data structure VMnumbers to store how many VM instances  we need to provision for each VM type, 
		// in this Map, VM type is the key, and the number of VM instances is the value. 
		Map<String, Integer> VMnumbers = new HashMap<String, Integer>();
		for(int i = 0; i < gsch.length(); i++){
			LocalSchedule ls = gsch.getLocalSchedule(i);
			System.out.println(ls.getVmType());
			if(VMnumbers.containsKey(ls.getVmType())){
				Integer value = VMnumbers.get(ls.getVmType());
				VMnumbers.put(ls.getVmType(), value+1);
			}else{
				VMnumbers.put(ls.getVmType(), 1);
			}
		}		
		Dataview.debugger.logObjectValue("VMnumbers", VMnumbers);
		
		ArrayList<String> ips = new ArrayList<String>();
		VMProvisioner m = new VMProvisioner();	
		for(String str : VMnumbers.keySet()){
			m.provisionVMs(str,VMnumbers.get(str), workflowLibdir);
			
		}
		Thread.sleep(90000);
		
		// We introduce ipsAndType (also called IPPool) to store the IPs of VM instances for each VM type
		// Here, VM type is the key, and the list of IPs is the value.
		Map<String, LinkedList<String>> ipsAndType = m.getVMInstances();
		for(String str:ipsAndType.keySet()){
			ips.addAll(ipsAndType.get(str));
		}
		Dataview.debugger.logObjectValue("ipsAndType", ipsAndType);
		
		
		// get the pem file generated from the VM provisioning process. 
		String pemFileLocation = workflowLibdir + VMProvisioner.keyName + ".pem";
		
		// configure each VM instance with confidential information instead of using pem 
		// Use SSH to send the pem file to each VM instance
		MakeMachinesReady.getMachineReady(pemFileLocation, ips);
		
		// move the pem file to each VM instance to send intermedidate output.
		MoveToCloud.getFileReady(pemFileLocation, ips);
		
		// prepare each VM instance by stopping listening services and remove task files.   
		MoveToCloud.getCodeReady(ips);
		
		// assign ips to each local schedule.
		for (int i = 0; i < gsch.length(); i++) {
			LocalSchedule ls = gsch.getLocalSchedule(i);
			ls.setIP(ipsAndType.get(ls.getVmType()).pop());
		}
		
		// propagate IP assingment to TaskSchedules and outgoing data channels  in TaskSchedules
		gsch.completeIPAssignment();
	}
	
	
	/**
	 * The execute() method is implemented by all subclasses of the WorkflowExecutor class. 
	 * In the implementation of WorkflowExecutor_Beta, we launch m LocalScheduleRun threads, where m is the number of LocalSchedules 
	 * in the GlobalSchedule, each  LocalScheduleRun thread manages the execution of all TaskSchedules in the corresponding 
	 * LocalSchedule on a VM that is assigned to it. 
	 *
	 * 
	 */
	public void execute() throws InterruptedException {
		lschRuns =  new LocalScheduleRun[gsch.length()];
		for(int i = 0; i < lschRuns.length; i++){
			LocalSchedule localSchedule = gsch.getLocalSchedule(i);
			LocalScheduleRun scheduleRunner = new LocalScheduleRun(localSchedule);
			lschRuns[i] = scheduleRunner;
		}
		for(LocalScheduleRun run : lschRuns){
			run.start();
		}
		for(LocalScheduleRun run : lschRuns){
			run.join();
		}	
	}

	/**
	 * A LocalScheduleRun is a thread that is responsible for the execution of all tasks in a local schedule, which will 
	 * be executed in one VM instance. All LocalScheduleRuns will run in parallel. Each LocalScheduleRun will submit tasks 
	 * in a local schedule sequentially to the correspondingly assigned VM instance. 
	 *  
	 * 
	 * Each thread holds local schedule and submit those tasks in the local shcedule to VM instacne sequentially 
	 * If the parent task and child task are submitted in different threads, signal needs to be communicated between two threads.
	 * 
	 * To illustrate how a LocalScheduleRun interacts with other parallel LocalScheduleRun and the corresponding TaskExecutor, consider 
	 * three tasks T1, T2 and T3, in which T1 and T2 are two consective tasks controlled by LocalScheduleRunA and are assigned to 
	 * virtual machine VM1, and T3 is another task that is controlled by LocalScheduleRunB and assigned to VM instance VM2.
	 * The outputs of T1 and T3 are the inputs for T2. 
	 *  
	 */
	public class LocalScheduleRun extends Thread {
		public LocalSchedule lsc;
		public Lock mLock;
		public Condition mReady;
		public TaskRun[] mTaskRunners;

		public LocalScheduleRun(LocalSchedule lsc) {
			mTaskRunners = new TaskRun[lsc.length()];
			for(int i = 0; i < mTaskRunners.length; i++){
				TaskSchedule taskschedule = lsc.getTaskSchedule(i);
				TaskRun taskRunner = new TaskRun(this, taskschedule);
				this.mTaskRunners[i] = taskRunner;
				for (String parentInstanceID : taskschedule.getParents()){
					ConcurrentLinkedQueue<TaskRun> runChildren = relationMap
							.get(parentInstanceID);
					if (runChildren == null) {
						runChildren = new ConcurrentLinkedQueue<>();
						relationMap.put(parentInstanceID, runChildren);
					}
					runChildren.add(taskRunner);
				}
			}
			this.lsc = lsc;
			mLock = new ReentrantLock();
			mReady = mLock.newCondition();
		}
		
		// wake up this LocalScheduleRun thread so that it can execute the next TaskRun
		// WakeupSleep point
		public void wakeUP() {
			mLock.lock();
			mReady.signal();
			mLock.unlock();
		}
		
		/**
		 * wait for until the task is ready to submit to a VM instacne, then a class file or jar file is sent to
		 * the corresponding VM instacne. If the workflow is constructed from the webbench, the input data will be 
		 * downloaded from the Dropbox directly, otherwise, the input should be in the folder of "workflowTaskDir".
		 * once the task is finished. The processor of the children of the task should be updated and wake up the 
		 * waiting thread to execute the children task. 
		 * 
		 */
		@Override
		public void run() {
			for (TaskRun taskrun : mTaskRunners) {
				try {
					// Step 1: check if a task is ready to run, that means, all its parents have finished their execution and 
					// all their output data have completed their data transfer processes. 
					while (!taskrun.isReady()) { // WakeupSleep point: The LocalScheduleRun thread will sleep here if the next TaskRun is not ready
						mLock.lock();     // for execution
						mReady.await();
						mLock.unlock();
					}
					// move each task to VM instance
					String taskFileLocation =  workflowTaskDir + taskrun.taskschdule.getTaskName();
					if(new File(taskFileLocation + ".jar").exists()){
						MoveToCloud.getFileReadyOneIP(taskFileLocation+".jar", taskrun.taskschdule.getIP());
					}else if (new File(taskFileLocation + ".class").exists()){
						MoveToCloud.getFileReadyOneIP(taskFileLocation+".class", taskrun.taskschdule.getIP());
					}else{
						System.out.println("THE TASK FILE IS NOT AVAILABLE");
					}
					List<IncomingDataChannel> indcs = taskrun.taskschdule.getIncomingDataChannels();
					if(dropboxToken.isEmpty()){
						for(int i = 0; i < indcs.size(); i++){
							if(indcs.get(i).winIndex != -1){
								if(w.wins[indcs.get(i).winIndex].getClass().equals(DATAVIEW_BigFile.class)){
									String filename = ((DATAVIEW_BigFile)w.wins[indcs.get(i).winIndex]).getFilename();
									String	dataFileLocation = workflowTaskDir + filename;
									MoveToCloud.getFileReadyOneIP(dataFileLocation, taskrun.taskschdule.getIP());
								}else{
									String filename = w.workflowName + w.hashCode() +"@"+ indcs.get(i).winIndex;
									String dataFileLocation = workflowTaskDir + filename;
									if(!new File(dataFileLocation).exists()){
										BufferedWriter	writer = new BufferedWriter(new FileWriter(dataFileLocation));
										writer.write(w.wins[indcs.get(i).winIndex].toString());	
										writer.close();
									}
									MoveToCloud.getFileReadyOneIP(dataFileLocation, taskrun.taskschdule.getIP());
								}
							}
						}
					}
					
					// taskrun.execute() is a blocking method, which will wait for the completion of the task execution as well as all the output
					// data from the task have been moved to their destination VM instances successfully.
					String taskspec = taskrun.execute();
					System.out.println(taskspec);
					JSONParser p = new JSONParser(taskspec);
					JSONObject taskSpec = p.parseJSONObject();
					Dataview.debugger.logSuccessfulMessage("local recv " + taskSpec.get("taskInstanceID").toString().replace("\"", ""));
					taskSpecObj.add(taskSpec);
					synchronized(this){
						taskNum--;	
						System.out.println("The task number is " +  taskNum);
					}
					// will refactor this to recordProvenance() 
					if(taskNum == 0){
						recordProvenance();
						if(dropboxToken.isEmpty()){
							fetchDataFromVM();
						}
						
					}
					
					// when the execution of task T is completed, we need to inform all its child TaskRun, so that 
					// each Child TaskRun can modify its NumOfParentsFinished status via the onParentFinished() method
					ConcurrentLinkedQueue<TaskRun> children = relationMap.get(taskrun.taskRunID);
					if (children != null) {
						for (TaskRun tr: children) {
							tr.onParentFinished();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void fetchDataFromVM() throws IOException{
		for(JSONObject tmp:taskSpecObj){
			 JSONArray outdcs = tmp.get("outgoingDataChannels").toJSONArray();
			 for(int i = 0; i < outdcs.size(); i++){
					JSONObject outdc = outdcs.get(i).toJSONObject();
					if(outdc.get("destTask").isEmpty()){
						
						String filename = outdc.get("destFilename").toString().replace("\"", "");
						File of = new File(workflowTaskDir + File.separator+ filename);
						if(of.exists()){
							of.delete();
						}
						String strHostName = tmp.get("myIP").toString().replace("\"","");
						File f=  CmdLineDriver.getFile(filename,"/home/ubuntu/", strHostName);
						BufferedReader reader = new BufferedReader(new FileReader(f));
						
						BufferedWriter writer = new BufferedWriter(new FileWriter(workflowTaskDir + File.separator+ filename, true));
						String line = reader.readLine();
						while(line!=null){
							writer.append(line);
							line = reader.readLine();	
						}
						reader.close();
						writer.close();
						}
					}
	}
}	
	
	
	
	public void recordProvenance(){
		long endTime = System.currentTimeMillis();
		System.out.println("The workflow execution time is " + (endTime-starTime));
		Date now = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
		String datetime = ft.format(now);
		ProvenanceGraph pgraph = new ProvenanceGraph(workflowName, "RunID-"+datetime);						
		for(JSONObject tmp:taskSpecObj){
		   String taskId = tmp.get("taskInstanceID").toString().replace("\"", "");
		   Double exeTime = Double.parseDouble(tmp.get("execTime").toString().replace("\"", ""));
		   pgraph.myActivities.add(new ProvenanceNode(taskId,exeTime));
		   JSONArray outdcs = tmp.get("outgoingDataChannels").toJSONArray();
		   for(int i = 0; i < outdcs.size(); i++){
				JSONObject outdc = outdcs.get(i).toJSONObject();
				if(!outdc.get("destTask").isEmpty()){
					String destTask = outdc.get("destTask").toString().replace("\"", "");
					int portId = Integer.parseInt(outdc.get("inputPortIndex").toString().replace("\"", ""));
					double transTime = Double.parseDouble(outdc.get("transTime").toString().replace("\"", ""));
					pgraph.addEdge_TransTime(taskId, destTask, portId, transTime);
				}
				
			}
		}
		//pgraph.record();
		pgraph.record(workflowLibdir);
	}
	
	/**
	 * A TaskRun is responsible for submitting the task execution information stored in a TaskSchedule object to the 
	 * corresponding TaskExector and waiting for the completion of the task execution. 
	 * 
	 * @author shiyo
	 *
	 */
	public class TaskRun {
		private String taskRunID; // 
		private int numOfParentsFinished;
		private LocalScheduleRun mLocalRunner;
		private TaskSchedule taskschdule;
		
		public TaskRun(LocalScheduleRun localRunner, TaskSchedule taskschedule){
			numOfParentsFinished = taskschedule.getParents().size();
			mLocalRunner = localRunner;
			this.taskschdule = taskschedule;
			taskRunID = taskschedule.getTaskInstanceID();
		}
		
		/**
		 * The method onParentFinished() will be called by a LocalScheduleRun when one of it parent tasks finishes its execution, 
		 * which will decrease NumOfParentsFinished by 1.
		 */
		public void onParentFinished() {
			--numOfParentsFinished;
			if(isReady())
			    mLocalRunner.wakeUP();
		}

		/**
		 * A TaskRun is ready to run when all its parents complete their execution, that is, when numOfParentsFinished = 0.
		 * 
		 * @return
		 */
		public boolean isReady() {
			return numOfParentsFinished == 0;
		}

		/**
		 * The execute() method remotely interacts with the remote TaskExecutor to execute a task. The method is a blocking one.
		 * It uses an TCP/IP protocol to conduct such interaction based on port 2004.
		 * 
		 * 
		 * @return
		 * @throws Exception
		 */
		public String execute() throws Exception {
			JSONObject taskscheduleJson = taskschdule.getSpecification();
			
			JSONArray indcs = taskscheduleJson.get("incomingDataChannels").toJSONArray();
			for(int i = 0; i < indcs.size(); i++){
				JSONObject indc = indcs.get(i).toJSONObject();
				if(!indc.get("win").isEmpty()){
					String inputindex = indc.get("win").toString().replace("\"", "");
					if(w.wins[Integer.parseInt(inputindex)].getClass().equals(DATAVIEW_BigFile.class)){
						String inputindexfilename = ((DATAVIEW_BigFile)w.wins[Integer.parseInt(inputindex)]).getFilename();
						indc.put("srcFilename",new JSONValue(inputindexfilename));
					}
					else{
						String inputindexfilename = w.workflowName + w.hashCode() +"@"+  Integer.parseInt(inputindex);
						indc.put("srcFilename",new JSONValue(inputindexfilename));
					}
					
					
				}
				
			}
			
			
			
			JSONArray outdcs = taskscheduleJson.get("outgoingDataChannels").toJSONArray();
			for(int i = 0; i < outdcs.size(); i++){
				JSONObject outdc = outdcs.get(i).toJSONObject();
				if(!outdc.get("wout").isEmpty()){
					String outputindex = outdc.get("wout").toString().replace("\"", "");
					if(w.wouts[Integer.parseInt(outputindex)].getClass().equals(DATAVIEW_BigFile.class)){
						String outputindexfilename = ((DATAVIEW_BigFile)w.wouts[Integer.parseInt(outputindex)]).getFilename();
						outdc.put("destFilename",new JSONValue(outputindexfilename));
					}
				}
			}
			
			
			
			Message m = new Message(dropboxToken,taskscheduleJson.toString());
			MSGClient client = new MSGClient(mLocalRunner.lsc.getIP(), m );
			client.run();
			return client.getResp();
		}
	}
	

}




/**
 * 
 * The message class will separate the token information and task specification information
 *
 */

class Message implements Serializable {
    private static final long serialVersionUID = -5399605122490343339L;

    private String A;
    private String B;
   

    public Message(String A, String B ){
        this.A = A; 
        this.B = B;
    }

    public String getA() {
        return A;
    }

    public String getB() {
        return B;
    }
    
    
    
}

