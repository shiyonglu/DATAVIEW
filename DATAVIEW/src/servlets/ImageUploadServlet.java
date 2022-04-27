package servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;


@WebServlet(urlPatterns={"/profileImageUpload/*"})
@MultipartConfig(
		  fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
		  maxFileSize = 1024 * 1024 * 10,      // 10 MB
		  maxRequestSize = 1024 * 1024 * 100   // 100 MB
		)
public class ImageUploadServlet extends HttpServlet{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	 /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageUploadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	
	 public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		    /* Receive file uploaded to the Servlet from the HTML5 form */
		    Part filePart = request.getPart("image");
		    String account = (String)request.getParameter("userId");
		    String path = getServletContext().getRealPath(request.getServletPath()).replace("profileImageUpload", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator + account + File.separator;
		    String fileName = "profileImage.png";
		    for (Part part : request.getParts()) {
		      part.write(path + fileName);
		    }
		    response.getWriter().print("The file uploaded sucessfully.");
		    getServletConfig().getServletContext().getRequestDispatcher(
 			        "/profile.jsp").forward(request,response);
		  }

}
