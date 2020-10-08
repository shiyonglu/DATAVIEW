import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import usermgmt.Encrypt;



/**
 * This class is used to read and write user information for multiple users, which has three static methods.
 * The user information in the file with this format below.
 * Changxin Bai,baichangxin@gmail.com,self-sells,self-sells,United States,123, dropbox token, EC2 accesskey, EC2 secretkey,
 */

public class ReadAndWrite {
	// Write the dropbox token into a file

	
	public static void write(String filename, String userId, String token, int index){
		File src = new File(filename);
		File dest = new File(filename + ".bak");
		try {
			Files.copy(src.toPath(), dest.toPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		 Encrypt encrypt = null;
			try {
				encrypt = new Encrypt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		FileInputStream in = null;
		String information ;
		BufferedReader br = null;
		 StringBuffer inputBuffer = new StringBuffer();
		java.nio.channels.FileLock lock = null;
		try {
			in = new FileInputStream(filename + ".bak");
			lock = in.getChannel().lock(0L, Long.MAX_VALUE, true);
			 br = new BufferedReader(new InputStreamReader(in));
			 List<String> allLines = Files.readAllLines(Paths.get(filename));
			 src.delete();
			 for(int i = 0; i < allLines.size(); i++){
				 information = allLines.get(i);
				 String[] informationItem = information.split(",");
				 if(informationItem[1].equals(userId)){
					 	token = encrypt.encrypt(token);
					 	if(informationItem.length <= index){
					 		String tmp = information + token + ",";
							inputBuffer.append(tmp);
					 	}else{
					 		informationItem[index] = token;
					 		String tmp = new String("");
					 		for(int j =0; j< informationItem.length; j++){
					 			tmp += informationItem[j]+",";
					 		}
					 		inputBuffer.append(tmp);
					 	}
					 	
					}else{
						inputBuffer.append(information);
					}
					if(i<allLines.size()-1){
						inputBuffer.append(System.getProperty("line.separator"));
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
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		dest.delete();
		
	}
	// Write the access key and secret key into file 
	public static void write(String filename, String userId, String accessKey,String secretKey , int accessKeyIndex, int secretKeyIndex){
		 Encrypt encrypt = null;
			try {
				encrypt = new Encrypt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		File src = new File(filename);
		File dest = new File(filename + ".bak");
		try {
			Files.copy(src.toPath(), dest.toPath());
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		FileInputStream in = null;
		String information ;
		BufferedReader br = null;
		 StringBuffer inputBuffer = new StringBuffer();
		java.nio.channels.FileLock lock = null;
		try {
			in = new FileInputStream(filename + ".bak");
			lock = in.getChannel().lock(0L, Long.MAX_VALUE, true);
			 br = new BufferedReader(new InputStreamReader(in));
			 List<String> allLines = Files.readAllLines(Paths.get(filename));
			 src.delete();
			 for(int i = 0; i < allLines.size(); i++){
				 information = allLines.get(i);
				 String[] informationItem = information.split(",");
				 if(informationItem[1].equals(userId)){
					accessKey = encrypt.encrypt(accessKey);
					secretKey = encrypt.encrypt(secretKey);
					
					System.out.println("-------"+ informationItem.length);
					if(informationItem.length < (accessKeyIndex+1)){
						String tmp = information + accessKey + "," + secretKey +",";
						inputBuffer.append(tmp);
					
					}else if(informationItem.length >= (accessKeyIndex+1) && informationItem.length< (secretKeyIndex+1)){
						informationItem[accessKeyIndex] = accessKey;
						String tmp = new String("");
				 		for(int j =0; j< informationItem.length; j++){
				 			tmp += informationItem[j]+",";
				 		}
						tmp = tmp + secretKey +",";
						inputBuffer.append(tmp);
					}else{
						informationItem[accessKeyIndex] = accessKey;
						informationItem[secretKeyIndex] = secretKey;
						String tmp = new String("");
				 		for(int j =0; j< informationItem.length; j++){
				 			tmp += informationItem[j]+",";
				 		}
				 		inputBuffer.append(tmp);
					}
					
					 
				 
				 }else{
						inputBuffer.append(information);
					}
					if(i<allLines.size()-1){
						inputBuffer.append(System.getProperty("line.separator"));
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
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		dest.delete();
		
	}
	
	// Read the token or access key and secret key from file with the column id.
	public static String read(String filename, String userId, int i ){
		 Encrypt encrypt = null;
			try {
				encrypt = new Encrypt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		String token = "";
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
					token =  informationItem[i];
					token = encrypt.decrypt(token);
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
		return token;
	}
	
}
