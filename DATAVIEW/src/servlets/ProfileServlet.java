package servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dataview.models.User;

@WebServlet(urlPatterns={"/profile/*"})
public class ProfileServlet extends HttpServlet{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	 /**
     * @see HttpServlet#HttpServlet()
     */
    public ProfileServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		try{
			String userId = (String)request.getParameter("userId");
			request.setAttribute("userId", userId);
     		getServletConfig().getServletContext().getRequestDispatcher(
 			        "/profile.jsp").forward(request,response);
  
     	}
     	catch(Exception e)
     	{
     		System.out.println(e.toString());
     	}
     	
	}

}
