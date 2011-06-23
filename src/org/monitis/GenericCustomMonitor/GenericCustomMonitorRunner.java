package org.monitis.GenericCustomMonitor;

import org.json.JSONObject;
import org.json.XML;
import org.monitis.api.monitor.Agent;
import org.monitis.api.monitor.CustomMonitor;
import org.monitis.beans.Response;
import org.monitis.enums.OutputType;
import org.monitis.utils.TimeUtility;

/**
 * This class is responsible for adding of a new custom monitor to the existing
 * Monitis monitors scope (if it doesn't yet exist), configure and run it. 
 * The necessary parameters and values should be provided by user class 
 * that extends the IGenericCustomMonitor abstract class.
 */
public class GenericCustomMonitorRunner  extends Thread {

	// some constants
    private static final String monitor_tag_key = "tag";
    private static final String monitor_id_key = "id";
    
	private IGenericCustomMonitor custom_monitor;
	private long processing_time = 60000;
    private long test_duration = 600000;//10 min
    private boolean delete_monitor = false;
	private String token = null;
    private boolean stop = false;
    private Integer monitorId = null;// monitor ID
    private CustomMonitor cm = null;
	private Response resp = null;
	
	public GenericCustomMonitorRunner(IGenericCustomMonitor custom_monitor) throws Exception {
		if (custom_monitor != null && custom_monitor instanceof IGenericCustomMonitor) {
			this.custom_monitor = custom_monitor;
		} else {
			throw new Exception("Custom monitor instance isn't specified");
		}
		runMonitor();
	}
	
    @Override
    public void run() {
    	
		while (!stop) {
			// add next result
			Long now = TimeUtility.getNowByGMT().getTime().getTime();
			try {
				resp = cm.addResult(monitorId, now, custom_monitor.get_results());
				System.out.println("addResult() - " + resp.getResponseText());
			} catch (Exception e) {
				System.err.println("Monitis Exception: " + e.getMessage());
				stop = true;
			}
			try { // processing imitation
				Thread.sleep(processing_time);
			} catch (InterruptedException ex) {// nothing to do
				Thread.yield();
			}
		}
	}

    /**
     * The recursive function which searches for a value of a key 
     * in a complex JSON object and that contain also tag_value for tag_key.
     * 
     * E.g for JSONobject {"monitors":{"monitor":{"id":"456","tag":"Custom_monitor","name":"My_Custom_monitor"}}}
     * and tag_key="tag" with tag_value="Custom_monitor" and key="id", it will return "456"
     * 
     * @param json - complex JSONobject with multiply nested JSON objects
     * @param tag_key - tag key in an embedded JSON object
     * @param tag_value - the corresponding tag key value 
     * @param key - searching key in same embedded JSON object.
     * @return - value for the key (or null if couldn't find anything)
     */
    private Object getTagId(JSONObject json, String tag_key, String tag_value, String key){
    	Object value = null;
		while (json != null && json.length() > 0 && value == null) {
//			System.out.println("toJSONObject = "+ json);
			String _tag_value = json.optString(tag_key);
			value = json.opt(key);
//			System.out.println("tag = "+_tag_value+"\tkey = "+value);
			if (_tag_value != null && tag_value.equalsIgnoreCase(tag_value) && value != null) break;
			java.util.Iterator<String> keys = json.keys();
			String k = null;
			while (keys.hasNext()) {
				k = keys.next();
//				System.out.println(k);
				try {
					value = getTagId(json.getJSONObject(k), tag_key, tag_value, key);
				} catch (Exception e) {
					value = null;
					return value;
				}
				if (value != null) break;
			}
		}
		return value;
    }

    private void runMonitor() {
		String apiKey = custom_monitor.get_apiKey();
		String secretKey = custom_monitor.get_secretKey();
		
	    Agent agent = null;
		int id = 0;
		
		try {
			// registering and get token
			agent = new Agent(apiKey, secretKey);
			resp = agent.getToken();
			System.out.println("getToken()\n"+resp.getResponseText());
			token = new JSONObject(resp.getResponseText()).getString("authToken");
			System.out.println("New authToken = "+token);
			if (token == null || token.length() <= 0) {
				System.err.println("Couldn't obtain authToken");
				System.exit(0);
			}
						
			// build Custom Monitor
			cm = new CustomMonitor(apiKey, secretKey);
//			resp = cm.getMonitors(monitor_tag_value, OutputType.JSON);//Internal Server ERROR!!!
			String monitor_tag_value = custom_monitor.get_monitor_tag_value();
			resp = cm.getMonitors(monitor_tag_value, OutputType.XML);
			if (resp != null && !resp.getResponseText().isEmpty()) {
				System.out.println("getMonitors("+monitor_tag_value+",...) - \n"+resp.getResponseText());
				JSONObject json = XML.toJSONObject(resp.getResponseText());
				Object obj = getTagId(json, monitor_tag_key, monitor_tag_value, monitor_id_key);
				if (obj != null && obj instanceof String) {
					id = Integer.valueOf((String)obj).intValue();
					System.out.println("Existing Monitor id = "+id);
				}
			}
			if (id <= 0) {
				System.out.println("Couldn't obtain existing monitor - creating a new one.");
				//add custom monitor
				resp = cm.addMonitor(custom_monitor.get_monitor_name(), monitor_tag_value, custom_monitor.get_monitorParams(), custom_monitor.get_resultParams(), null);
				System.out.println("addMonitor() - "+ resp.getResponseText());
				JSONObject obj = new JSONObject(resp.getResponseText());
				id = obj.getInt("data");
				System.out.println("New monitorId="+id);
			}
			
			if (id > 0) {
				monitorId = Integer.valueOf(id);
		    	delete_monitor = custom_monitor.deleteMonitor();
		    	long duration = custom_monitor.get_processingTime();
		    	if (duration > processing_time) processing_time = duration;
		    	duration = custom_monitor.get_testDuration();
		    	if (duration <= 0) {
		    		/*
		    		  In case if test duration is set less or equal 0 then 
		    		  test should run infinitely 
		    		*/
		    		test_duration = Long.MAX_VALUE;//infinite duration
		    	} else if (duration > processing_time) {
		    		/*
		    		  Test duration cannot be less than 
		    		  duration interval between sending monitoring result. 
		    		  If so, leave the default test duration 
		    		*/
		    		test_duration = duration;
		    	}
				
				this.start();//start the thread
				
				Thread.sleep(test_duration);
				
				stop = true;
				System.out.println("Test is over!!! Added values: "+ test_duration/processing_time);
				if (delete_monitor) {
					 //delete monitor
					 resp = cm.deleteMonitor(monitorId);
					 System.out.println("deleteMonitor() - " + resp.getResponseText());
				}
				custom_monitor.signal_testEnded(0);
			}
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}
    	
    }
    
}
