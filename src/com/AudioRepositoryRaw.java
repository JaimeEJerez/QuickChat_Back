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
@WebServlet("/AudioRepositoryRaw")
public class AudioRepositoryRaw extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AudioRepositoryRaw() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String userID 		= request.getParameter( "userID" );
		String audioUUID 	= request.getParameter( "audioUUID" );
		

		response.setContentType("audio/snd");
				
		DataOutputStream dos = new DataOutputStream( response.getOutputStream() );
				
		Repository.toStream( userID, audioUUID, "AUDIO", dos, false );		
		
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
