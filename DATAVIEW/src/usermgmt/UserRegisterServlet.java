/**
 * This servlet is used for setting up user access.
 * @author Aravind Mohan.
 *
 */

package usermgmt;

import java.io.File;

import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import dataview.models.User;



/**
 * Servlet implementation class UserRegister 
 */

public class UserRegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserRegisterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Inside servlet now...");
		// TODO Auto-generated method stub
     	String strUserName = (String)request.getParameter("input-name");
     	String strEmailID = (String)request.getParameter("input-email");
     	String strOrganization = (String)request.getParameter("input-organization");
     	String strTitle = (String)request.getParameter("input-title");
     	String strCountry = (String)request.getParameter("country");
     	String strPass = (String)request.getParameter("input-password");
     	Encrypt encrypt = null;
		try {
			encrypt = new Encrypt();
		} catch (Exception e) {
			e.printStackTrace();
		}
     	strPass = encrypt.encrypt(strPass);
     	String tableLocation = getServletContext().getRealPath(request.getServletPath()).replace("UserReg", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator + "users.table";
     	
     	System.out.println(tableLocation);
     	User user = new User(strUserName, strEmailID, strOrganization, strTitle, strCountry, strPass, tableLocation);
     	if(user.signup()){
     		request.setAttribute("statusMsg", "<br/> Registration success.. <br/>");
 			getServletConfig().getServletContext().getRequestDispatcher(
 			        "/login.jsp").forward(request,response);
 			System.out.println("Success");
     	}
     	else{
     		request.setAttribute("statusMsg", "<br/>Account already exists. <br/> <br/>");
     		getServletConfig().getServletContext().getRequestDispatcher("/login.jsp").forward(request,response);
     		//response.sendRedirect("login.jsp");
     	}
     	
		
     	
	}

}
