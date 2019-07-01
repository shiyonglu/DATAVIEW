package dataview.workflowexecutors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.rds.AmazonRDSClient;

import dataview.models.Dataview;

/**
 * The VMProvisioner class will launch the enough VMs number based on different VM types. It can also reuse existing available
 * VMs by checking the available status.
 *
 *
 */

public class VMProvisioner {
	public static String accessKey;
	public static String secretKey;
	public static String groupName;
	public static String keyName;
	public static String imageID;
	public static String instanceType;
	public static AWSCredentials credentials;
	public static Region region;
	public static AmazonEC2Client ec2client;
	public static AmazonRDSClient rdsclient;
	public static String localpath;
	
	
	public static void initializeProvisioner(String paramaccessKey,
			String paramsecretKey, String paramgroupName, String paramkeyName,
			String paramimageID) {
			accessKey = paramaccessKey;
			secretKey = paramsecretKey;
			groupName = paramgroupName;
			keyName = paramkeyName;
			imageID = paramimageID;
			//System.out.println("#Initialized successfully....");
		}
	
	public static Properties getPropValues(String workflowlibdir) throws IOException {
		 Properties properties = new Properties();
		 try {
		       properties.load(new FileInputStream(workflowlibdir+ File.separator+"config.properties"));
		   } catch (IOException e) {
		   }
		   return properties;
	}
	
	
	public static void parametersetting(String workflowlibdir) throws IOException{
		Properties prop = VMProvisioner.getPropValues(workflowlibdir);
		accessKey = prop.getProperty("accessKey");
		secretKey = prop.getProperty("secretKey");
		groupName = prop.getProperty("groupName");
		keyName = prop.getProperty("keyName");
		imageID = prop.getProperty("imageID");
		//instanceType = prop.getProperty("instanceType");
	}
	
	
	public void init() throws IOException {
		credentials = new BasicAWSCredentials(accessKey, secretKey);
		region = Region.getRegion(Regions.US_EAST_1);
		ec2client = new AmazonEC2Client(credentials);
		ec2client.setRegion(region);
	}
	
	
	public String createEC2Instance(String vmType, int noOfInstances){
		try {
			RunInstancesRequest rir = new RunInstancesRequest();
			rir.withImageId(imageID);
			rir.withInstanceType(vmType);
			rir.withMinCount(noOfInstances);
			rir.withMaxCount(noOfInstances);
			rir.withKeyName(keyName);
			rir.withMonitoring(true);
			rir.withSecurityGroups(groupName);
			RunInstancesResult riresult = ec2client.runInstances(rir); 
			riresult.toString();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		return null;
	}
	
	
	
	
	
	public String createEC2Instance(int noOfInstances) {
		try {
			RunInstancesRequest rir = new RunInstancesRequest();
			rir.withImageId(imageID);
			rir.withInstanceType(instanceType);
			rir.withMinCount(noOfInstances);
			rir.withMaxCount(noOfInstances);
			rir.withKeyName(keyName);
			rir.withMonitoring(true);
			rir.withSecurityGroups(groupName);
			RunInstancesResult riresult = ec2client.runInstances(rir); 
			riresult.toString();
			// return null;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	public static void stopInstance(String instanceId) {
		StopInstancesRequest sir = new StopInstancesRequest();
		sir.withInstanceIds(instanceId);
		ec2client.stopInstances(sir);
	}

	public static void startInstance(String instanceId) {
		StartInstancesRequest sir = new StartInstancesRequest();
		sir.withInstanceIds(instanceId);
		ec2client.startInstances(sir);
	}

	public static void terminateInstance(String instanceId) {
		TerminateInstancesRequest tir = new TerminateInstancesRequest();
		tir.withInstanceIds(instanceId);
		ec2client.terminateInstances(tir);
	}

	
	
	
	public Map<String, LinkedList<String>> getVMInstances() throws IOException{
		ArrayList<String> initialVMInstances = VMProvisioner.getAvailableInstIds();
		Map<String, LinkedList<String>> ipAddressesAndVMTpe = getIPaddressesAndVMType(initialVMInstances);
		return ipAddressesAndVMTpe;
	}
	
	public void provisionVMs(String vmType, int noOfInstances, String path) throws Exception {
		System.out.println(path);
		ArrayList<String> initialVMTypeIntances = VMProvisioner.getAvailableInstIDs(vmType);
		if (initialVMTypeIntances.size() < noOfInstances) {
			VMProvisioner.justLaunchVMs(vmType, noOfInstances - initialVMTypeIntances.size(), path);
		}
		//ArrayList<String> runningAndPendingVMs = VMProvisioner.getAvailableAndPendingInstIds();
		//VMProvisioner.waitUntilAllPendingBecomeRunning(runningAndPendingVMs);
		System.out.println("#Provisioned machines successfully....");
	}
	
	
	
	public Map<String, LinkedList<String>> provisionVMs(int noOfInstances) throws Exception {
		ArrayList<String> initialVMs = VMProvisioner.getAvailableInstIds();
		VMProvisioner.justLaunchVMs(noOfInstances);
		ArrayList<String> runningAndPendingVMs = VMProvisioner.getAvailableAndPendingInstIds();
		runningAndPendingVMs.removeAll(initialVMs);
		VMProvisioner.waitUntilAllPendingBecomeRunning(runningAndPendingVMs);
		Map<String, LinkedList<String>> ipAddressesAndVMTpe = getIPaddressesAndVMType(runningAndPendingVMs);
		//Dataview.debugger.logSuccessfulMessage("#Provisioned machines successfully....");
		
		System.out.println("#Provisioned machines successfully....");
		return ipAddressesAndVMTpe;
	}
	
	
	

    public void getInstanceInformation()
    {
        boolean done = false;
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        while(!done) {
            DescribeInstancesResult response = ec2client.describeInstances(request);

            for(Reservation reservation : response.getReservations()) {
                for(Instance instance : reservation.getInstances()) {
                    System.out.printf(
                        "Found instance with id %s, " +
                        "AMI %s, " +
                        "type %s, " +
                        "state %s " +
                        "and monitoring state %s",
                        instance.getInstanceId(),
                        instance.getImageId(),
                        instance.getInstanceType(),
                        instance.getState().getName(),
                        instance.getMonitoring().getState());
                 
                }
            }

            request.setNextToken(response.getNextToken());

            if(response.getNextToken() == null) {
                done = true;
            }
        }
    }
	
    public static ArrayList<String> getAvailableAndPendingInstIds(String vmType) throws IOException {
		ArrayList<String> resultList = new ArrayList<String>();
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		DescribeInstancesResult result = VMProvisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			for (Instance ii : instances) {
				if (ii.getState().getName().equals("running") || ii.getState().getName().equals("pending")) {
					if(ii.getInstanceType().equals(vmType)){
						resultList.add(ii.getInstanceId());
					}
				}
			}
		}
		return resultList;
	}
    
    
    
    
	
	public static ArrayList<String> getAvailableAndPendingInstIds() throws IOException {
		ArrayList<String> resultList = new ArrayList<String>();
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		DescribeInstancesResult result = VMProvisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			for (Instance ii : instances) {
				if (ii.getState().getName().equals("running") || ii.getState().getName().equals("pending")) {
					resultList.add(ii.getInstanceId());
				}
			}
		}
		return resultList;
	}

	
	public static void waitUntilAllPendingBecomeRunning(ArrayList<String> pendingAndRunningInstIds) throws Exception {
		int noOfPending = pendingAndRunningInstIds.size();
		boolean isWaiting = true;
		while (isWaiting) {
			Thread.sleep(1000);
			DescribeInstancesResult r = VMProvisioner.ec2client.describeInstances();
			Iterator<Reservation> ir = r.getReservations().iterator();
			List<Reservation> ir1 =  r.getReservations();
			for(Reservation reservation:ir1){
				List<Instance> instances = reservation.getInstances();
				for(Instance ii :instances){
					if (ii.getState().getName().trim().equals("pending")){
						break;
					}
				}
			}
			
			while (ir.hasNext()) {
				Reservation rr = ir.next();
				List<Instance> instances = rr.getInstances();
				for (Instance ii : instances) {
					if (ii.getState().getName().trim().equals("pending")){
						break;
					}
					if(ii.getState().getName().trim().equals("running")){
						noOfPending--;
					}
				}
			}
			if(noOfPending==0){
				isWaiting = false;
			}
		}

		
	}
	
	
	
	
	public static boolean createSecurityGroup(String paramSecGroupName) {
		boolean result = false;
		try {
			VMProvisioner provisioner = new VMProvisioner();
			provisioner.init();
			CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
			csgr.withGroupName(paramSecGroupName).withDescription(
					"My security group");
			checkIfSecGroupExists(ec2client, paramSecGroupName);
			CreateSecurityGroupResult createSecurityGroupResult = ec2client
					.createSecurityGroup(csgr);
			Thread.sleep(2000);

			IpPermission ipPermission1 = new IpPermission();
			ipPermission1.withIpRanges("0.0.0.0/0").withIpProtocol("tcp")
					.withFromPort(22).withToPort(22);

			IpPermission ipPermission2 = new IpPermission();
			ipPermission2.withIpRanges("0.0.0.0/0").withIpProtocol("tcp")
					.withFromPort(3306).withToPort(3306);
			
			
			IpPermission ipPermission3 = new IpPermission();
			ipPermission3.withIpRanges("0.0.0.0/0").withIpProtocol("tcp")
			.withFromPort(2004).withToPort(2004);
			
			AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();
			authorizeSecurityGroupIngressRequest.withGroupName(
					paramSecGroupName).withIpPermissions(ipPermission1);
			authorizeSecurityGroupIngressRequest.withGroupName(
					paramSecGroupName).withIpPermissions(ipPermission2);
			authorizeSecurityGroupIngressRequest.withGroupName(
					paramSecGroupName).withIpPermissions(ipPermission3);
			
			ec2client
					.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);

			System.out.println("Following group:" + paramSecGroupName
					+ " is created.");
			result = true;
			// log.Info("KeyPair :" + privateKey);
			// writePemFile(privateKey, pemFilePath, pemFileName);
		} catch (Exception e) {
			System.out.println("Failure...");
			System.out.println(e.toString());
		}
		return result;
	}
	
	public static boolean checkIfSecGroupExists(AmazonEC2Client ec2Client, String secGroupName)
	{
		DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
		DescribeSecurityGroupsResult response = ec2Client.describeSecurityGroups(request);
		for (int i=0; i < response.getSecurityGroups().size(); i++){
			if (response.getSecurityGroups().get(i).getGroupName().equalsIgnoreCase(secGroupName)){
				System.out.println("group already exists, which can be used");
				//deleteSecurityGroup(secGroupName);
				return true;
			}
			
		}
		
		return false;
	  
	}
	public static boolean deleteSecurityGroup(String paramSecGroupName) {
		boolean result = false;
		try {
			VMProvisioner provisioner = new VMProvisioner();
			provisioner.init();
			DeleteSecurityGroupRequest csgr = new DeleteSecurityGroupRequest();
			csgr.setGroupName(paramSecGroupName);
			ec2client
					.deleteSecurityGroup(csgr);
			
			System.out.println("Following group:" + paramSecGroupName
					+ " is delete.");
			result = true;
			// log.Info("KeyPair :" + privateKey);
			// writePemFile(privateKey, pemFilePath, pemFileName);
		} catch (Exception e) {
			System.out.println("Failure...");
			System.out.println(e.toString());
		}
		return result;
	}
	
	
	public static boolean createKeyPair(String pemFilePath, String paramkeyName) {
		boolean result = false;
		try {
			VMProvisioner provisioner = new VMProvisioner();
			provisioner.init();
			CreateKeyPairRequest ckpr = new CreateKeyPairRequest();
			ckpr.withKeyName(paramkeyName);
			//checkIfKeyExists(ec2client, paramkeyName);
			CreateKeyPairResult ckpresult = ec2client.createKeyPair(ckpr);
			KeyPair keypair = ckpresult.getKeyPair();
			String privateKey = keypair.getKeyMaterial();
			writePemFile(privateKey, pemFilePath, paramkeyName);
			System.out.println("Following key:" + paramkeyName + " is created.");
			result = true;

			// log.Info("KeyPair :" + privateKey);

		} catch (Exception e) {
			System.out.println("Failure...");
			System.out.println(e.toString());
		}
		return result;
	}

	
	
	public static boolean checkIfKeyExists(AmazonEC2Client ec2Client, String keyName)
	{
		DescribeKeyPairsRequest request = new DescribeKeyPairsRequest();
		DescribeKeyPairsResult response = ec2Client.describeKeyPairs(request);
		for (int i=0; i < response.getKeyPairs().size(); i++){
			if (response.getKeyPairs().get(i).getKeyName().equalsIgnoreCase(keyName)){
				System.out.println("key already exists, can be used");
				//deleteKeyPair(keyName);
				return true;
			}
			
		}
		return false;
	  
	}
	public static boolean deleteKeyPair(String paramkeyName) {
		boolean result = false;
		try {
			VMProvisioner provisioner = new VMProvisioner();
			provisioner.init();
			DeleteKeyPairRequest ckpr = new DeleteKeyPairRequest();
			ckpr.setKeyName(paramkeyName);
			ec2client.deleteKeyPair(ckpr);
			System.out
					.println("Following key:" + paramkeyName + " is deleted.");
			result = true;

			// log.Info("KeyPair :" + privateKey);

		} catch (Exception e) {
			System.out.println("Failure...");
			System.out.println(e.toString());
		}
		return result;
	}
	
	
	public static void writePemFile(String privateKey, String pemFilePath,
			String keyname) {
		try {
			PrintWriter writer = new PrintWriter(pemFilePath + File.separator + keyname
					+ ".pem", "UTF-8");
			writer.print(privateKey);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void justLaunchVMs(String vmType, int noOfInstances, String path)throws IOException {
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		if(!checkIfSecGroupExists(ec2client, groupName)){
			createSecurityGroup(groupName);
		}
		if(!checkIfKeyExists(ec2client, keyName)){
			createKeyPair(path, keyName);
		}
		/*
		if(checkIfKeyExists(ec2client, keyName)){
			deleteKeyPair(keyName);
		}
		String pemFilePath = path;
		createKeyPair(pemFilePath, keyName);
		/*if(!checkIfKeyExists(ec2client, keyName)){
			createKeyPair(path, keyName);
		}*/
		provisioner.createEC2Instance(vmType, noOfInstances);
	}
	
	
	
	public static void justLaunchVMs(int noOfInstances) throws IOException {
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		createSecurityGroup(groupName);
		createKeyPair("", keyName);
		provisioner.createEC2Instance(noOfInstances);
	}

	public static ArrayList<String> getAvailableInstIDs(String vmType){
		ArrayList<String> resultList = new ArrayList<String>();
		VMProvisioner provisioner = new VMProvisioner();
		try {
			provisioner.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		DescribeInstancesResult result = VMProvisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while(i.hasNext()){
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			for (Instance ii : instances) {
				if (ii.getState().getName().equals("running")) {
					if(ii.getInstanceType().equals(vmType)){
						resultList.add(ii.getInstanceId());
					}
				
				}
			}
		}
		return resultList;
		
	}
	
	
	
	public static ArrayList<String> getAvailableInstIds() throws IOException {
		ArrayList<String> resultList = new ArrayList<String>();
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		DescribeInstancesResult result = VMProvisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			for (Instance ii : instances) {
				if (ii.getState().getName().equals("running")) {
					resultList.add(ii.getInstanceId());
				}
			}
		}
		return resultList;
	}
	
	
	public static Map<String,LinkedList<String>> getIPaddressesAndVMType(ArrayList<String> instIds) throws IOException {
		Map<String,LinkedList<String>> resultMap = new HashMap<String,LinkedList<String>>();
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		DescribeInstancesResult result = VMProvisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			for (Instance ii : instances) {
				for (String instId : instIds) {
					if (ii.getInstanceId().trim().equals(instId))
						if(resultMap.containsKey(ii.getInstanceType())){
							LinkedList<String> tmp = resultMap.get(ii.getInstanceType());
							tmp.add(ii.getPublicIpAddress());
							resultMap.put(ii.getInstanceType(), tmp);
						}
						else{
							LinkedList<String> tmp = new LinkedList<String>();
							tmp.add(ii.getPublicIpAddress());
							resultMap.put(ii.getInstanceType(), tmp);
						}
						
				}
			}
		}
		return resultMap;
	}

	public static ArrayList<String> getIPaddresses(ArrayList<String> instIds) throws IOException {
		ArrayList<String> resultList = new ArrayList<String>();
		VMProvisioner provisioner = new VMProvisioner();
		provisioner.init();
		DescribeInstancesResult result = VMProvisioner.ec2client.describeInstances();
		Iterator<Reservation> i = result.getReservations().iterator();
		while (i.hasNext()) {
			Reservation r = i.next();
			List<Instance> instances = r.getInstances();
			for (Instance ii : instances) {
				for (String instId : instIds) {
					if (ii.getInstanceId().trim().equals(instId))
						resultList.add(ii.getPublicIpAddress());
				}
			}
		}
		return resultList;
	}
	
	public static void vmTerminationAndKeyDelete(String workflowLibDir){
		try {
			VMProvisioner.parametersetting(workflowLibDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> vms = null;
		try {
			vms = VMProvisioner.getAvailableAndPendingInstIds();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String vmid:vms){
			VMProvisioner.terminateInstance(vmid);
		}
		VMProvisioner.deleteKeyPair(keyName);		
	}
	

}