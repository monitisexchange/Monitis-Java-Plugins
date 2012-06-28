package org.monitis.monitor.jboss7;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.monitis.GenericCustomMonitor.GenericCustomMonitorRunner;
import org.monitis.GenericCustomMonitor.IGenericCustomMonitor;
import org.monitis.beans.MonResult;
import org.monitis.beans.MonResultParameter;
import org.monitis.beans.MonitorParameter;
import org.monitis.enums.DataType;
import org.monitis.utils.MConfig;

/**
 * @author Drago Z Kamenov
 *	This class implements a custom monitor for JBoss7 monitoring with Monitis
 */
public class JBoss7Monitor extends IGenericCustomMonitor {
	public static final String CONFIG_FILE = "properties/monitor.config";
	public static final int DEFAULT_PORT = 9990;
	public static final String DEFAULT_HOST = "localhost";
	
	private Logger logger = Logger.getLogger(getClass());
	private String apiKey;
	private String secretKey;
	private String paramSeparator;
	private MConfig cfg = null;
	private ManagementClient client = null;

	private String monitorName;
	private String monitorTag;
	private String monitorType;
	private List<MonResultParameter> resultParams;

	/*
	 * Main method - invoke this from the command line
	 */
	public static void main(String[] args) throws Exception {
		JBoss7Monitor mon = new JBoss7Monitor();
		GenericCustomMonitorRunner runner = new GenericCustomMonitorRunner(mon);
		runner.runMonitor(); // this will run the monitor in a new thread
	}

	
	/** Instantiate a monitor with default settings
	 * @throws Exception
	 */
	public JBoss7Monitor() throws Exception {
		try {

			cfg = MConfig.getConfig(CONFIG_FILE);

			String username = cfg.getConfigStringValue("monitor.username");
			String password = cfg.getConfigStringValue("monitor.password");
			String host = cfg.getConfigStringValue("monitor.host", DEFAULT_HOST);
			paramSeparator = cfg.getConfigStringValue("monitor.params_separator", ":");
			int port = cfg.getConfigIntValue("monitor.port", DEFAULT_PORT);
			logger.info("host=" + host + ", port=" + port);
			apiKey = cfg.getConfigStringValue("user_account.apiKey");
			secretKey = cfg.getConfigStringValue("user_account.secretKey");
			monitorName = cfg.getConfigStringValue("monitor.name", "Custom");
			monitorTag = cfg.getConfigStringValue("monitor.tag", "custom");
			monitorType = cfg.getConfigStringValue("monitor.type", "custom");
			resultParams = buildResultParams("monitor.result_params.attributes");

			client = new ManagementClient(username, password);

			client.setHost(host);
			client.setPort(port);
		} catch (Exception e) {
			throw new MonitorException("Could not read config file", e);
		}
	}

	@Override
	public JSONArray get_additionalResults() {
		return null;
	}

	
	/* This method will be called by the CustomMonitorRunner to collect the data
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_results()
	 */
	@Override
	public List<MonResult> get_results() {
		logger.debug("Getting results from Client...");

		try {
			JSONObject resp = client
					.executeOp("/management/subsystem/datasources/data-source/AsteriskDS/statistics/pool?include-runtime=true");
			logger.debug("JSON Response:" + resp);
			ArrayList<MonResult> results = new ArrayList<MonResult>();

			logger.debug("Response length is " + resp.length());

			for (@SuppressWarnings("unchecked")
			Iterator<String> i = resp.keys(); i.hasNext();) {
				String key = i.next();
				String val = resp.getString(key);

				logger.debug(key + "=>" + val);
				results.add(new MonResult(key, val));
			}
			return results;
		} catch (JSONException e) {
			logger.error("Could not process JSON Response:", e);
			throw new MonitorException("Could not process JSON Response", e);
		} catch (MonitorException e) {
			logger.error("ManagementClient threw an exception", e);
			throw e;
		}
	}

	@Override
	public List<MonResultParameter> get_additionalResultParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String get_apiKey() {
		return apiKey;
	}

	@Override
	public List<MonitorParameter> get_monitorParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String get_monitor_name() {
		return monitorName;
	}

	@Override
	public String get_monitor_tag_value() {
		return monitorTag;
	}

	@Override
	public String get_monitor_type_value() {
		return monitorType;
	}

	@Override
	public List<MonResultParameter> get_resultParams() {
		return resultParams;
	}

	/** Build the monitor specification based on the config file. The CustomMonitorRunner will invoke this method to create the monitor
	 * @param key - a path in the JSON configuration pointing to the monitor parameters array
	 * @return
	 */
	private List<MonResultParameter> buildResultParams(String key) {
		List<MonResultParameter> retval = new ArrayList<MonResultParameter>();
		String val = cfg.getConfigStringValue(key);
		try {
			JSONArray params = new JSONArray(val);
			logger.debug("Building ResultParameters array: " + key);
			logger.debug("Array " + key + " contains " + params.length()
					+ " elements");
			for (int i = 0; i < params.length(); i++) {
				JSONObject entry = params.getJSONObject(i);
				String format = entry.getString("format");

				String parmAttrs[] = format.split(paramSeparator); // name, displayName, uom, dataType
				DataType type = DataType.valueOf(new Integer(parmAttrs[3]));
				retval.add(new MonResultParameter(parmAttrs[0], parmAttrs[1],
						parmAttrs[2], type));
			}
		} catch (JSONException e) {
			logger.error("Could not load result parameters from monitor.result_params.attributes - does not contain a JSON array");
			throw new MonitorException(
					"Could not locate result parameter settings in config file", e);
		}
		return retval;
	}

	@Override
	public String get_secretKey() {
		return secretKey;
	}
}
