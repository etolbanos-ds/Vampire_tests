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
	String refID = "5223244ccce91f4c67e4052e5eade47d000d8ea2" ;
	int videoId = 57525;
	//int videoId = 147116; // Video ID from file tlc-manifest_orig.xml
	 
	static Session session;
	
	 String url ="";
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
             System.out.println("localhost:"+assinged_port+" -> "+rhost+":"+rport);
         }
         catch(Exception e)
         {
        	 System.err.print(e);
         }
     }
     
     public void RestoreDB() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
     {
    	/* try
    	 {
             go();
         } 
    	 catch(Exception ex)
    	 {
             ex.printStackTrace();
         }
    	 */
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
     
     
     @Test(priority=1)
     
     public void QueryDB() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
     {
    	 // Query: SELECT * FROM `dws_video` WHERE `network_id`='tlc' AND `mp4_path`='/digmed/hdnet/e9/23/13740901401197_SS303_AnatomyPain'
    	    //SELECT * FROM devcontent.dws_video where reference_id = 'ffff18610198a1062ed06baf5cc7a103eeffabcf';
    	// refid="004d23ad9b04f7c9c6f9147c5c44fde5025ef371"	
    	 
    	 try
    	 {
             go();
         } 
    	 catch(Exception ex)
    	 {
             ex.printStackTrace();
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
     
     
 	@Test(priority=2) 
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
 	//		go();
  		
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
    
     
     @Test(priority=3)
     public void QueryUpdateDB() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
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
        		   RestoreDB();
        	   }
       		System.out.println();
      
     }
 
}