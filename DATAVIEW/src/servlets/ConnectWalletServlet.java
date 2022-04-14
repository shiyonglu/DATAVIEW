package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dataview.models.User;

@WebServlet(urlPatterns={"/connectWallet/*"})
public class ConnectWalletServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	  /**
     * @see HttpServlet#HttpServlet()
     */
    public ConnectWalletServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		try{
     		getServletConfig().getServletContext().getRequestDispatcher(
 			        "/connectWallet.jsp").forward(request,response);
  
     	}
     	catch(Exception e)
     	{
     		System.out.println(e.toString());
     	}
     	
	}
}
