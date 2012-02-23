package org.monitis.google;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.monitis.api.monitor.CustomMonitor;
import org.monitis.beans.MonResult;
import org.monitis.beans.MonResultParameter;
import org.monitis.beans.MonitorParameter;
import org.monitis.beans.Response;
import org.monitis.enums.DataType;
import org.monitis.enums.OutputType;
import org.monitis.exception.MonitisException;


public class GoogleMonitor {
	
	//private monitor
	private CustomMonitor monitor;
	
	
	private static final String tag = "google analytics";
	
	//google parameters that appears in the custom monitor
	private static final String metrics = "ga:avgPageLoadTime,ga:visitors,ga:pageviews";
	private static final String[] params = {"loadtime", "visits", "pageviews", "enddate", "startdate"};
	
	private int search(String username,String accountname, String profilename) throws MonitisException {
		int id = -1;
		Response response = monitor.getMonitors(username + " " + tag, "custom", OutputType.JSON);
		try {
			JSONArray array = new JSONArray(new JSONTokener(response.getResponseText()));
			if(array.length() > 0) {
				for(int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					String name = obj.getString("name");
					if(name.equals(accountname + " " + profilename))
						id = Integer.parseInt(obj.getString("id"));
				}
			}
		} catch (JSONException e) {
			throw new MonitisException(e, e.getMessage());
		} 
		return id;
	}
	
	//return true if the monitor with username, accountname, profilename exists
	public boolean GoogleMonitorExists(String username,String accountname, String profilename) throws MonitisException {
		return search(username, accountname, profilename) != -1;
	}
	
	public GoogleMonitor(String apikey, String secretkey) {
		monitor = new CustomMonitor(apikey, secretkey);
	}
	
	//add a monitor on the dashboard
	public void addGoogleMonitor(String username, String password,String accountname, String profilename) throws MonitisException {
		List<MonitorParameter> monitorParameters = new ArrayList<MonitorParameter>();
		monitorParameters.add(new MonitorParameter("account", "account", accountname, DataType.STRING, false));
		monitorParameters.add(new MonitorParameter("profile", "profile", profilename, DataType.STRING, false));
		monitorParameters.add(new MonitorParameter("user", "user", username, DataType.STRING, false));
		List<MonResultParameter> monResultParameters = new ArrayList<MonResultParameter>();
		for(String param : params) {
			monResultParameters.add(new MonResultParameter(param, param, "", DataType.STRING));
		}
		Response response = monitor.addMonitor(null,accountname + " " + profilename, username + " " + tag, null, monitorParameters, monResultParameters,null);
		JSONObject obj; 
		try {
			obj = new JSONObject(new JSONTokener(response.getResponseText()));
			if(!obj.getString("status").equalsIgnoreCase("OK")) {
				
				throw new MonitisException(obj.getString("status"));
			}
		} catch (JSONException e) {
			throw new MonitisException(e, e.getMessage());
		} 
	}
	
	//delete a monitor on the dashboard
	public void deleteGoogleMonitor(String username,String accountname, String profilename) throws MonitisException {
		int id = search(username, accountname, profilename);
		if(id != -1)
			monitor.deleteMonitors(new Integer[] {id});
		else 
			throw new MonitisException(new IllegalArgumentException("the monitor doesn't exist"),null);
	}

	//add values on the custom monitor on the dashboard
	public void writeValues(String username, String password, String accountname, String profilename, String startdate, String enddate) throws MonitisException {
		int id = search(username, accountname, profilename);
		if(id == -1)
			throw new MonitisException(new IllegalArgumentException("the monitor doesn't exist"),null);
		AnalyticsClient client = new AnalyticsClient(username, password, accountname, profilename);
		String[] values =client.getValues(startdate, enddate, metrics);
		List<MonResult> results = new ArrayList<MonResult>();
		results.add(new MonResult(params[params.length - 1] , startdate));
		results.add(new MonResult(params[params.length - 2] , enddate));
		for(int i = 0; i < values.length; i++)
			results.add(new MonResult(params[i],values[i]));
		monitor.addResult(id, Long.valueOf(new Date().getTime()), results);
	}
	
	
}
