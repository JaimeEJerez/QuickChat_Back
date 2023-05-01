package quick.chat.stress;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quick.chat.db_io.MySQL;

/**
 * Servlet implementation class GetStressStatics
 */
@WebServlet("/GetStressStatics")
public class GetStressStatics extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	static DateTimeFormatter timeFormatObj = DateTimeFormatter.ofPattern("HH:mm:ss");
	
	static quick.chat.stress.CpuStats cpuStats = new CpuStats();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetStressStatics() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		PrintWriter w = response.getWriter();
		
		ServerCounters counters = ServerCounters.getSingleton();
		
		w.append( timeFormatObj.format( LocalDateTime.now() ) ).append(";");
		
		w.append( String.valueOf( Math.round(cpuStats.getCupUsageAverage() * 100.0) ) ).append("%;");
		w.append( getFreeMemoryInMiB() ).append(";");
		
		w.append( String.valueOf(counters.getInThreads()) ).append(";");
		w.append( String.valueOf(counters.getOutThreads()) ).append(";");
		
		w.append( String.valueOf(counters.getInCount())  ).append(";");
		w.append( String.valueOf(counters.getOutCount()) ).append(";");

		w.append( String.valueOf(counters.calcInputSpeed())  ).append(" msg/s;");
		w.append( String.valueOf(counters.calcOutputSpeed()) ).append(" msg/s;");

		w.append( String.valueOf(counters.getInErrors()) ).append(";");
		w.append( String.valueOf(counters.getOutErrors()) ).append(";");
		
		w.append( String.valueOf(counters.getOutRetries()) ).append(";");
		
		w.append( String.valueOf(MySQL.countConections()) );
		
		//w.append( String.valueOf(counters.getOutRetries()) ).append(";");
		//
		//w.append( String.valueOf(counters.getLastError()) );
		
		response.flushBuffer();
	}

	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

	private static final double MEGABYTE_FACTOR = 1024L * 1024L;
	private static final DecimalFormat ROUNDED_DOUBLE_DECIMALFORMAT;
	private static final String MIB = "MiB";

	static 
	{
	    DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
	    otherSymbols.setDecimalSeparator('.');
	    otherSymbols.setGroupingSeparator(',');
	    ROUNDED_DOUBLE_DECIMALFORMAT = new DecimalFormat("####0.00", otherSymbols);
	    ROUNDED_DOUBLE_DECIMALFORMAT.setGroupingUsed(false);
	}


	public static double bytesToMiB( double bytes )
	{
		return bytes/MEGABYTE_FACTOR;
	}

	    public static String getTotalMemoryInMiB() {
	        double totalMiB = bytesToMiB(getTotalMemory());
	        return String.format("%s %s", ROUNDED_DOUBLE_DECIMALFORMAT.format(totalMiB), MIB);
	    }

	    public static String getFreeMemoryInMiB() {
	        double freeMiB = bytesToMiB(getFreeMemory());
	        return String.format("%s %s", ROUNDED_DOUBLE_DECIMALFORMAT.format(freeMiB), MIB);
	    }

	    public static String getUsedMemoryInMiB() {
	        double usedMiB = bytesToMiB(getUsedMemory());
	        return String.format("%s %s", ROUNDED_DOUBLE_DECIMALFORMAT.format(usedMiB), MIB);
	    }

	    public static String getMaxMemoryInMiB() {
	        double maxMiB = bytesToMiB(getMaxMemory());
	        return String.format("%s %s", ROUNDED_DOUBLE_DECIMALFORMAT.format(maxMiB), MIB);
	    }

	    public static double getPercentageUsed() {
	        return ((double) getUsedMemory() / getMaxMemory()) * 100;
	    }

	    public static String getPercentageUsedFormatted() {
	        double usedPercentage = getPercentageUsed();
	        return ROUNDED_DOUBLE_DECIMALFORMAT.format(usedPercentage) + "%";
	    }
	    
	    
	    public static long getMaxMemory() {
	        return Runtime.getRuntime().maxMemory();
	    }

	    public static long getUsedMemory() {
	        return getMaxMemory() - getFreeMemory();
	    }

	    public static long getTotalMemory() {
	        return Runtime.getRuntime().totalMemory();
	    }

	    public static long getFreeMemory() {
	        return Runtime.getRuntime().freeMemory();
	    }
}
