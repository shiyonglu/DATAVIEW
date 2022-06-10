package servlets;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns={"/profileImage/*"})
public class ImageServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ImageServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		try{
			String account = (String)request.getParameter("id");
			String path = getServletContext().getRealPath(request.getServletPath()).replace("profileImage", "") + "WEB-INF" + File.separator + "systemFiles" + File.separator + account;
			System.out.println(path);
			File image = new File(path, URLDecoder.decode("/profileImage.png", "UTF-8"));
			response.reset();
	        response.setContentType("image/png");
	        response.setHeader("Content-Length", String.valueOf(image.length()));

	        // Write image content to response.
	        Files.copy(image.toPath(), response.getOutputStream());
  
     	}
     	catch(Exception e)
     	{
     		System.out.println(e.toString());
     	}
     	
	}

}
