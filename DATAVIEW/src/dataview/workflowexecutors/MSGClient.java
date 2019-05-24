package dataview.workflowexecutors;



import java.io.*;
import java.net.*;

import dataview.models.Dataview;

/**
 * The client code will create a sock and connect to the server socket through port number 2004.
 * @author changxinbai
 *
 */


public class MSGClient extends Thread {
	Socket requestSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	Message message;
	String remoteIP;
	int remotePort = 2004;
	int taskId;
		
	private String resp = "";

	MSGClient(String ip, Message msg) throws UnknownHostException, IOException {
		remoteIP = ip;
		message = msg;
		requestSocket = new Socket(remoteIP, remotePort);
	}

	public void run() {
		send();
	}

	public String send() {
		//String ret = "";
		try {
			// 1. creating a socket to connect to the server
			// requestSocket = new Socket("localhost", 2004);
			// System.out.println("Connected to localhost in port 2004");
			// 2. get Input and Output streams
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			// out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			// 3: Communicating with the server

			sendMessage(message);
			//System.out.println("send " + message);
			Dataview.debugger.logSuccessfulMessage("send " + message);
			// wait until recv the FIN from the remote node
			
			resp = (String) in.readObject();
			
			//Dataview.debugger.logSuccessfulMessage("local recv " + ret);
			//System.out.println("local recv " + resp);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finally {
			// 4: Closing connection
			try {
				 in.close();
				out.close();
				requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
		return resp;
	}

	void sendMessage(Message msg) {
		try {
			out.writeObject(msg);
			out.flush();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	public String getResp() {
		return resp;
	}
}