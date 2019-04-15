/**
 * This servlet is used for setting up user access.
 * @author Aravind Mohan.
 *
 */

package usermgmt;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dataview.models.User;



/**
 * Servlet implementation class UserLogin
 */

public class UserLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserLoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		
		String emailId = (String)request.getParameter("txtEmailId");
     	String passwrd = (String)request.getParameter("txtPasswd");
     	Encrypt encrypt = null;
		try {
			encrypt = new Encrypt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		passwrd = encrypt.encrypt(passwrd);
     	String tableLocation = getServletContext().getRealPath(request.getServletPath()).replace("UserLogin", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator + "users.table";
     	try{
     		//WebbenchUtility.initializeWebbenchConfig();
     		User user = new User(emailId,passwrd,tableLocation);
     		if(user.login(user.email, user.password))
     		{
     			//succesful logic
     			request.setAttribute("userId",emailId);
     			HttpSession session = request.getSession(true);
     			session.setAttribute("UserID", emailId);
     			getServletConfig().getServletContext().getRequestDispatcher(
     			        "/workflow.jsp").forward(request,response);
     		}
     		else
     		{
     			//failure logic
     			request.setAttribute("statusMsg","Incorrect credentials, please try again.");
     			getServletConfig().getServletContext().getRequestDispatcher(
     			        "/login.jsp").forward(request,response);
     		}
     	}
     	catch(Exception e)
     	{
     		System.out.println(e.toString());
     	}
     	
	}

}
