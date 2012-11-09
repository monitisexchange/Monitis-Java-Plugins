package org.monitis.monitor.crl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.monitis.GenericCustomMonitor.GenericCustomMonitorRunner;
import org.monitis.GenericCustomMonitor.IGenericCustomMonitorWrapper;
import org.monitis.beans.MonResult;
import org.monitis.crl.CRL;
import org.monitis.crl.CRLTest;
import org.monitis.exception.MonitisException;
import org.monitis.util.Utils;
import org.monitis.utils.MConfig;

public class CRLMonitor  extends IGenericCustomMonitorWrapper {
	private static String confFile = "/properties/monitor.config";
	private ArrayList<JSONObject> params = new ArrayList<JSONObject>();
	// stores results in the file only when debug_on is true and debug_file is specified 
	private String debug_file = null;
	private boolean debug_on = false;
	private long duration = 0;
	private long test_duration = 0;
	private String crl_uri = null;
	private Logger logger;

	public CRLMonitor() throws Exception {
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
		crl_uri = conf.getConfigStringValue("monitor.crl_uri");
		debug_file = conf.getConfigStringValue("debug.file");
  		debug_on = conf.getConfigBooleanValue("debug.turn_on");
  		duration = conf.getConfigIntValue("monitor.processingTime", 1) * 60000l;
  		test_duration = conf.getConfigIntValue("monitor.testDuration", (int)duration*10) * 60000l;
  		
		//Also the Log4j configuration file should be there.
		String path = new File(conf.getPropFile()).getParent().concat("/log4j.xml");
		if (new File(path).canRead()){
			DOMConfigurator.configure(path);
		}
		logger = Logger.getLogger(CRLMonitor.class);

		if (crl_uri == null || crl_uri.length() == 0){
			logger.fatal("CRL URI isn't defined");
			throw new Exception("CRL URI isn't defined");
		}
		
		ArrayList<String> arr = conf.getConfigStringAsArray("monitor.result_params");
		if (arr == null) {
			logger.fatal("No data for 'monitor.result_params'");
			throw new Exception("No data for 'monitor.result_params'");
		} else {
			logger.info("'monitor.result_params' aray contain " + arr.size() + " elements");
			for (int i = 0; i < arr.size(); i++) {
				String prm = arr.get(i);
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
					array.add(Arrays.toString(res.get(i).getParamValue()));
					logger.trace(Arrays.toString(res.get(i).getParamValue()));
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
			GenericCustomMonitorRunner tst = new GenericCustomMonitorRunner(this);
			tst.runMonitor();
		}

	}

	private MonResult getResult(List<CRL> list, JSONObject json) {
		MonResult ret = null;
		if (list != null && json != null) {
			String[] format = splitFormat(json.optString("format"), ":");
			if (format != null) {
				String name = format[0];
				String[] value = new String[list.size()];
				for (int i = 0; i < value.length; i++) {
					if (name.equalsIgnoreCase("name")){
						value[i] = list.get(i).getName();
					} else
					if (name.equalsIgnoreCase("nextUpdate")){
						value[i] = Utils.getFormated(list.get(i).getNextUpdate(), "yyyy-MM-dd HH.mm.ss");
					} else
					if (name.equalsIgnoreCase("valid")){
						value[i] = list.get(i).isValid()?"yes":"no";
					} else
					if (name.equalsIgnoreCase("accessible")){
						value[i] = list.get(i).isAccessible()?"yes":"no";
					} else
					if (name.equalsIgnoreCase("status")){
						value[i] = (list.get(i).isAccessible() && list.get(i).isValid())?"ok":"nok";
					}
				}
				try {
					ret = new MonResult(name, value);
				} catch (MonitisException e) {
					logger.error("getResult: Exception while creating MonResult("+name+", "+Arrays.toString(value)+") "+e.toString());
				}
			}
		}

		return ret;
	}
	
	private MonResult getResult(CRL node, JSONObject json) {
		MonResult ret = null;

		if (node != null && json != null) {
			String[] format = splitFormat(json.optString("format"), ":");
			if (format != null) {
				String name = format[0];
				String value = null;
				if (name.equalsIgnoreCase("name")){
					value = node.getName();
				} else
				if (name.equalsIgnoreCase("nextUpdate")){
					value = Utils.getFormated(node.getNextUpdate(), "yyyy-MM-dd HH.mm.ss");
				} else
				if (name.equalsIgnoreCase("valid")){
					value = node.isValid()?"yes":"no";
				} else
				if (name.equalsIgnoreCase("accessible")){
					value = node.isAccessible()?"yes":"no";
				} else
				if (name.equalsIgnoreCase("status")){
					value = (node.isAccessible() && node.isValid())?"ok":"nok";
				}
				
				ret = new MonResult(name, value);
			}
		}
		return ret;
	}

	@Override
	public JSONArray get_additionalResults() {
		return null;
	}

	@Override
	public List<MonResult> get_results() {
		List<MonResult> results = new ArrayList<MonResult>();

		CRLTest test = null;
		List<CRL> list = null;
		try {
			test = new CRLTest(crl_uri);
//			logger.info("\nCRLTest "+test+" for "+test.getUri()+" has type "+test.getType());
			list = test.analyze();
		} catch (Exception e) {
			logger.fatal("Exception: for "+crl_uri+" - "+e.toString());
			e.printStackTrace();
		}

		if (list != null) {
			for (int i = 0; i < params.size(); i++) {
				MonResult res = getResult(list, params.get(i));
				if (res != null) {
					results.add(res);
				}
			}
		}
//		logger.info("get_results: "+Arrays.toString(results.toArray(new MonResult[0])));
		return results;
	}

	public static void main(String[] args) {
		
		try {
			CRLMonitor crlMon = new CRLMonitor();
			crlMon.processing();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}
