package demo;
/*
 * 
 * This package includes the code to do the following while testing Vampire: 
 * 
 * 1) Get the current video description from the DB, for a given video reference ID
 * 
 * 2) Remotely connect to a QA instance and execute Vampire
 * 
 * 3) For the same reference ID used in 1), gets the updated video description and compares it to the 
 * video description from 1). If the description was updated, restores the description to its original
 * content in order to re-use it next time the test is executed.
 * 
 * As a POC, this test case used a hardcoded data provider with reference ids present in both the 
 * available manifest files and the QA environment DB. Currently the hardcoded data provider is 
 * commented out the code and the code has been updated to use the DiscoveryDataProvider class to read
 * the reference ids information from an excel file. 
 * 
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;



//import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

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


public class Vampire_Update extends SeleneseTestBase
{	
	String original = "";
	String updated = "";
	String network= "";
	static int firstRun = 0;
	static Session session;
	
	/*@DataProvider(name = "myTest")
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
	}*/
	
	@DataProvider(name = "myTest")
	public  Object[][] createData1() throws Exception 
	{
        DiscoveryDataProvider dp = new DiscoveryDataProvider();
     // String path = "MetricsInput" + File.separator +"EventsToValidate.xls";
        //String path = "C:\\Users\\Eduardo\\workspace-kepler\\BasicDemo\\src\\DP_Vampire.xls";
        Object[][] retObjArr = dp.ReadDatafrmExcel("ids", "refID", "C:\\Users\\Eduardo\\workspace-kepler\\BasicDemo\\src\\DP_Vampire.xls");
        return (retObjArr);
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
             System.out.println();
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
    
          // Set video reference ID for the DB Query
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
    public void remote(String network) 
 	{
 		//Session session = null;
 		Channel channel=null;
 		// Commands to be executed, separated by semi colons
 		String command = "/http/versions-available/vampire/vampire-182/lib/Vampire/runVampire.php --log-success --log-skips --verbose "+ network;//DSC";
 		
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
           
           // Get connection to DB
           Connection con = DriverManager.getConnection(url1, "root","");
           
          // Create Select Statement
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
      //  updated = "This is a test description";        
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
     public void Test(String refID, String network) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
     {
    	 QueryDB(refID);
    	 remote(network);
    	 QueryUpdateDB(refID);
     }
}