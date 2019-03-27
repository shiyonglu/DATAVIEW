package dataview.workflowexecutors;

/**
 [ * This is the class that provides the cloud resource provisioner functionalities. 
 * @author  Aravind Mohan.
 */


import java.util.ArrayList;


public class MakeMachinesReady {
	public static void getMachineReady(String pemFileLocation, ArrayList<String> ips) throws Exception{
		for (String ipAddress : ips) {
			System.out.println("Preparing the data node:"+ipAddress);
		 	ArrayList<String> commands = new ArrayList<String>();
		 	commands.add("sudo su -");
		 	commands.add("echo -e \"dataview\ndataview\" | (passwd ubuntu)");
		 	commands.add("exit");
		 	commands.add("sed -\"s/PasswordAuthentication no/PasswordAuthentication yes/g\" /etc/ssh/sshd_config");
		 	commands.add("sudo service ssh restart");
		 	commands.add("exit");
		 	CmdLineDriver.executeCommandsInEC2(pemFileLocation, ipAddress.trim(), "ubuntu", commands);
	   
		}
		System.out.println("All set to go...");

	}
	
}
