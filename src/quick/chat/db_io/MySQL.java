package quick.chat.db_io;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import com.globals.Globals;


public class MySQL 
{
	public static class TimedMySQLConnection
	{
		long 		time = System.currentTimeMillis();
		Connection 	conn = null;
				
		static public TimedMySQLConnection openTimedMySQLConnection( String db ) throws ClassNotFoundException, SQLException
		{
			String url = String.format("jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false&useUnicode=yes&characterEncoding=UTF-8", host, port, db );		
					
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			Connection conn = DriverManager.getConnection(url, user, pass);
			
			return new TimedMySQLConnection( conn );
		}
		
		public TimedMySQLConnection( Connection  conn )
		{
			this.conn = conn;
		}
				
		public boolean isTimeOut()
		{
			return (System.currentTimeMillis() - time) > (1000L*360L);// 10 Minutes
		}
		
		public Statement createStatement() throws SQLException
		{
			time = System.currentTimeMillis();
			
			return conn.createStatement();
		}

		public PreparedStatement prepareStatement(String query) throws SQLException
		{
			time = System.currentTimeMillis();
			
			return conn.prepareStatement( query );
		}

		public void close()
		{
			try
			{
				conn.close();
			} 
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			
			finally
			{
				conn = null;
			}	
		}
		
	}
	
	public static interface QueryCallBack
	{
		
		public boolean execute( ResultSet rs, int rowCount, int columnNumber, Object linkObj ) throws SQLException, IOException;

	}
	
	private static class TimeThread extends Thread
	{
		@Override
		public void run()
		{			
			for(;;)
			{
				try
				{
					Thread.sleep( 1000L*60L );
					
					Vector<TimedMySQLConnection> toClose = new Vector<TimedMySQLConnection>();

					synchronized ( connStack )
					{
						for ( TimedMySQLConnection c : connStack )
						{
							if ( c.isTimeOut() )
							{
								toClose.add(c);
							}
						}
						
						for ( TimedMySQLConnection c : toClose )
						{
							connStack.remove( c );
							
							c.close();
							
							connectionsCount--;
						}
					}
				} 
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}


	private static final Stack<TimedMySQLConnection> connStack = new Stack<TimedMySQLConnection>();
	
	private String lastError = null;

	private TimedMySQLConnection 	timedMySQLConnection;
	private String 					db;

	private static final String host 	= Globals.mysqlHost;
	private static final String port 	= "3306";
	private static final String user 	= Globals.mysqlUser;
	private static final String pass 	= Globals.mysqlPass;
	
	private static int connectionsCount = 0;
	
	private static 	TimeThread timeThread = null;
	
	static 
	{
		timeThread = new TimeThread();
		
		timeThread.setDaemon( true );
		
		timeThread.start();
	}
	
	public MySQL()
	{
		this.db = Globals.dataBase;
	}

	public boolean connect()
	{
		if ( timedMySQLConnection != null )
		{			
			return true;
		}

		while ( connStack.size() < 2 && connectionsCount > 490 )
		{
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{}
		}
		
		synchronized ( connStack )
		{
			if ( connStack.size() > 0 )
			{
				timedMySQLConnection = connStack.pop();
			}
			else
			{				
				try 
				{
					timedMySQLConnection = TimedMySQLConnection.openTimedMySQLConnection( db );
										
					connectionsCount++;
				} 
				catch (SQLException e) 
				{
					lastError = ( "SQLException: " + e );
				} 
				catch (ClassNotFoundException e) 
				{
					lastError = ( "SQLException: " + e );
				} 
			}
		}
		
		return timedMySQLConnection != null;
	}
	
	public void disconect()
	{
		if ( timedMySQLConnection != null )
		{
			synchronized ( connStack )
			{
				connStack.push( timedMySQLConnection );
				
				timedMySQLConnection = null;
			}
		}
	}

	public String getLastError()
	{
		return lastError;
	}

	public int executeCommand( String command )
	{
		if ( connect() == false )
			return -2;
		
		lastError = null;
		
		Statement 	stmt 			= null;
		int			returnValue 	= 0;
	
		try 
		{
		    stmt = timedMySQLConnection.createStatement();
		    
		    returnValue = stmt.executeUpdate( command );
		} 
		catch (SQLException e) 
		{
			returnValue = -1;
			lastError = e.getMessage();
			disconect();
		} 
		finally 
		{
		    // it is a good idea to release
		    // resources in a finally{} block
		    // in reverse-order of their creation
		    // if they are no-longer needed

		    if (stmt != null) 
		    {
		        try 
		        {
		            stmt.close();
		        } 
		        catch (SQLException sqlEx) {  }

		        stmt = null;
		    }
		    
		    disconect();
		}
		    
		return returnValue;
	}
	

	public long get_mysql_insert_id()
	{
		long lastID = -1;
		
		try
		{
			lastID = Long.parseLong( simpleQuery( "SELECT LAST_INSERT_ID();" ) );
		} 
		catch (NumberFormatException e)
		{
			lastError = e.getMessage();
		}
		
		return lastID;
	}
	
	@SuppressWarnings("resource")
	public String simpleQuery( String query )
	{
		if ( connect() == false )
			return null;
		
		Statement 	stmt 		= null;
		ResultSet 	rs 			= null;
		String		returnValue = null;
	
		try 
		{
		    stmt = timedMySQLConnection.createStatement();
		    
		    rs = stmt.executeQuery( query );

		    if ( rs != null ) 
		    {
		        rs = stmt.getResultSet();
		        	        		        
		        while (rs.next()) 
		        {
		        	if ( returnValue == null )
		        	{
		        		returnValue = rs.getString(1);
		        	}
		        }
		    }
		} 
		catch (SQLException e) 
		{
			lastError = e.getMessage();
		} 
		finally 
		{
		    // it is a good idea to release
		    // resources in a finally{} block
		    // in reverse-order of their creation
		    // if they are no-longer needed

		    if (rs != null) 
		    {
		        try 
		        {
		            rs.close();
		        } 
		        catch (SQLException sqlEx) {  }

		        rs = null;
		    }

		    if (stmt != null) 
		    {
		        try 
		        {
		            stmt.close();
		        } 
		        catch (SQLException sqlEx) {  }

		        stmt = null;
		    }
		    
		    disconect();
		}
		    
		return returnValue;
	}

	@SuppressWarnings("resource")
	public void callBackQuery( String query, Object linkObj, QueryCallBack queryCallBack )
	{
		if ( connect() == false )
			return;
		
		Statement 	stmt 		= null;
		ResultSet 	rs 			= null;
		
		try 
		{
		    stmt = timedMySQLConnection.createStatement();
		    
		    rs = stmt.executeQuery( query );

		    stmt.setFetchSize(1000);
		    
		    if ( rs != null ) 
		    {
		        rs = stmt.getResultSet();
		        
		        java.sql.ResultSetMetaData md = rs.getMetaData();
	        
		        int columnNumber = md.getColumnCount();
		        int rowCounter	= 0;
		        
		        while (rs.next()) 
		        {
		        	try 
		        	{
						if ( queryCallBack.execute( rs, rowCounter, columnNumber, linkObj ) == false )
						{
							break;
						}
					} 
		        	catch (IOException e) 
					{
		        		lastError = e.getMessage();
						break;
					}
		        }
		    }
		} 
		catch (SQLException e) 
		{
			lastError = e.getMessage();
			
			disconect();
		}
		
		finally 
		{
		    // it is a good idea to release
		    // resources in a finally{} block
		    // in reverse-order of their creation
		    // if they are no-longer needed

		    if (rs != null) 
		    {
		        try 
		        {
		            rs.close();
		        } catch (SQLException sqlEx) {  }

		        rs = null;
		    }

		    if (stmt != null) 
		    {
		        try 
		        {
		            stmt.close();
		        } catch (SQLException sqlEx) {  }

		        stmt = null;
		    }
		    
		    disconect();
		}
		    
	}

	public Vector<String> simpleVQuery( String query )
	{
		Vector<String> resultVect = new Vector<String>();
		
		simpleVQueryAdd( query, resultVect );
		
		return resultVect;
	}
	
	public String[] simpleAQuery( String query )
	{
		Vector<String> resultVect = new Vector<String>();
		
		simpleVQueryAdd( query, resultVect );
		
		String[] retArr = new String[resultVect.size()];
				
		return resultVect.toArray( retArr );
	}
	
	@SuppressWarnings("resource")
	public int simpleVQueryAdd( String query, Vector<String> resultVect )
	{
		int columnCount = 0;
		
		if ( connect() == false )
			return -1;
		
		Statement 	stmt 		= null;
		ResultSet 	rs 			= null;
	
		lastError = null;
		
		try 
		{
		    stmt = timedMySQLConnection.createStatement();
		    
		    rs = stmt.executeQuery( query );

		    if ( rs != null ) 
		    {
		        rs = stmt.getResultSet();
		        
		        java.sql.ResultSetMetaData md = rs.getMetaData();
	        
		        columnCount = md.getColumnCount();
		        
		        if ( resultVect != null )
		        {
			        while (rs.next()) 
			        {
		        		for ( int i = 1; i<=columnCount; i++)
			        	{
		        			String s = rs.getString(i);
		        			
		        			resultVect.add( s );
			        	}
			        }
		        }
		    }

		    // Now do something with the ResultSet ....
		} 
		catch (SQLException e) 
		{
			lastError = e.getMessage();
		} 
		
		finally 
		{
		    // it is a good idea to release
		    // resources in a finally{} block
		    // in reverse-order of their creation
		    // if they are no-longer needed

		    if (rs != null) 
		    {
		        try 
		        {
		            rs.close();
		        } catch (SQLException sqlEx) {  }

		        rs = null;
		    }

		    if (stmt != null) 
		    {
		        try 
		        {
		            stmt.close();
		        } catch (SQLException sqlEx) {  }

		        stmt = null;
		    }
		    
		    disconect();
		}
		    
		return columnCount;
	}

	public Vector<Map<String, String>> simpleHMapQuery( String query )
	{
		final  	Vector<Map<String, String>> resultVect 	= new  Vector<Map<String, String>>();
		
		this.callBackQuery(query, null, new QueryCallBack()
		{

			@Override
			public boolean execute(ResultSet rs, int rowCount, int columnNumber, Object linkObj) throws SQLException, IOException
			{
				ResultSetMetaData 			rsmd 		= rs.getMetaData();
				int 						numColumns 	= rsmd.getColumnCount();
				Map<String, String> 		resultMap 	= new HashMap<String, String>();
				
				for (int i=1; i<numColumns+1; i++) 
				{
				    String 	column_name = rsmd.getColumnName(i);
				    int 	column_type = rsmd.getColumnType(i);
				    	
				    switch ( column_type )
				    {
					    case java.sql.Types.CHAR:
					    {
					    	resultMap.put(column_name, rs.getString(column_name));
					    	
					    	break;
					    }
				        case java.sql.Types.ARRAY:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.BIGINT:
				        {
				        	resultMap.put(column_name, String.valueOf( rs.getInt(column_name) ));
				        	
				        	break;
				        }
				        case java.sql.Types.BOOLEAN:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.BLOB:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.DOUBLE:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.FLOAT:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.INTEGER:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.NVARCHAR:
				        {
				        	resultMap.put(column_name,  rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.VARCHAR:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.TINYINT:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.SMALLINT:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.DATE:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        case java.sql.Types.TIMESTAMP:
				        {				        	
				        	resultMap.put(column_name, rs.getString(column_name) );
				        	
				        	break;
				        }
				        default:
				        {
				        	resultMap.put(column_name, rs.getString(column_name) );
				        }
				    }
				}
				
				resultVect.add(resultMap);

				return true;
			}
		});
		
		
		return resultVect;
	}

	public PreparedStatement prepareStatement(String query) throws SQLException 
	{
		if ( connect() == false )
		{
			throw new SQLException(); 
		}
		
		return timedMySQLConnection.prepareStatement(query);
	}

	public static String getPassword()
	{
		return pass;
	}

	public static String geomFromCoordinates( double longitud, double latitud )
	{
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
		DecimalFormat format = new DecimalFormat("###.00000000",symbols);
		String sLongitud 	= format.format( longitud );
		String sLatitud 	= format.format( latitud );
		String geolocation	= "ST_GeomFromText('POINT(" + sLongitud + " " + sLatitud + ")')";
		return geolocation;
	}

	public static int countConections()
	{
		return connectionsCount;
	}
}
