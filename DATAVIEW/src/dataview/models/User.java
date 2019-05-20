package dataview.models;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.servlet.ServletContext;

/**
 * This class maintains all the information about the current user. Such
 * information is brought into the memory-based Java object and then is stored
 * back to the users.table file when necessary. A lock mechanism is necessary to
 * deal the conflicts between different concurrent users in terms of read and
 * write. It is also important to maintain the consistency between the state of
 * the User Java object and the content of the user.table file. Thanks to the
 * isolation of different users, each user will only allow to access his own
 * information by authentication (user/password). A one-time session key is
 * generated for each login, so that authentication can be performed based on
 * the session key from then on. More careful analysis needs to be done about
 * the vulnerability of our approach.
 * 
 * Only three methods are public now: login and signup, updatePassword, we will
 * implement more update methods in the future, for example, change the
 * credentials of Dropbox and EC2.
 * 
 * @author shiyong lu
 *
 */
public class User {
	public static String usertable = "";

	public String username = "";
	public String password = "";
	//public String sessionkey = ""; // this line is not stored in the user table
									// in the disk.

	public String firstname = "";
	public String lastname = "";
	public String email = "";
	public String organization = "";
	public String jobtitle = "";
	public String country = "";

	// dropbox confidentials
	public String dropboxToken = "";

	// Amazon confidentials
	public String amazonec2_accesskey = "";
	public String amazonec2_secretkey = "";

	public String other1 = "";
	public String other2 = "";
	public String other3 = "";

	/**
	 * A user will provide a username and password to login into the system. If
	 * successful, the login method will return true and all the information
	 * about the user will be return back to the User object. Otherwise,
	 * 
	 * @param username
	 * @return
	 */
	public User(String email, String password, String tableLocation){
		this.email = email;
		this.password = password;
		this.usertable = tableLocation;
	}
	
	
	public User(String username, String email, String organization,String jobtitle, String country, String password,String usertable){
		this.username = username;
		this.email = email;
		this.organization = organization;
		this.jobtitle = jobtitle;
		this.country = country;
		this.password = password;
		this.usertable = usertable;
	}
	
	public boolean login(String email, String password) {
		boolean loginsuccess = false;
		FileInputStream in = null;
		String information ;
		BufferedReader br = null;
		java.nio.channels.FileLock lock = null;
		try {
			in = new FileInputStream(usertable);
			lock = in.getChannel().lock(0L, Long.MAX_VALUE, true);
			 br = new BufferedReader(new InputStreamReader(in));
			 while( (information = br.readLine() ) != null) {
				 String[] informationItem = information.split( ",");
					if (informationItem[1].equals(email) && informationItem[5].equals(password)) {
						// successful login
						loginsuccess = true;
					}   
				}
			// information = br.readLine();
			
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
		return loginsuccess;
	}

	public boolean signup() {
		FileInputStream in = null;
		java.nio.channels.FileLock lock = null;
		try {
			in = new FileInputStream(usertable);
			lock = in.getChannel().lock(0L, Long.MAX_VALUE, true);
			if (existsUser(this.email, in)) {
				return false;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				lock.release();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		appendARecord();
		return true;
	}

	public boolean updatePassword(String email, String password, String newpassword) {
		boolean updatesuccess = false;
		// lock the user table until we succeed
		String information;
		try {
			// back up the userTable file
			File src = new File(usertable);
			File dest = new File(usertable + ".bak");
			Files.copy(src.toPath(), dest.toPath());
			src.delete();

			BufferedReader br;
			br = new BufferedReader(new FileReader(usertable + ".bak"));

			do {
				information = br.readLine();
				String[] informationItem = information.split(",");
				if (informationItem[1].equals(email) && informationItem[6].equals(password)) {
					this.password = newpassword;
					updatesuccess = true;
				}
				appendARecord();
			} while (br.readLine() != null);

			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}

		return updatesuccess;
	}

	
	private void appendARecord() {
		FileLock lock = null;
		try {
			Path path = Paths.get(usertable);
			FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
			lock = fileChannel.lock();
			fileChannel.position(fileChannel.size());
			
			if(fileChannel.size()!=0){
				fileChannel.write(ByteBuffer.wrap(("\n").getBytes()));
			}
			
			fileChannel.write(ByteBuffer.wrap((username+",").getBytes()));
			fileChannel.write(ByteBuffer.wrap((email+",").getBytes()));
			fileChannel.write(ByteBuffer.wrap((organization+",").getBytes()));
			fileChannel.write(ByteBuffer.wrap((jobtitle+",").getBytes()));
			fileChannel.write(ByteBuffer.wrap((country+",").getBytes()));
			fileChannel.write(ByteBuffer.wrap((password+",").getBytes()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				lock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean existsUser(String email, FileInputStream in) {
		boolean exists = false;
		try {
			BufferedReader br;
			String information;
			br = new BufferedReader(new InputStreamReader(in));
			while((information = br.readLine())!=null){
				String[] informationItem = information.split(",");
				if (informationItem[1].equals(email)) {
					// successful login
					exists = true;
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		} catch (IOException e) {
			e.printStackTrace();
			Dataview.debugger.logException(e);
		}
		return exists;

	}

}
