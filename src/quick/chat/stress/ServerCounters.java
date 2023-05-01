package quick.chat.stress;

public class ServerCounters
{
	private static ServerCounters self = new ServerCounters();
	
	private long 	startTime 		= System.currentTimeMillis();
	private int 	outCount		= 0;
	private int 	inCount			= 0;
	private int 	letencyAccum	= 0;
	private int 	latencyCount	= 1;
	private int		inErrors		= 0;
	private int		inThreads		= 0;
	private int		outErrors		= 0;
	private int		outRetries		= 0;
	private int		outThreads		= 0;
	private String 	lastError = "";
	
	private	Object	letencyAccum_synchro = new Object();
	
	public static ServerCounters getSingleton()
	{
		return self;
	}
	
	private void clear()
	{
		startTime 		= 0;
		outCount 		= 0;
		inCount 		= 0;
		letencyAccum 	= 0;
		latencyCount 	= 1;
		inErrors		= 0;
		outErrors		= 0;
		outRetries		= 0;
		lastError		= "";
		Runtime.getRuntime().gc();
	}

	public void start()
	{
		startTime 		= System.currentTimeMillis();
		outCount 		= 0;
		inCount 		= 0;
		letencyAccum 	= 0;
		latencyCount 	= 1;
		inErrors		= 0;
		outErrors		= 0;
		outRetries		= 0;
		lastError		= "";
	}
	
	public int calcElepsedTime()
	{
		return (int)Math.round( (System.currentTimeMillis()-startTime)/1000 );
	}
	
	public int calcLatency()
	{
		int result = letencyAccum/latencyCount;
		
		synchronized ( letencyAccum_synchro )
		{
			letencyAccum = 0;
			latencyCount = 1;
		}
		
		return result;
	}
	
	int 	lastCalcInputSpeedCount 	= 0;
	long 	lastCalcInputSpeedTime 		= 0;
	
	public int calcInputSpeed()
	{
		if ( lastCalcInputSpeedCount > 0 )
		{
			long mSeg = 1 + System.currentTimeMillis()-lastCalcInputSpeedTime;

			int retValue = (int)Math.round((1000*(inCount-lastCalcInputSpeedCount))/mSeg);
			
			lastCalcInputSpeedCount = inCount;
			lastCalcInputSpeedTime = System.currentTimeMillis();
			
			return retValue;
		}
		else
		{		
			lastCalcInputSpeedCount = inCount;
			lastCalcInputSpeedTime = System.currentTimeMillis();

			return 0;
		}
	}
	
	int 	lastCalcOutputSpeedCount 	= 0;
	long 	lastCalcOutputSpeedTime 	= 0;

	public int calcOutputSpeed()
	{
		if ( lastCalcOutputSpeedCount > 0 )
		{
			long mSeg = 1 + System.currentTimeMillis()-lastCalcOutputSpeedTime;

			int retValue = (int)Math.round((1000*(outCount-lastCalcOutputSpeedCount))/mSeg);;
			
			lastCalcOutputSpeedCount = outCount;
			lastCalcOutputSpeedTime = System.currentTimeMillis();
			
			return retValue;
		}
		else
		{	
			lastCalcOutputSpeedCount = outCount;
			lastCalcOutputSpeedTime = System.currentTimeMillis();

			return 0;
		}
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime()
	{
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @return the outCount
	 */
	public int getOutCount()
	{
		return outCount;
	}

	/**
	 * @param outCount the outCount to set
	 */
	synchronized public void addOutCount()
	{
		this.outCount++;
	}

	/**
	 * @return the inCount
	 */
	public int getInCount()
	{
		return inCount;
	}

	/**
	 */
	synchronized public void addInCount()
	{
		this.inCount++;
	}

	/**
	 * @param letencyAccum the letencyAccum to set
	 */
	void addLetencyAccum(long letencyAccum)
	{
		synchronized ( letencyAccum_synchro )
		{
			this.letencyAccum += letencyAccum;
			
			this.latencyCount++;
		}
	}

	/**
	 * @return the inErrors
	 */
	public int getInErrors()
	{
		return inErrors;
	}

	/**
	 * @param inErrors the inErrors to set
	 */
	synchronized public void addInErrors( String error )
	{
		this.inErrors++;
		
		this.lastError = error;
	}
	
	/**
	 * @param inErrors the inErrors to set
	 */
	synchronized public void addOutErrors( String error )
	{
		this.outErrors++;
		
		this.lastError = error;
	}


	/**
	 * @return the inThreads
	 */
	public int getInThreads()
	{
		return inThreads;
	}

	
	synchronized public void addInThreads()
	{
		this.inThreads++;
	}

	synchronized public void removeInThreads()
	{
		this.inThreads--;
	}

	/**
	 * @return the outErrors
	 */
	public int getOutErrors()
	{
		return outErrors;
	}


	/**
	 * @return the outThreads
	 */
	public int getOutThreads()
	{
		return outThreads;
	}

	/**
	 * @param outThreads the outThreads to set
	 */
	synchronized public void addOutThreads()
	{
		this.outThreads++;
	}
	
	synchronized public void removeOutThreads()
	{
		this.outThreads--;
		
		if ( this.outThreads == 0 )
		{
			this.clear();
		}

	}


	/**
	 * @return the lastError
	 */
	public String getLastError()
	{
		return lastError;
	}


	/**
	 * @return the outRetries
	 */
	public int getOutRetries()
	{
		return outRetries;
	}

	/**
	 * @param outRetries the outRetries to set
	 */
	synchronized public void addOutRetries()
	{
		this.outRetries++;
	}
	
	
}
