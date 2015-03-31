package org.monitis.monitor.jmx;

import java.io.File;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.monitis.GenericCustomMonitor.GenericCustomMonitorRunner;
import org.monitis.GenericCustomMonitor.IGenericCustomMonitorWrapper;
import org.monitis.beans.MonResult;
import org.monitis.util.Utils;
import org.monitis.utils.MConfig;

/**
 * The JMXMonitor represents the implementation of a standalone JMX client which allows to get Mbeans attributes values 
 * from any Java application and send them into Monitis via the Monitis open API. 
 * This project uses the Java Management Extensions possibilities and the Monitis custom monitor approach for providing  
 * of a simple solution for users who wants to monitor any Java application.
 * <p>
 * The JMX agent should be enabled on the monitored application JVM and be implemented on J2SE version 5 or higher.
 * </p>
  
 * <p>
 * The monitored application should be started by using the following parameters <br>
 * To do so usually you should add some additional keys when start application:<br>
 * -Dcom.sun.management.jmxremote.port=<concrete port|0>  0 mean that system should choose the first free port automatically<br> 
 * -Dcom.sun.management.jmxremote.authenticate=<"true"|"false"> <br>
 * -Dcom.sun.management.jmxremote=<"true"|"false"> <br>
 * -Dcom.sun.management.jmxremote.ssl=<"true"|"false"> <br>
 * -Dcom.sun.management.jmxremote.local.only=<"true"|"false"> <br>
 * </p>
 */
public class JMXMonitor extends IGenericCustomMonitorWrapper {
	private static String confFile = "/properties/monitor.config";
	private JMXClient client = null;
	private static String java_command = null;
	private JMXServiceURL serviceUrl;
	private MBeanServerConnection connection = null;

	private String host = "localhost";
	private int port = 0;
	private int jmxport = 0;
	private String username = null;
	private String password = null;
	private ArrayList<JSONObject> params = new ArrayList<JSONObject>();
	private List<MonResult> prev_results = new ArrayList<MonResult>();
	// stores results in the file only when debug_on is true and debug_file is specified 
	private String debug_file = null;
	private boolean debug_on = false;
	private long duration = 0;
	private long test_duration = 0;
	private Logger logger;
	
	public JMXMonitor() throws Exception {
		super();
		monitor_init();
	}

	/**
	 * Initializing of Monitor object
	 * @throws Exception
	 */
	private void monitor_init() throws Exception {
		MConfig conf = MConfig.getConfig(confFile);
		if (conf == null) {
			throw new Exception("Couldn't load properties.");
		}
		debug_file = conf.getConfigStringValue("debug.file");
  		debug_on = conf.getConfigBooleanValue("debug.turn_on");
  		duration = conf.getConfigIntValue("monitor.processingTime", 1) * 60000l;
  		test_duration = conf.getConfigIntValue("monitor.testDuration", (int)duration*10) * 60000l;
  		
		java_command = conf.getConfigStringValue("monitor.app_command", "TestServer");
		host = conf.getConfigStringValue("monitor.host", "localhost");
		port = conf.getConfigIntValue("monitor.port", 0);
		jmxport = conf.getConfigIntValue("monitor.jmxport", 0);
		username = conf.getConfigStringValue("monitor.username");
		password = conf.getConfigStringValue("monitor.password");

		//Also the Log4j configuration file should be there.
		String path = new File(conf.getPropFile()).getParent().concat("/log4j.xml");
		if (new File(path).canRead()){
			DOMConfigurator.configure(path);
		}
		logger = Logger.getLogger(JMXMonitor.class);

		ArrayList<String> arr = conf.getConfigStringAsArray("monitor.result_params");
		if (arr == null) {
			logger.fatal("No data for 'monitor.result_params'");
			throw new Exception("No data for 'monitor.result_params'");
		} else {
			logger.info("'monitor.result_params' aray contain " + arr.size() + " elements");
			for (int i = 0; i < arr.size(); i++) {
				String prm = arr.get(i);
				if (port > 0){
					prm = prm.replaceAll("XXXX", String.valueOf(port));
				}
				logger.info(prm);
				try {
					params.add(new JSONObject(prm));
				} catch (Exception ex) {/* ignore */
				}
			}
			if (params.size() <= 0) {
				logger.fatal("No data in 'monitor.result_params'");
				throw new Exception("No data in 'monitor.result_params'");
			}
		}
//		connect();
	}

	public void connect() throws Exception{
        client = new JMXClient();

		if (java_command != null) {
			String JMXConnectionAddress = client.getJMXlocalConnectorAddress(java_command);
			if (JMXConnectionAddress != null && JMXConnectionAddress.length() > 0) {
				serviceUrl = new JMXServiceURL(JMXConnectionAddress);
			} else {
				logger.error("Couldn't find the JMXConnectionAddress for " + java_command);
			}
		}
		if (serviceUrl == null && host != null && host.length() > 0 && jmxport != 0) {
			serviceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + jmxport + "/jmxrmi");
		}
		logger.info("serviceURL = " + serviceUrl);
		if (serviceUrl == null) {
			logger.fatal("Couldn't connect to required object by JMX. ");
			throw new Exception("Couldn't connect to required object by JMX. ");
		}
		try {
			connection = client.openConnection(serviceUrl, username, password);
		} catch (IOException ex){
			logger.fatal("Could not establish connection..."+ex.toString());
			throw new Exception("Could not establish connection..."+ex.toString());
		}
	}

	/**
	 * Queries for JMX object attribute value
	 * @param connection
	 * @param jmxObject
	 * @param attr
	 * @param key
	 * @return query result
	 */
	private Object query(MBeanServerConnection connection, String jmxObject, String attr, String key) {
		Object ret = null;
		try {
			ret = client.query(connection, jmxObject, attr, key);
		} catch (Exception ex) {/*Ignore*/}
		return ret;
	}

	/**
	 * Composes result object for required metric
	 * 
	 * @param connection
	 * @param json the object that represent measured metric
	 *      - "calculate":"_The calculation style of metric_"
        - 0 - do nothing and put the measured value in result set as it
        - 1 - calculate absolute difference between current and previous results
        - 2 - calculate rate of changes between current and previous results per sec
        - 3 - calculate percentage of difference between current and previous results
        - 4 - tranform to time format (days-hours.min.sec)
		- 5 - convert ns to sec
		- 6 - convert ms to sec
		- 7	- convert byte to kb
		- 8 - convert byte to mb
		- 9 - convert to percent (multiply by 100)
	 * @return MonResult object (null on FAIL) 
	 */
	private MonResult getResult(MBeanServerConnection connection, JSONObject json) {
		MonResult ret = null;

		if (connection != null && json != null) {
			String jmxObject = json.optString("jmxObject");
			String attribute = json.optString("attribute");
			String key = json.optString("key");
			String[] format = splitFormat(json.optString("format"), ":");
			JSONArray jarray = 	new JSONArray();
			Object calc = json.opt("calculate");
			if (calc != null){
				if (calc instanceof JSONArray){
					jarray = (JSONArray)calc;
				} else if (calc instanceof Integer){
					jarray.put((Integer)calc);
				} else {
					jarray.put(0);
				}
			}
			String name = format[0];
			if (jmxObject != null && attribute != null && format != null) {
				Object obj = query(connection, jmxObject, attribute, key);
				String value = String.valueOf(obj!= null?obj:0);
				Double prev_value = 0.0;
				if (prev_results.size() >= params.size()) {
					ListIterator<MonResult> iter = prev_results.listIterator();
					while (iter.hasNext()) {
						MonResult res = iter.next();
						if (res.getParamName().equalsIgnoreCase(name)) {
							prev_value = Double.valueOf(res.getParamValue());
							iter.set(new MonResult(name, value));
						}
					}
				} else {
					prev_results.add(new MonResult(name, value));
				}
				int len = jarray.length();
				for (int i = 0; i < len; i++){//browse through the calculate array
					int calculate = jarray.optInt(i, 0);
					switch (calculate) {
					case 0:// nothing to do
						break;
					case 1:// calculate difference
						try {
							value = String.valueOf(Double.valueOf(value) - prev_value);
						} catch (Exception e) {/* ignore */	}
						break;
					case 2:// calculate difference per sec
						try {
							value = String.format("%3.3f", 1000.0 * (Double.valueOf(value) - prev_value) / duration);
						} catch (Exception e) {/* ignore */ }
						break;
					case 3:// calculate percentage of difference
						try {
							value = String.format("%3.3f", 200.0 * (Double.valueOf(value) - prev_value) / (Double.valueOf(value) + prev_value));
						} catch (Exception e) {/* ignore */ }
						break;
					case 4: // time format
						try{
							value = Utils.toFormatedTime(Long.valueOf(value));
						}catch (Exception e) {/* ignore */ }
						break;
					case 5:// convert ns to sec
						value = String.format("%3.3f", Double.valueOf(value)/1e9);
						break;
					case 6:// convert ms to sec
						value = String.format("%3.3f", Double.valueOf(value)/1e3);
						break;
					case 7:// convert byte to kb
						value = String.format("%3.3f", Double.valueOf(value)/1024.0);
						break;
					case 8://convert byte to mb
						value = String.format("%3.3f", Double.valueOf(value)/1048576.0);
						break;
					case 9://convert to percent (multiply by 100)
						value = String.format("%3.3f", Double.valueOf(value)*100.0);
						break;
					}
				}

				ret = new MonResult(name, value);
			}
		}
		return ret;
	}

	/**
	 * Composes results string
	 */
	@Override
	public List<MonResult> get_results() {
		List<MonResult> results = new ArrayList<MonResult>();
		if (connection != null) {
			try {
				connection.getDefaultDomain();// heartbeat
			} catch (IOException e) {// problem while connect
				try {
					client.closeConnection(connection);
				} catch (IOException e1) {
					/*ignore*/
				} finally {
					connection = null;
				}
				try {// restore connection
					connect();
//					connection = client.openConnection(serviceUrl, username, password);
					logger.info("Connection reestablished");
				} catch (Exception e1) {// Impossible to connect
					connection = null;
					e1.printStackTrace();
				}
			}
		} else {
			try {// prepare connection
				connect();
//				connection = client.openConnection(serviceUrl, username, password);
			} catch (Exception e1) {// Impossible to connect
				connection = null;
				e1.printStackTrace();
			}
		}

		if (connection != null) {
			for (int i = 0; i < params.size(); i++) {
				MonResult res = getResult(connection, params.get(i));
				if (res != null) {
					results.add(res);
				}
			}
			results.add(new MonResult("status", "OK"));
		} else {
			results.add(new MonResult("status", "NOK"));
		}
		return results;
	}

	@Override
	public JSONArray get_additionalResults() {
		return null;
	}

	/**
	 * stores results into CSV file instead to send them into Monitis
	 */
	private void debug_processing() {
		boolean header = true;
		String fileName = debug_file;
		long stop_time = Long.MAX_VALUE;
		if (test_duration > 0){
			stop_time =	new Date().getTime() + test_duration;
		}
		while (new Date().getTime() < stop_time) {
			List<MonResult> res = get_results();

			if (res.size() > 0) {
				String[] line = null;
				ArrayList<String> array = new ArrayList<String>();
				if (header) {
					array.add("Timestamp");
					for (int i = 0; i < res.size(); i++) {
						array.add(res.get(i).getParamName());
					}
					line = new String[array.size()];
					line = array.toArray(line);
					Utils.putIntoCSV(fileName, false, ",", line);
					header = false;
				}
				array.clear();
				array.add(String.valueOf(new Date().getTime()));
				for (int i = 0; i < res.size(); i++) {
					array.add(res.get(i).getParamValue());
					logger.trace(res.get(i).toString());
				}
				line = new String[array.size()];
				line = array.toArray(line);
				boolean ok = Utils.putIntoCSV(fileName, true, ",", line);
				logger.info("Put line into CSV - "+ok);
			}
			try {
				Thread.sleep(duration);
			} catch (InterruptedException e) {/* ignore */}
		}
	}
	
	private void processing() throws Exception {
		if (debug_on && debug_file != null && debug_file.length() > 0) {
			logger.info("***** DEBUG PROCESSING *****");
			debug_processing();
		} else {
			logger.info("***** Monitoring started *****");
			GenericCustomMonitorRunner tst = new GenericCustomMonitorRunner(new JMXMonitor());
			tst.runMonitor();
		}

	}
	
	@Override
	protected void finalize() throws Throwable {
		if (connection != null) {
			try {
				client.closeConnection(connection);
			} catch (IOException e) {/* ignore */
			}
		}			
		super.finalize();
	}

	public static void main(String[] args) {
		
		try {
			JMXMonitor jmxMon = new JMXMonitor();
			jmxMon.processing();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}
