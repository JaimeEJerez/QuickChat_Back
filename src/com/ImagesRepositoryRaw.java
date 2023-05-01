package com;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.managers.Repository;

/**
 * Servlet implementation class ImagesRepository
 */
@WebServlet("/ImagesRepositoryRaw")
public class ImagesRepositoryRaw extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImagesRepositoryRaw() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String imageUUID 	= request.getParameter( "imageUUID" );
		String userID 		= request.getParameter( "userID" );

		response.setContentType("image/jpeg");
		
		response.setHeader("Content-Disposition",  "attachment; filename=\"" + imageUUID + "\"");		
		
		DataOutputStream dos = new DataOutputStream( response.getOutputStream() );
				
		Repository.toStream( userID, imageUUID, "STATIC_IMAGE", dos, false );		
		
		dos.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

}
