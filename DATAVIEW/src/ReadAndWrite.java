import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import dataview.models.Dataview;

/**
 * This class is used to read and write user information for multiple users, which has three static methods.
 * The user information in the file with this format below.
 * Changxin Bai,baichangxin@gmail.com,self-sells,self-sells,United States,123, dropbox token, EC2 accesskey, EC2 secretkey,
 */

public class ReadAndWrite {
	// Write the dropbox token into a file
	public static void write(String filename, String userId, String dropboxToken ){
		File src = new File(filename);
		File dest = new File(filename + ".bak");
		try {
			Files.copy(src.toPath(), dest.toPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		src.delete();
		FileInputStream in = null;
		String information ;
		BufferedReader br = null;
		 StringBuffer inputBuffer = new StringBuffer();
		java.nio.channels.FileLock lock = null;
		try {
			in = new FileInputStream(filename + ".bak");
			lock = in.getChannel().lock(0L, Long.MAX_VALUE, true);
			 br = new BufferedReader(new InputStreamReader(in));
			
			while((information = br.readLine())!=null){
				String[] informationItem = information.split(",");
				if(informationItem[1].equals(userId)){
					String tmp = information + dropboxToken + ",";
					inputBuffer.append(tmp);
					inputBuffer.append('\n');
				}else{
					inputBuffer.append(information);
					inputBuffer.append('\n');
				}
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				lock.release();
				in.close();
				br.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		String inputStr = inputBuffer.toString();
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(filename);
			fileOut.write(inputStr.getBytes());
		    fileOut.close();
		} catch (FileNotFoundException e) {
			Dataview.debugger.logException(e);
			e.printStackTrace();
		} catch (IOException e) {
			Dataview.debugger.logException(e);
			e.printStackTrace();
		}
		dest.delete();
		
	}
	// Write the access key and secret key into file 
	public static void write(String filename, String userId, String accessKey,String secretKey ){
		File src = new File(filename);
		File dest = new File(filename + ".bak");
		try {
			Files.copy(src.toPath(), dest.toPath());
		} catch (IOException e1) {
			Dataview.debugger.logException(e1);
			e1.printStackTrace();
		}
		src.delete();
		FileInputStream in = null;
		String information ;
		BufferedReader br = null;
		 StringBuffer inputBuffer = new StringBuffer();
		java.nio.channels.FileLock lock = null;
		try {
			in = new FileInputStream(filename + ".bak");
			lock = in.getChannel().lock(0L, Long.MAX_VALUE, true);
			 br = new BufferedReader(new InputStreamReader(in));
			
			while((information = br.readLine())!=null){
				String[] informationItem = information.split(",");
				if(informationItem[1].equals(userId)){
					String tmp = information + accessKey + "," + secretKey +",";
					inputBuffer.append(tmp);
					inputBuffer.append('\n');
				}else{
					inputBuffer.append(information);
					inputBuffer.append('\n');
				}
				
			}
		} catch (FileNotFoundException e) {
			Dataview.debugger.logException(e);
			e.printStackTrace();
		} catch (IOException e) {
			Dataview.debugger.logException(e);
			e.printStackTrace();
		} finally {
			try {
				lock.release();
				in.close();
				br.close();
				
			} catch (IOException e) {
				Dataview.debugger.logException(e);
				e.printStackTrace();
			}

		}
		String inputStr = inputBuffer.toString();
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(filename);
			fileOut.write(inputStr.getBytes());
		    fileOut.close();
		} catch (FileNotFoundException e) {
			Dataview.debugger.logException(e);
			e.printStackTrace();
		} catch (IOException e) {
			Dataview.debugger.logException(e);
			e.printStackTrace();
		}
		dest.delete();
		
	}
	
	// Read the token or access key and secret key from file with the column id.
	public static String read(String filename, String userId, int i ){
		String dropboxToken = "";
		FileInputStream in = null;
		String information ;
		BufferedReader br = null;
		java.nio.channels.FileLock lock = null;
		try {
			in = new FileInputStream(filename);
			lock = in.getChannel().lock(0L, Long.MAX_VALUE, true);
			 br = new BufferedReader(new InputStreamReader(in));
			while((information = br.readLine())!=null){
				String[] informationItem = information.split(",");
				if (informationItem[1].equals(userId)&&informationItem.length > i) {
					dropboxToken =  informationItem[i];
				}
			}
						
		} catch (FileNotFoundException e) {
			Dataview.debugger.logException(e);
			e.printStackTrace();
		} catch (IOException e) {
			Dataview.debugger.logException(e);
			e.printStackTrace();
		} finally {
			try {
				lock.release();
				in.close();
				br.close();
			} catch (IOException e) {
				Dataview.debugger.logException(e);
				e.printStackTrace();
			}

		}
		return dropboxToken;
	}
	
}
