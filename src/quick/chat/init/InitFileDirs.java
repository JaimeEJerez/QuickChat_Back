package quick.chat.init;


import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globals.Globals;

import quick.chat.utils.Util;

/**
 * Inicializa los directorios internos
 */
@WebServlet("/InitDirs")
public class InitFileDirs extends HttpServlet 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 133242L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public InitFileDirs() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.getWriter().append("InitDirs\r\n");
		
		response.getWriter().append( "Globals.rootDirectory:" + Globals.rootDirectory + "\r\n" );
				
		String directPath = Util.createDirectoryTree( Globals.rootDirectory );
		
		response.getWriter().append("createDirectoryTree:").append( directPath );
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		doGet(request, response);
	}

}
