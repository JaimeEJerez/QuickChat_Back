package quick.chat.stress;

import java.lang.management.ManagementFactory;

public class CpuStats
{
   private final long threadId;
   private long lastCpuTime = 0;
   private long lastPoll = 0;
   private double usageAverage = 0;

   private double[] usageArr = new double[50];
   /**
    * Creates a CpuStats object for a single thread.
    * @param threadId The id of the thread to monitor
    * 
    */
   public CpuStats (long threadId)
   {
      this.threadId = threadId;
      lastCpuTime = getTotalTime ();
      lastPoll = System.nanoTime ();
      startCPUAutoSample();
   }

   Thread  cupUsageSampleThread = null;
   
   public void  startCPUAutoSample()
   {
      for ( int i=0; i<usageArr.length; i++ )
      {
    	  usageArr[i] = getUsage();
      }
      
      if ( cupUsageSampleThread == null )
      {
    	  cupUsageSampleThread = new Thread()
		  {
    		  public void run()
    		  {
    			  try
    			  {
	    			  for(;;)
	    			  {
		    			  double usage 		= getUsage();
		    			  double usageSum 	= 0;
		    			      			  
		    			  for ( int i=0; i<usageArr.length-1; i++ )
		    		      {
		    		    	  usageArr[i] = usageArr[i+1];
		    		    	  usageSum += usageArr[i];
		    		      }
		    			  
		    			  usageArr[usageArr.length-1] = usage;
		    			  usageSum += usage;
		    			  
		    			  synchronized ( this )
		    			  {
		    				  usageAverage = (usageSum/(double)(2.0*usageArr.length));
		    			  }
		    			  
		    			  Thread.sleep( 100 );
	    			  }
    			  }
    			  catch (InterruptedException e)
    			  {
    				  e.printStackTrace();
    			  }
    		  }
		  };
		  
		  cupUsageSampleThread.start();

      }
   }
   
   public double getCupUsageAverage()
   {
	  synchronized ( this )
	  {
		  return usageAverage;
	  }
   }
   /**
    * Creates a CpuStatus object for all threads. The supplied statistics affect
    * all threads in the current VM.
    */
   public CpuStats ()
   {
      threadId = -1;
      lastCpuTime = getTotalTime ();
      lastPoll = System.nanoTime ();
      startCPUAutoSample();
   }

   private long getRelativeTime ()
   {
      long currentCpuTime = getTotalTime ();
      long ret = currentCpuTime - lastCpuTime;
      lastCpuTime = currentCpuTime;

      return ret;
   }

   public double getUsage ()
   {
      long timeBefore = this.lastPoll;

      lastPoll = System.nanoTime ();
      long relTime = getRelativeTime ();

      return Math.max ((double)relTime / (double)(lastPoll - timeBefore), 0.0);
   }

   private long getTotalTime ()
   {
      if (threadId == -1)
      {
         long cpuTime = 0;
         for (long id : ManagementFactory.getThreadMXBean ().getAllThreadIds ())
         {
            cpuTime += ManagementFactory.getThreadMXBean ().getThreadCpuTime (id);
         }

         return cpuTime;
      }
      else
      {
         return ManagementFactory.getThreadMXBean ().getThreadCpuTime (threadId);
      }
   }
}