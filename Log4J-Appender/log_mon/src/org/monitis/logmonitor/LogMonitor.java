package org.monitis.logmonitor;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.NullEnumeration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.monitis.GenericCustomMonitor.GenericCustomMonitorRunner;
import org.monitis.GenericCustomMonitor.IGenericCustomMonitor;
import org.monitis.beans.MonResult;
import org.monitis.beans.MonResultParameter;
import org.monitis.beans.MonitorParameter;
import org.monitis.enums.DataType;
import org.monitis.logmonitor.logger.LogWriter;
import org.monitis.logmonitor.logger.MonitisAppender;
import org.monitis.logmonitor.test.LogSimulator;

public class LogMonitor extends  IGenericCustomMonitor implements Runnable {

//	private static final String apiKey = "2PE0HVI4DHP34JACKCAE37IOD4";
//	private static final String secretKey = "7OI90FU3C3DA8ENLNJ0JGGOGO0";
//    private static final String monitor_name = "Log_files_monitor";
//    private static final String monitor_tag_value = "Custom_logger_monitor";
    private static final String appenderName = "monitisAppender";
    private static final String writerName = "LogWriter";
    private LogWriter log_writer = null;
    private MonitisAppender monitis_appender = null;
    private GenericCustomMonitorRunner runner = null;
//    private Logger logger =Logger.getLogger("log_monitor");
    
    private LogMonitor(){
    	
    }
    
    public LogMonitor(MonitisAppender monitis_appender, LogWriter log_writer) throws Exception{
    	if (monitis_appender == null || !monitis_appender.getClass().getSimpleName().equalsIgnoreCase(appenderName)
   		|| log_writer == null || !log_writer.getClass().getSimpleName().equalsIgnoreCase(writerName)){
    		throw new Exception("Wrong parameters for LogMonitor.");
    	}
    	this.monitis_appender = monitis_appender;
    	this.log_writer = log_writer;
    	new Thread(this).start();
    }

	@Override
	public void run() {
		try {
			runner = new GenericCustomMonitorRunner(this);
			runner.runMonitor();
		} catch (Exception ex){
			System.err.println("LogMonitor >> Exception during starting of GenericCustomMonitor: "+ex.getMessage());
			System.err.println("Monitis Appender cannot be used.");
//			System.exit(1);
			return;
		}
		Thread.yield();
	}


	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_apiKey()
	 */
	@Override
	public String get_apiKey() {
		String apiKey = monitis_appender.getMonitisApiKey();
		System.out.println("LogMonitor >> apiKet: "+apiKey);
		return apiKey;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_secretKey()
	 */
	@Override
	public String get_secretKey() {
		String secretKey = monitis_appender.getMonitisSecretKey();
		System.out.println("LogMonitor >> secretKet: "+secretKey);
		return secretKey;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_monitor_name()
	 */
	@Override
	public String get_monitor_name() {
		String monitor_name = monitis_appender.getMonitisMonitorName();
		System.out.println("LogMonitor >> monitor name: "+monitor_name);
		return monitor_name;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_monitor_tag_value()
	 */
	@Override
	public String get_monitor_tag_value() {
		String monitor_tag_value = monitis_appender.getMonitisMonitorTag();
		System.out.println("LogMonitor >> monitor tag value: "+monitor_tag_value);
		return monitor_tag_value;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_processingTime()
	 */
	@Override
	public long get_processingTime() {
		long interval = monitis_appender.getMonitisSendInterval();
		System.out.println("LogMonitor >> monitor send interval [ms]: "+interval);
		return interval;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_testDuration()
	 */
	@Override
	public long get_testDuration() {
		return 0;// infinite
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
		
		System.out.println("LogMonitor >> result params: "+resultParams.toString());
		
		return resultParams;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#get_additionalResultParams()
	 */
	@Override
	public List<MonResultParameter> get_additionalResultParams() {
		List<MonResultParameter> resultParams = new ArrayList<MonResultParameter>();
		resultParams.add(new MonResultParameter("details", "Errors_detail", "", DataType.STRING));
		
		System.out.println("LogMonitor >> additional result params: "+resultParams.toString());
		
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
			System.err.println("LogMonitor >> Attention: no any records found yet.");
		} else {
			System.out.println("LogMonitor >> get_results: errors count = "+count);
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
					System.err.println(e.getMessage());
				} 
			}
		}
		
		if (jsa == null || jsa.length() <= 0){
			System.err.println("LogMonitor >> Attention - get_additionalResults returns NULL");			
		} else {
			System.out.println("LogMonitor >> get_additionalResults: "+jsa.toString());
		}
		
		return jsa;
	}

	/* (non-Javadoc)
	 * @see org.monitis.GenericCustomMonitor.IGenericCustomMonitor#deleteMonitor()
	 */
	@Override
	public boolean deleteMonitor() {
		return false;
	}

	private LogWriter getWriter(){
		LogWriter ret = log_writer;
		if (ret == null){
			ret = (LogWriter)findWriter(appenderName, writerName);
			log_writer = ret;
		}
		return ret;
	}
	
	private Writer findWriter(String appenderName, String writerName) {
		Writer ret = null;
		Category ct = Logger.getLogger("log_monitor");
		Enumeration<Category> en = null;
		while (ct != null
				&& (en = ct.getAllAppenders()).equals(NullEnumeration.getInstance())) {
			ct = ct.getParent();
		}

		Appender ap = null;
		while (en.hasMoreElements()) {
			ap = (Appender) en.nextElement();
			System.out.println("LogMonitor >> DEBUG: Appender " + ap.getName() + "(" + ap.getClass() + ")");
			if (ap.getName().equalsIgnoreCase(appenderName)) {
				Writer wr = ((MonitisAppender) ap).getWriter();
				if (wr != null) {
					System.out.println("LogMonitor >> DEBUG: Writer - " + wr.getClass());

					if (wr.getClass().getName().contains(writerName)) {
						System.out.println("LogMonitor >> DEBUG: Used writer is " + wr.getClass());
						ret = wr;
					}
				}
			}
		}
		return ret;
	}
	
	public static void main(String[] args) {
		System.err.println("LogMonitor >> This class shouldn't be called directly.");
		System.exit(1);
//		LogMonitor lm = new LogMonitor();
//		System.out.println("Returned writer is "+lm.getWriter());
//		System.out.println("Returned writer is "+lm.getWriter());
//		new LogSimulator().start();
//		try {
//			GenericCustomMonitorRunner tst = new GenericCustomMonitorRunner(new LogMonitor());
//		} catch (Exception e) {
//			System.err.println("Exception while test custom monitor: "+ e.getMessage());
//			System.exit(0);
//		}		
	}

}
