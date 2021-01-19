package dataview.workflowexecutors;

import java.net.InetAddress;

import dataview.models.Dataview;

public class MoveDataToCloud {
	public static void getDataReady(String Location, String ip, String keyName) throws Exception{
		//
		String destination_dir = "/home/ubuntu/";
		CmdLineDriver.copyFile("/home/ubuntu/"+Location, destination_dir, ip.trim(), keyName);
		Dataview.debugger.logSuccessfulMessage("#Data on " + ip + " is ready!!");
	
	}
}
