package org.monitis.monitor.jmx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.monitis.GenericCustomMonitor.GenericCustomMonitorRunner;
import org.monitis.GenericCustomMonitor.IGenericCustomMonitorWrapper;
import org.monitis.beans.MonResult;
import org.monitis.utils.MConfig;

public class JMXMonitor extends IGenericCustomMonitorWrapper {
	private static String confFile = "/properties/monitor.config";
	private JMXClient client = null;
	private static String java_command = null;
	private JMXServiceURL serviceUrl;

	private String host = "localhost";
	private int port = 0;
	private String username = null;
	private String password = null;
	private ArrayList<JSONObject> params = new ArrayList<JSONObject>();

	public JMXMonitor() throws Exception {
		super();
		monitor_init();
	}

	private void monitor_init() throws Exception {
		MConfig conf = MConfig.getConfig(confFile);
		java_command = conf.getConfigStringValue("monitor.app_command", "TestServer");
		host = conf.getConfigStringValue("monitor.host", "localhost");
		port = conf.getConfigIntValue("monitor.port", 0);
		username = conf.getConfigStringValue("monitor.username");
		password = conf.getConfigStringValue("monitor.password");
		ArrayList<String> arr = conf.getConfigStringAsArray("monitor.result_params");
		if (arr == null) {
			System.err.println("No data for 'monitor.result_params'");
			System.exit(1);
		} else {
			System.out.println("'monitor.result_params' aray contain " + arr.size() + " elements");
			for (int i = 0; i < arr.size(); i++) {
				System.out.println(arr.get(i));
				try {
					params.add(new JSONObject(arr.get(i)));
				} catch (Exception ex) {/* ignore */
				}
			}
			if (params.size() <= 0) {
				System.err.println("No data in 'monitor.result_params'");
				System.exit(1);
			}
		}

		client = new JMXClient();

		if (java_command != null) {
			String JMXConnectionAddress = client.getJMXlocalConnectorAddress(java_command);
			if (JMXConnectionAddress != null && JMXConnectionAddress.length() > 0) {
				serviceUrl = new JMXServiceURL(JMXConnectionAddress);
			} else {
				System.err.println("Couldn't find the JMXConnectionAddress for " + java_command);
			}
		}
		if (serviceUrl == null && host != null && host.length() > 0 && port != 0) {
			// int pid = Integer.parseInt( ( new File("/proc/self")).getCanonicalFile().getName() );
			// System.out.println("pid = "+pid);
			//
			serviceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
		}
		System.out.println("serviceURL = " + serviceUrl);
		if (serviceUrl == null) {
			System.err.println("Couldn't connect to required object by JMX. ");
			System.exit(1);
		}
	}

	private Object query(MBeanServerConnection connection, String jmxObject, String attr, String key) {
		Object ret = null;
		try {
			ret = client.query(connection, jmxObject, attr, key);
		} catch (Exception ex) {/*Ignore*/}
		return ret;
	}

	private MonResult getResult(MBeanServerConnection connection, JSONObject json) {
		MonResult ret = null;

		if (connection != null && json != null) {
			String jmxObject = json.optString("jmxObject");
			String attribute = json.optString("attribute");
			String key = json.optString("key");
			String[] format = splitFormat(json.optString("format"), ":");

			if (jmxObject != null && attribute != null && format != null) {
				Object obj = query(connection, jmxObject, attribute, key);
				String value = String.valueOf(obj);
				ret = new MonResult(format[0], value);
			}
		}
		return ret;
	}

	@Override
	public List<MonResult> get_results() {
		MBeanServerConnection connection = null;
		List<MonResult> results = new ArrayList<MonResult>();
		Object attr, attr_;

		try {
			connection = client.openConnection(serviceUrl, username, password);
		} catch (Exception ex) {
			System.err.println("Could not establish connection...");
			System.exit(1);
		}

		for (int i = 0; i < params.size(); i++) {
			MonResult res = getResult(connection, params.get(i));
			if (res != null) {
				results.add(res);
			}
		}

		if (connection != null) {
			try {
				client.closeConnection(connection);
			} catch (IOException e) {/* ignore */
			}
		}
		return results;
	}

	@Override
	public JSONArray get_additionalResults() {
		return null;
	}

	public static void main(String[] args) throws Exception {
		// JMXMonitor jmxMon = new JMXMonitor();
		// List<MonResult> res = jmxMon.get_results();
		// System.out.println("Results count = "+res.size());
		// if (res.size() > 0){
		// for (int i = 0; i < res.size(); i++){
		// System.out.println(res.get(i).toUrlString());
		// }
		// }

		try {
			GenericCustomMonitorRunner tst = new GenericCustomMonitorRunner(new JMXMonitor());
			tst.runMonitor();
		} catch (Exception e) {
			System.err.println("Exception while testing custom monitor: " + e.getMessage());
			System.exit(0);
		}
	}

}
