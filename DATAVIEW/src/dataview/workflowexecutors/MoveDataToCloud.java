package dataview.workflowexecutors;



public class MoveDataToCloud {
	public static void getDataReady(String Location, String ip) throws Exception{
		//
		String destination_dir = "/home/ubuntu/";
		
			CmdLineDriver.copyFile(Location, destination_dir, ip.trim());
			System.out.println("#Data on " + ip + " is ready!!");
	
	}
}
