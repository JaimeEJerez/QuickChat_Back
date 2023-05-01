package quick.chat;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globals.Globals;

import quick.chat.utils.MyCookie;

/**
 * Retorna la imagen del Avatar en formato .jpg
 * 
 */
@WebServlet("/GetAvatarImage")
public class GetAvatarImage extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetAvatarImage() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String 	fname 			= MyCookie.getCookiesValueByName( request , MyCookie.CookieNames.USER_UUID ) + ".png";
		
		String rootDirectory = Globals.rootDirectory;

		File avatarImagesDir = new File(rootDirectory + "/" + Globals.avatarImgsDir);

		if (!avatarImagesDir.exists())
		{
			avatarImagesDir.mkdir();
		}
				
		String fPath =  avatarImagesDir.getAbsolutePath() + "/" + fname;
		
		File imageFile = new File( fPath );
		
		InputStream inpuStrem = null;
		
		if ( imageFile.exists() )
		{
			inpuStrem = new FileInputStream( imageFile );
		}
		else
		{
			inpuStrem 	= this.getServletContext().getResourceAsStream( "assets/img/avatar.png" );
		}
		
		byte[] 	buff = new byte[1024*16];
		int		size = 0;
		
		response.setContentType("image/png");
		
		ServletOutputStream os = response.getOutputStream();
		
		while ( (size = inpuStrem.read( buff )) > 0 )
		{
			os.write(buff, 0, size);
		}
		
		inpuStrem.close();
		
		os.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

}
