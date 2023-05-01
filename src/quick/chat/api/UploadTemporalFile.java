package quick.chat.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.globals.Globals;

import quick.chat.utils.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Servlet implementation class UploadFile
 */
@MultipartConfig
@WebServlet("/UploadTemporalFile")
public class UploadTemporalFile extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadTemporalFile()
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
		java.io.PrintWriter	out = response.getWriter();
		
		Object object = request.getAttribute("file");
		
		final Part 			uploadedFile 	= request.getPart("file");
		final InputStream 	filecontent 	= uploadedFile.getInputStream();
		final File 			tempDir 		= new File( Globals.tempDirectory + File.separator );
		final String		userID			= request.getParameter( "userID" );
		
		if (!tempDir.exists())
		{
			Util.createDirectoryTree( tempDir.getAbsolutePath() );	
		}

		if ( !tempDir.isDirectory() )
		{
			out.println("ERROR Interno: " + tempDir.getAbsolutePath() + " no es un directorio." );
			
			return;
		}
		
		if ( !tempDir.exists() )
		{
			out.println("ERROR Interno: El directorio " + tempDir.getAbsolutePath() + " no existe." );
			
			return;
		}
		
		if ( !tempDir.canWrite() )
		{
			out.println("ERROR Interno: No puede escribir en el directorio: " + tempDir.getAbsolutePath() );
			
			return;
		}
		
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream( 1024 * 32 );

		byte[] 	buff 		= new byte[10000];
		int 	readSize	= 0;

		while ((readSize = filecontent.read(buff)) > 0)
		{
			bOutput.write(buff, 0, readSize);
		}

		byte[] bytes = bOutput.toByteArray();

		bOutput.flush();

		String	fName 		= uploadedFile.getSubmittedFileName();
		int		lastDotPos 	= fName.lastIndexOf( '.' );
		String	fileType 	= lastDotPos > 0 ? fName.substring(lastDotPos).toLowerCase() : "";
				
		boolean imageFile = fileType.endsWith(".png" ) || 
							fileType.endsWith(".jpg" ) ||
							fileType.endsWith(".bmp" ) ||
							fileType.endsWith(".gif" );
		
		if ( !imageFile )
		{
			File f = new File( tempDir + File.separator + userID + File.separator + fName );
			
			if ( f.exists() )
			{
				if ( !f.delete() )
				{
					out.println("ERROR Interno: No pudo borrar el archivo: " + f.getAbsolutePath() );
					
					return;
				}
			}
			
			f.createNewFile();
			
			OutputStream os = new FileOutputStream( f );
			
			os.write(bytes);
			
			os.close();
			
			out.println( fName );		
		}
		else
		{
			UUID 	uuid 		= UUID.randomUUID();
			String 	fileName 	= uuid + fileType;
			File 	f 			= new File( tempDir + File.separator + userID + File.separator + fileName );
			
			if ( !f.getParentFile().exists() )
			{
				f.getParentFile().mkdir();
			}
			
			if ( f.exists() )
			{
				if ( !f.delete() )
				{
					out.println("ERROR Interno: No pudo borrar el archivo: " + f.getAbsolutePath() );
					
					return;
				}
			}
			
			f.createNewFile();
			
			OutputStream os = new FileOutputStream( f );
			
			os.write(bytes);
			
			os.close();
			
			out.println( fileName );		
					
			
			/*BufferedImage bufferedImage = fixImageSize( bytes );
			if ( bufferedImage != null )
			{
				UUID 	uuid 		= UUID.randomUUID();
				String 	fileName 	= uuid + ".jpg";
				File 	f 			= new File( tempDir + "/" + fileName );
				
				if ( f.exists() )
				{
					if ( !f.delete() )
					{
						out.println("ERROR Interno: No pudo borrar el archivo: " + f.getAbsolutePath() );
						
						return;
					}
				}
				
				f.createNewFile();
				
				OutputStream os = new FileOutputStream( f );
				
				//JPEG, PNG, BMP, WEBMP, GIF
				ImageIO.write( bufferedImage, "JPEG", os );
				
				os.close();
				
				out.println( fileName );
			}
			else
			{
				out.println("ERROR: parece que el archivo no es una imagen...");
			}*/
		}
	}

	public BufferedImage fixImageSize( byte[] imageBuff )
	{
		BufferedImage image = null;

		ByteArrayInputStream bis = new ByteArrayInputStream(imageBuff);
					
		try
		{
			image = ImageIO.read( bis );		
		}
		catch( IOException e )
		{
			return null;
		}
		
		
		if ( image == null )
		{
			return null;
		}
		
		boolean wider 			=  image.getWidth() > image.getHeight();
		
		int 	targetImageSize =  1024;
		int		actualImageSize =  wider ? image.getHeight() : image.getWidth();
		
		float	factor			=  (float)targetImageSize/(float)actualImageSize;
		
		if ( factor < 1.0f )
		{
			int 	targetWidth		= (int)(image.getWidth()  * factor);
			int 	targetHeight	= (int)(image.getHeight() * factor);
			
			image = resizeImage( image, targetWidth, targetHeight );
		}
		
		return image;
		
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
