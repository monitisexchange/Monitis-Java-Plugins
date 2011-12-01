package org.monitis.logmonitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.monitis.GenericCustomMonitor.GenericCustomMonitorRunner;
import org.monitis.GenericCustomMonitor.IGenericCustomMonitor;
import org.monitis.beans.MonResult;
import org.monitis.beans.MonResultParameter;
import org.monitis.beans.MonitorParameter;
import org.monitis.enums.DataType;
import org.monitis.logmonitor.logger.LogWriter;
import org.monitis.logmonitor.server.LogServer;
import org.monitis.logmonitor.utils.Config;

/**
 * This class implements Monitis Custom Monitor that provides remote monitoring
 * of Java textual log. The necessary parameters should be provided by
 * configuration.
 */
public class LogMonitor extends  IGenericCustomMonitor {

	private String apiKey = Config.getConfigStringValue("mon.apiKey", "2PE0HVI4DHP34JACKCAE37IOD4");
	private String secretKey = Config.getConfigStringValue("mon.secretKey", "7OI90FU3C3DA8ENLNJ0JGGOGO0");
    private String monitor_name = Config.getConfigStringValue("mon.monitor_name", "Log_files_monitor");
    private String monitor_tag_value = Config.getConfigStringValue("mon.monitor_tag_value", "Custom_logger_monitor");
    private long processingTime = Config.getConfigIntValue("mon.processingTime", 60000); // 1 min
    private long testDuration = Config.getConfigIntValue("mon.testDuration", 600000); //10 min
    private boolean delete_Monitor = Config.getConfigBooleanValue("mon.deleteMonitor"); //delete monitor after test finishing
    private LogWriter logWriter = new LogWriter();
    private Logger logger =Logger.getLogger("log_monitor");
    
    public LogMonitor(){
    	new LogServer(logWriter).start();// Running log listening server
    }
    
	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_apiKey()
	 */
	@Override
	public String get_apiKey() {
		logger.info("apiKet: "+apiKey);
		return apiKey;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_secretKey()
	 */
	@Override
	public String get_secretKey() {
		logger.info("secretKet: "+secretKey);
		return secretKey;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_monitor_name()
	 */
	@Override
	public String get_monitor_name() {
		logger.info("monitor name: "+monitor_name);
		return monitor_name;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_monitor_tag_value()
	 */
	@Override
	public String get_monitor_tag_value() {
		logger.info("monitor tag value: "+monitor_tag_value);
		return monitor_tag_value;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_processingTime()
	 */
	@Override
	public long get_processingTime() {
		return processingTime;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_testDuration()
	 */
	@Override
	public long get_testDuration() {
		return testDuration;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_monitorParams()
	 */
	@Override
	public List<MonitorParameter> get_monitorParams() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_resultParams()
	 */
	@Override
	public List<MonResultParameter> get_resultParams() {
		List<MonResultParameter> resultParams = new ArrayList<MonResultParameter>();
		resultParams.add(new MonResultParameter("errors", "Errors_Number", "", DataType.INTEGER));
		
		logger.info("result params: "+resultParams);
		
		return resultParams;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_additionalResultParams()
	 */
	@Override
	public List<MonResultParameter> get_additionalResultParams() {
		List<MonResultParameter> resultParams = new ArrayList<MonResultParameter>();
		resultParams.add(new MonResultParameter("details", "Errors_detail", "", DataType.STRING));
		
		logger.info("additional result params: "+resultParams);
		
		return resultParams;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_results()
	 */
	@Override
	public List<MonResult> get_results() {
		String count = String.valueOf(getWriter().getListLength()); 
		List<MonResult> results = new ArrayList<MonResult>();
		results.add(new MonResult("errors", count));
		
		if (count.equalsIgnoreCase("0")){
			logger.warn("Attention: no any records found yet.");
		} else {
			logger.info("get_results: "+results);
		}

		return results;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_additionalResults()
	 */
	@Override
	public JSONArray get_additionalResults() {
		JSONArray jsa = null;
		int count = getWriter().getListLength();
		if (count > 0){
			jsa = new JSONArray();
			JSONObject jso;
			List<String> list = getWriter().getList(true);
			Iterator<String> it = list.listIterator();
			while (it.hasNext()){
				try {
//					jso = new JSONObject().put("details", Utils.encodeURI(it.next().trim()));
					jso = new JSONObject().put("details", it.next().trim());
					jsa.put(jso);
				} catch (Exception e) {
					logger.debug(e.getMessage());
				} 
			}
		}
		
		if (jsa == null || jsa.length() <= 0){
			logger.warn("please warning - get_additionalResults returns NULL");			
		} else {
			logger.info("get_additionalResults: "+jsa.toString());
		}
		
		return jsa;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#deleteMonitor()
	 */
	@Override
	public boolean deleteMonitor() {
		return delete_Monitor;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#signal_testEnded(int)
	 */
	@Override
	public void signal_testEnded(int err_code) {
		super.signal_testEnded(err_code);
		System.exit(err_code);
	}

	public LogWriter getWriter(){
		LogWriter ret = logWriter;
		if (ret == null){
			ret = new LogWriter();
			logWriter = ret;
		}
		return ret;
	}
		
	public static void main(String[] args) {
		try {
			new GenericCustomMonitorRunner(new LogMonitor()).runMonitor();
		} catch (Exception e) {
			System.err.println("Exception at start point of test custom monitor: "+ e.getMessage());
			System.exit(0);
		}		
	}

}
