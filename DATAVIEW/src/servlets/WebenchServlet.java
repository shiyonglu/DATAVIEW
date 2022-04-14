package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns={"/webench/*"})
public class WebenchServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	 /**
     * @see HttpServlet#HttpServlet()
     */
    public WebenchServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		try{
			request.setAttribute("userId","123@gmail.com");
			HttpSession session = request.getSession(true);
 			session.setAttribute("UserID", "123@gmail.com");
     		getServletConfig().getServletContext().getRequestDispatcher(
 			        "/workflow.jsp").forward(request,response);
  
     	}
     	catch(Exception e)
     	{
     		System.out.println(e.toString());
     	}
     	
	}
}
