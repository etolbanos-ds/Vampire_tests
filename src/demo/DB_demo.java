package demo;
/*
 * 
 * 
 * This package includes the code to do the following while testing Vampire: 
 * 1) Updates the "modified_by_etb" timestamp for a given "dws_video_id" with the current system timestamp.
 * 		The same video should be modified in the manifest.xml file and uploaded to the Vampire ftp.
 * 
 * 2) Remotely executes Unix commands, to run Vampire for a given QA environment 
 * 
 * 3) Once Vampire finishes its execution, a query to the DB is executed for the "dws_video_id" updated on 1), to 
 * obtain it's timestamp and compare it with the timestamp from 1)	
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import com.mysql.jdbc.Statement;
import com.thoughtworks.selenium.SeleneseTestBase;

import org.openqa.selenium.WebDriver;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


public class DB_demo extends SeleneseTestBase
{	
	String original = "";
	String updated = "";
//	String refID = "5223244ccce91f4c67e4052e5eade47d000d8ea2" ;
	static int firstRun = 0;
	//int videoId = 57525;
	//int videoId = 147116; // Video ID from file tlc-manifest_orig.xml
	 
	static Session session;
	
	@DataProvider(name = "myTest")
	  public Object[][] createData1() 
	{
		return new Object[][] 
	    {
				{ "0013919ef52698a75bd4e729bf14212c6d4401fb"},
	            { "29ac18610198a1062ed06baf5cc7a103eeffabcf"},
	            { "d1e6e7c4862267937f11e78be32870d5cfcf7714"},
	            { "0011a601480fbb078cefcf146ef74cb9eaed655b"},
	            { "10878182"},
	            { "08ab6615df962a7236cdde1c5698e2319a9f1f44"},
	             
	    };
	}
	
     @BeforeTest

     public void setUp() throws Exception
     {     }
               
     
     public static void go()
     {
       	 String user = "etolbanos-ds";
         String password = "BZU9KRpd";
         String host = "qa-017.dp.discovery.com";
         int port=22;
         int lport, rport;
         String rhost;
         try
         {
             JSch jsch = new JSch();
             session = jsch.getSession(user, host, port);
             lport = 3306;
             rhost = "localhost";
             rport = 3306;
             session.setPassword(password);
             session.setConfig("StrictHostKeyChecking", "no");
             System.out.println("Establishing Connection...");
             session.connect();
             int assinged_port=session.setPortForwardingL(lport, rhost, rport);
             firstRun=1;
             System.out.println("localhost:"+assinged_port+" -> "+rhost+":"+rport);
         }
         catch(Exception e)
         {
        	 System.err.print(e);
         }
     }
     
     public void RestoreDB(String refID) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
     {
    	 //Prepare connection
       	 String url1 ="jdbc:mysql://localhost:3306/devcontent";
         
    	 // Load Microsoft SQL Server JDBC driver
         String dbClass = "com.mysql.jdbc.Driver";
         Class.forName(dbClass).newInstance();
           
         //Get connection to DB
           
         Connection con = DriverManager.getConnection(url1, "root","");
           
         //Create Update Statement
         PreparedStatement stmt = con.prepareStatement("UPDATE `dws_video` SET `description`= ? WHERE `reference_id`= ?");
    
           // Set original description and reference ID for the DB Update to restore the DB to it's original state
           stmt.setString(1, original);
           stmt.setString(2, refID);
           
           // Execute the Update
           stmt.executeUpdate();
           stmt.close();
           System.out.println("Video description has been successfully restored");
           System.out.println();
     }
     
     
   //  @Test(priority=1)
     
     public void QueryDB(String refID) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
     {
    	 // Query: SELECT * FROM `dws_video` WHERE `network_id`='tlc' AND `mp4_path`='/digmed/hdnet/e9/23/13740901401197_SS303_AnatomyPain'
    	    //SELECT * FROM devcontent.dws_video where reference_id = 'ffff18610198a1062ed06baf5cc7a103eeffabcf';
    	// refid="004d23ad9b04f7c9c6f9147c5c44fde5025ef371"	
    	 
    	 if (firstRun==0)
    	 {
    		 try
    	   	 {
    			 go();
    	   	 } 
    		 catch(Exception ex)
    		 {
    			 ex.printStackTrace();
    		 }
    	 } 
    	 //Prepare connection
       	 String url1 ="jdbc:mysql://localhost:3306/devcontent";
         
    	 // Load Microsoft SQL Server JDBC driver
         String dbClass = "com.mysql.jdbc.Driver";
         Class.forName(dbClass).newInstance();
           
         //Get connection to DB
           
         Connection con = DriverManager.getConnection(url1, "root","");
           
           //Create Update Statement
         PreparedStatement stmt = con.prepareStatement("SELECT `description` FROM `dws_video` WHERE `reference_id`= ?");
    
           // Set TimeStamp and video ID for the DB Update
          stmt.setString(1, refID);
          
           
           // Execute the Query
           ResultSet result = (ResultSet) stmt.executeQuery();
           while(result.next())
                   	   original=result.getString("description");
           System.out.println(original);
           //stmt.close();
           System.out.println();
     }
     
     
 	//@Test(priority=2) 
    public void remote() 
 	{
 		//Session session = null;
 		Channel channel=null;
 		
 		// Commands to be executed, separated by semi colons
 		// String command = "cd /; ls -l";
 		String command = "/http/versions-available/vampire/vampire-182/lib/Vampire/runVampire.php --log-success --log-skips --verbose DSC";
 		StringBuilder outputBuffer = new StringBuilder();
  
 		try 
 		{
  			// exec 'scp -f rfile' remotely
 			channel = session.openChannel("exec");
 			((ChannelExec)channel).setCommand(command);
 			channel.connect();
 			OutputStream out = channel.getOutputStream();
 			InputStream commandOutput  = channel.getInputStream();
 			int readByte = commandOutput.read();
 			while(readByte != 0xffffffff)
 			{
 				outputBuffer.append((char)readByte);
 				readByte = commandOutput.read();
 			}
 			System.out.println(outputBuffer.toString());
 			System.out.println();
 			//	channel.disconnect();
 		//	session.disconnect();
  
 		} 
 		catch (JSchException jsche) 
 		{
 			System.err.println(jsche.getLocalizedMessage());
 		} 
 		catch (IOException ioe) 
 		{
 			System.err.println(ioe.getLocalizedMessage());
 		} 
 /*		finally 
 		{
 			channel.disconnect();
 		}
  */
 	} 
    
     
    // @Test(priority=3)
     public void QueryUpdateDB(String refID) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
     {
       	 //Prepare connection
       	 String url1 ="jdbc:mysql://localhost:3306/devcontent";
         
    	 // Load Microsoft SQL Server JDBC driver
           String dbClass = "com.mysql.jdbc.Driver";
           Class.forName(dbClass).newInstance();
           
           //Get connection to DB
           
           Connection con = DriverManager.getConnection(url1, "root","");
           
         //Create Select Statement
           PreparedStatement stmt = con.prepareStatement("SELECT `description` FROM `dws_video` WHERE `reference_id`= ?");
        		   
           // Set video ID for the DB Query
           stmt.setString(1, refID);
           
           // Execute the Query
           ResultSet result = (ResultSet) stmt.executeQuery();
           
        //   stmt.close();
  
           System.out.println();
        	   //Retrieve by column name
           while(result.next())
                      updated = result.getString("description");
                
           System.out.println(updated);
       		System.out.println();
        	  
       		if (original.equals(updated))
        		   System.out.println("Vampire description update failed.");
        	   else
        	   {
        		   System.out.println("Vampire description update successful.");
        		   RestoreDB(refID);
        	   }
       		System.out.println();
     }

     
     @Test(dataProvider = "myTest")
     public void Test(String refID) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
     {
    	 QueryDB(refID);
    	 remote();
    	 QueryUpdateDB(refID);
     }

}