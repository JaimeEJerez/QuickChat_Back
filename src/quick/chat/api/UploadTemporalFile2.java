package quick.chat.api;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.globals.Globals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tcp.JSONResponse;

import quick.chat.utils.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Servlet implementation class UploadFile
 */
@MultipartConfig
@WebServlet("/UploadTemporalFile2")
public class UploadTemporalFile2 extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadTemporalFile2()
	{
		super();
	}

	public static String getFilename(Part part)
	{
		for (String cd : part.getHeader("content-disposition").split(";"))
		{
			if (cd.trim().startsWith("filename"))
			{
				String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
				
				return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
			}
		}
		return null;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{		
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 					gson 		= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 		osw 		= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		String 		userCode 	= request.getHeader("userCode");
		String 		filePath 	= request.getHeader("filePath");
		String 		pattern 	= Pattern.quote(System.getProperty("file.separator"));
		String[] 	fileSplit 	= filePath.split( pattern );
		String 		fileName 	= fileSplit[fileSplit.length-1];
		
		DataInputStream dis 		= new DataInputStream( request.getInputStream() );
		
		byte[] 			buffer 		= dis.readAllBytes();
		final File		tempDir		= new File( Globals.tempDirectory + File.separator + userCode );
		
		if (!tempDir.exists())
		{
			Util.createDirectoryTree( tempDir.getAbsolutePath() );	
		}

		if ( !tempDir.isDirectory() )
		{
			gson.toJson( JSONResponse.not_success( 1701, "ERROR Interno: " + tempDir.getAbsolutePath() + " no es un directorio." ), osw );
			osw.flush();
			return;
		}
		
		if ( !tempDir.exists() )
		{			
			gson.toJson( JSONResponse.not_success( 1701, "ERROR Interno: El directorio " + tempDir.getAbsolutePath() + " no existe."  ), osw );
			osw.flush();
			return;
		}
		
		if ( !tempDir.canWrite() )
		{
			gson.toJson( JSONResponse.not_success( 1701, "ERROR Interno: No puede escribir en el directorio: " + tempDir.getAbsolutePath()   ), osw );
			osw.flush();
			return;
		}
		
		int		lastDotPos 	= fileName.lastIndexOf( '.' );
		String	fileType 	= lastDotPos > 0 ? fileName.substring(lastDotPos).toLowerCase() : "";
				
		boolean imageFile = fileType.endsWith(".png" ) || 
							fileType.endsWith(".jpg" ) ||
							fileType.endsWith(".bmp" ) ||
							fileType.endsWith(".gif" );
		
		if ( !imageFile )
		{
			File f = new File( tempDir + File.separator + fileName );
			
			if ( f.exists() )
			{
				if ( !f.delete() )
				{					
					gson.toJson( JSONResponse.not_success( 1701, "ERROR Interno: No pudo borrar el archivo: " + f.getAbsolutePath() ), osw );
					osw.flush();
					return;
				}
			}
			
			f.createNewFile();
			
			OutputStream os = new FileOutputStream( f );
			
			os.write( buffer );
			
			os.close();
						
			Hashtable<String,String> results = new Hashtable<String,String>();
			
			results.put( "fileName", f.getName() );
			results.put( "fileType", "documentType" );

			gson.toJson( JSONResponse.success( results ), osw );
			osw.flush();
		}
		else
		{			
			BufferedImage bufferedImage = fixImageSize( buffer );
			
			if ( bufferedImage != null )
			{
				UUID 	uuid 		= UUID.randomUUID();
				String 	fName 		= null;
				
				if ( fileType.endsWith(".png" ) )
				{
					fileType 	= "PNG";
					fName 		= uuid + ".png";
				}
				else
				if ( fileType.endsWith(".jpg" ) )
				{
					fileType 	= "JPEG";
					fName 		= uuid + ".jpg";
				}
				else
				if ( fileType.endsWith(".bmp" ) )
				{
					fileType 	= "BMP";
					fName 		= uuid + ".bmp";
				}
				else
				if ( fileType.endsWith(".gif" ) )
				{
					fileType 	= "GIF";
					fName 		= uuid + ".gif";
				}
				else
				{
					fileType 	= "JPEG";
					fName 		= uuid + ".jpg";
				}
				
				File f = new File( tempDir + "/" + fName );
				
				if ( f.exists() )
				{
					if ( !f.delete() )
					{
						gson.toJson( JSONResponse.not_success( 1701, "ERROR Interno: No pudo borrar el archivo: " + f.getAbsolutePath() ), osw );
						osw.flush();
						return;
					}
				}
				
				f.createNewFile();
				
				OutputStream os = new FileOutputStream( f );

				//JPEG, PNG, BMP, WEBMP, GIF
				ImageIO.write( bufferedImage, fileType, os );
				
				os.close();
				
				Hashtable<String,String> results = new Hashtable<String,String>();
				
				results.put( "fileName", fName );
				results.put( "fileType", "imageType" );
				
				gson.toJson( JSONResponse.success( results ), osw );
				osw.flush();
			}
			else
			{
				gson.toJson( JSONResponse.not_success( 1701, "ERROR: parece que el archivo no es una imagen..." ), osw );
				osw.flush();
			}
		}
	}

	public BufferedImage fixImageSize( byte[] imageBuff )
	{
		BufferedImage orign_image = null;
		BufferedImage fixed_image = null;

		ByteArrayInputStream bis = new ByteArrayInputStream(imageBuff);
					
		try
		{
			orign_image = ImageIO.read( bis );		
		}
		catch( IOException e )
		{
			return null;
		}
		
		if ( orign_image == null )
		{
			return null;
		}
		
		int h 	= orign_image.getWidth();
		int v 	= orign_image.getHeight();
		
		fixed_image = orign_image;

		double hypo = Math.hypot( h , v );
        double fact = 400.0 / hypo;
        
        if ( fact < 1.0 )
        {
	        h = (int)Math.round( h*fact );
	        v = (int)Math.round( v*fact );
	        
	        fixed_image = UploadTemporalFile.resizeImage( orign_image, h, v );
	        
	        orign_image.flush();
        }
        
		return fixed_image;
		
		/*
		// Picture with transparent background
		BufferedImage formatAvatarImage = new BufferedImage( targetImageSize,  targetImageSize, BufferedImage.TYPE_4BYTE_ABGR );
		
		Graphics2D graphics = formatAvatarImage.createGraphics();
		
		// Cut the picture into a garden
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Leave a blank area of ​​one pixel, this is very important, cover this when
		// drawing a circle
		int border = 1;
		// The picture is a circle
		Ellipse2D.Double shape = new Ellipse2D.Double(border, border, targetImageSize - border * 2, targetImageSize - border * 2);
		// The area to be reserved
		graphics.setClip(shape);
		
		if ( wider )
		{
			graphics.drawImage( image, border - (targetWidth-targetImageSize)/2, border, targetWidth - border * 2, targetImageSize - border * 2, null );
		}
		else
		{
			graphics.drawImage( image, border, border - (targetHeight-targetImageSize)/2, targetImageSize - border * 2, targetHeight - border * 2, null );
		}
		
		graphics.dispose();
		
		// Draw another circle outside the circle chart
		// Create a new graphic so that the circle drawn will not be jagged
		graphics = formatAvatarImage.createGraphics();
		
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int border1 = 3;
		// The brush is 4.5 pixels, the use of BasicStroke can check the following
		// reference document
		// When making the brush, it will basically extend a certain pixel like the
		// outside, and you can test it when you can use it
		Stroke s = new BasicStroke(5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		graphics.setStroke(s);
		graphics.setColor(Color.WHITE);
		graphics.drawOval(border1, border1, targetImageSize - border1 * 2, targetImageSize - border1 * 2);
		graphics.dispose();
		
		return formatAvatarImage;
		*/
	}

	/**
	 * Reduce Image, this method returns the image after the source image is scaled
	 * under the given width and height restrictions
	 *
	 * @param inputImage : Width after compression : Height after compression
	 * @throws java.io.IOException return
	 */
	public static BufferedImage resizeImage(BufferedImage inputImage, int newWidth, int newHeight)
	{
		// Get the original image transparency type
		try
		{
			int type 	= BufferedImage.TYPE_INT_RGB;//inputImage.getColorModel().getTransparency();
			int width 	= inputImage.getWidth();
			int height 	= inputImage.getHeight();
			// Turn on anti-aliasing
			RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// Use high quality compression
			renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			BufferedImage img = new BufferedImage(newWidth, newHeight, type);
			Graphics2D graphics2d = img.createGraphics();
			graphics2d.setRenderingHints(renderingHints);
			graphics2d.drawImage(inputImage, 0, 0, newWidth, newHeight, 0, 0, width, height, null);
			graphics2d.dispose();
			return img;

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
