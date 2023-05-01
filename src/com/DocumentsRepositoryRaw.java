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
@WebServlet("/DocumentsRepositoryRaw")
public class DocumentsRepositoryRaw extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DocumentsRepositoryRaw() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//userID=SU00000002&documentID=El%20Gita.zip
		String documentID 	= request.getParameter( "documentID" );
		String userID 		= request.getParameter( "userID" );

		response.setContentType("APPLICATION/OCTET-STREAM");   
		
		response.setHeader("Content-Disposition",  "attachment; filename=\"" + documentID + "\"");		
		
		DataOutputStream dos = new DataOutputStream( response.getOutputStream() );
				
		Repository.toStream( userID, documentID, "DOCUMENT", dos, false );		
		
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
