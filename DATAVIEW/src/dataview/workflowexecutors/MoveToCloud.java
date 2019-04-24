package dataview.workflowexecutors;

/**
 * This is the class that provides the cloud resource provisioner functionalities. 
 * @author  Aravind Mohan.
 */

import java.util.ArrayList;

import dataview.models.Dataview;

public class MoveToCloud {
	public static void getCodeReady(ArrayList<String> ips) throws Exception {
		for (int i = 0; i < ips.size(); i++) {
			String strCommand = "sudo kill -9 $(sudo lsof -t -i:2004)";
			CmdLineDriver.executeCommands(ips.get(i), strCommand);
			String strCommand1 = "nohup java -Xmx700m -jar TaskExecutor.jar > /dev/null 2>&1 &";
			CmdLineDriver.executeCommands(ips.get(i), strCommand1);
			String strCommand2 = "rm *.txt";
			CmdLineDriver.executeCommands(ips.get(i), strCommand2);
			String strCommand3 = "rm *.class";
			CmdLineDriver.executeCommands(ips.get(i), strCommand3);
		}
	}

	public static void getFileReady(String Location, ArrayList<String> ips) throws Exception {
		String destination_dir = "/home/ubuntu/";
		for (int i = 0; i < ips.size(); i++) {
			CmdLineDriver.copyFile(Location, destination_dir, ips.get(i).trim());
			Dataview.debugger.logSuccessfulMessage(Location + "file on " + ips.get(i) + " is ready!!");
			System.out.println(Location + " file on " + ips.get(i) + " is ready!!");
		}
	}

	public static void getFileReadyOneIP(String Location, String ip) throws Exception {
		String destination_dir = "/home/ubuntu/";
		CmdLineDriver.copyFile(Location, destination_dir, ip);
		System.out.println("The current file "+ Location + " is ready");
	}

}
