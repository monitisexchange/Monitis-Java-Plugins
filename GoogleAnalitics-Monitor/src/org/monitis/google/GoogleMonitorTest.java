package org.monitis.google;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.monitis.exception.MonitisException;

public class GoogleMonitorTest {
	
	public static void main(String[] args) throws Exception {
		String apikey = "T5BAQQ46JPTGR6EBLFE28OSSQ";			// ApiKey that can be obtained from your Monitis account 
		String secretkey = "248VUB2FA3DST8J31A9U6D9OHT";		// SecretKey that can be obtained from your Monitis account 
		String username = "monitis.kpi@gmail.com"; 				// google account user name
		String password = "p@55w0rd4monkpi"; 					// google account password
		String accountname = "mon.itor.us"; 					// google account name
		String profilename = "*.mon.itor.us (Master Profile)"; 	// google account profile name
		try {
			// create a google monitor
			GoogleMonitor monitor = new GoogleMonitor(apikey, secretkey);

			// search if the monitor exist otherwise create it
			boolean exist = monitor.GoogleMonitorExists(username, accountname, profilename);
			if (!exist)
				monitor.addGoogleMonitor(username, password, accountname, profilename);

			// examine the date period and write values to the dashboard
			monitor.writeValues(username, password, accountname, profilename, "2011-11-06", "2011-12-06");

			// delete the monitor
			monitor.deleteGoogleMonitor(username, accountname, profilename);

		} catch (MonitisException e){
			System.err.println ("MonitisException: "+e.getErrorMsg());
			e.printStackTrace();
		}
		
	}
}
