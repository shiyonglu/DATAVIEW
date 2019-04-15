package dataview.workflowexecutors;

/**
 * This is the class that provides the cloud resource provisioner functionalities. 
 * such as assign a password to the launched VMs. Copy files between different VMs, and execute a shell command in a VM.
 * @author  Aravind Mohan.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class CmdLineDriver {
	public static String prvkey = WorkflowExecutor_Beta.workflowlibdir + VMProvisioner.keyName + ".pem";	
	public static void copyFile(String SourceDIR, String DestinationDIR,
			String strHostName) {
		System.out.println(prvkey);
		String SFTPHOST = strHostName;
		int SFTPPORT = 22;
		String SFTPUSER = "ubuntu";
		String SFTPWORKINGDIR = DestinationDIR;
		String FILETOTRANSFER = SourceDIR;
		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;
		try {
			JSch jsch = new JSch();
			jsch.addIdentity(prvkey);
			session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPort(22);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(SFTPWORKINGDIR);
			File f = new File(FILETOTRANSFER);
			channelSftp.put(new FileInputStream(f), f.getName());
			channel.disconnect();
			session.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		}

	}
	
	public static void executeCommands(String strHostName, String strCommand) {
		String SFTPHOST = strHostName;
		int SFTPPORT = 22;
		String SFTPUSER = "ubuntu";
		Session session = null;
		try {
			JSch jsch = new JSch();
			jsch.addIdentity(prvkey);
			session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPort(22);
			session.connect();
			
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			InputStream in = channelExec.getInputStream();
			channelExec.setCommand(strCommand);
			channelExec.connect();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String line;
			int index = 0;
			while ((line = reader.readLine()) != null) {
				System.out.println(++index + " : " + line);
			}
			
			int exitStatus = channelExec.getExitStatus();
			channelExec.disconnect();
			session.disconnect();
			if (exitStatus < 0) {
				System.out.println("Done, but exit status not set!");
			} else if (exitStatus > 0) {
				System.out.println("Done, exit status is positive");
			} else {
				System.out.println("Done!");
			}
			
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		
	
	}
	
	
	

	
	public static String executeCommandsInEC2( String pemFileLocation, String strHostName, String strUserName,
			List<String> commands){
		String strLogMessages = "";
	    try {
	    		JSch jsch = new JSch();
	    		jsch.addIdentity(pemFileLocation);
			Session session = jsch.getSession(strUserName, strHostName, 22);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
		    Channel channel=session.openChannel("shell");//only shell
	        OutputStream inputstream_for_the_channel = channel.getOutputStream();
	        PrintStream shellStream = new PrintStream(inputstream_for_the_channel, true);
	        channel.connect(); 
	        for(String command: commands) {
	            shellStream.println(command); 
	            shellStream.flush();
	        }
	        shellStream.close();
	        InputStream outputstream_from_the_channel = channel.getInputStream();
	        BufferedReader br = new BufferedReader(new InputStreamReader(outputstream_from_the_channel));
	        String line;
	        while ((line = br.readLine()) != null){
	        		strLogMessages = strLogMessages + line+"\n";
	        }
	        do {
	        } while(!channel.isEOF());
	        outputstream_from_the_channel.close();
	        br.close();
	        session.disconnect();
	        channel.disconnect();
	        return strLogMessages;
	    } catch (Exception e) { 
	        e.printStackTrace();
	    }
	    return strLogMessages;
	}
}
