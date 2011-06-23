package org.monitis.GenericCustomMonitor;

import java.util.List;

import org.monitis.beans.MonResult;
import org.monitis.beans.MonResultParameter;
import org.monitis.beans.MonitorParameter;
/**
 * Abstract class that provide definitions for Generic Custom Monitor.
 * User should extend it for implementing of own custom monitor
 */
public abstract class IGenericCustomMonitor {
	
	/**
	 * For Monitis API calls you need API key. 
	 * To get your keys log in to your Monitis account
	 *    go to Tools -> API -> API key
	 *   
	 * get_apiKey() should keep and return your apiKey 
	 * @return - your apiKey
	 */
	public String get_apiKey() {
		return null;
	}
	
	/**
	 * For Monitis API calls you need Secret key. 
	 * To get your keys log in to your Monitis account
	 *    go to Tools -> API -> API key
	 *   
	 * get_secretKey() should keep and return your secretKey 
	 * @return - your apiKey
	 */
	public String get_secretKey() {
		return null;
	}
	
	/**
	 * @return - existing or new custom monitor name
	 */
	public String get_monitor_name() {
		return null;
	}
	
	/**
	 * @return - existing or new custom monitor tag value
	 */
	public String get_monitor_tag_value() {
		return null;
	}
	
	/**
	 * The duration interval between sending monitoring result  
	 * 
	 * @return processing time [ms]
	 */
	public long get_processingTime() {
		return 60000;// 1 min
	}

	/**
	 * The duration for custom monitor test  
	 * 
	 * @return test duration [ms] (infinitely if returns <= 0)
	 */
	public long get_testDuration() {
		return 300000;// 5 min
	}

	/**
	 * The new monitor can have some parameters
	 * 
	 * Example for preparing  parameters
	 * <pre>
	 * 	List<MonitorParameter> monitorParams = new ArrayList<MonitorParameter>();
	 * 	monitorParams.add(new MonitorParameter("Quantity of servers", "ServersNumber", "5", DataType.INTEGER, true));
	 * 	monitorParams.add(new MonitorParameter("password", "Password", "mypass", DataType.STRING, true))
	 * </pre>
	 * 
	 * @return - new custom monitor list of parameters
	 * @see MonitorParameter class for details
	 */
	public List<MonitorParameter> get_monitorParams() {
		return null;
	}

	/**
	 * The resulting parameters should be defined for new custom monitor
	 * 
	 * Example of preparing custom monitor result parameters
	 * <pre>
	 * 	List<MonResultParameter> resultParams = new ArrayList<MonResultParameter>();
	 * 	resultParams.add(new MonResultParameter("cpu", "CPU", "%", DataType.FLOAT));
	 * 	resultParams.add(new MonResultParameter("mem", "MEM", "MB", DataType.INTEGER));
	 * </pre>
	 * 
	 * @return - new custom monitor list of resulting parameters
	 * @see MonitorParameter class for details
	 */
	public List<MonResultParameter> get_resultParams() {
		return null;
	}

	/**
	 * The current set of custom monitor results 
	 * 
	 * Example of generating custom monitor results
	 * <pre>
	 * 	String cpu = String.format("%3.1f", (Min + (Math.random() * (Max - Min))));
	 * 	String mem = String.format("%2d", (int)(Min + (Math.random() * (Max - Min))));
	 * 	List<MonResult> results = new ArrayList<MonResult>();
	 * 	results.add(new MonResult("cpu", cpu));
	 * 	results.add(new MonResult("mem", mem));
	 * </pre>
	 * 
	 * @return custom monitor list of current results set 
	 * @see MonResult class for details
	 */
	public List<MonResult> get_results() {
		return null;
	}

	/**
	 * Monitor can be deleted after series of tests
	 * 
	 * @return true if custom monitor have to be deleted after test
	 */
	public boolean deleteMonitor() {
		return true;
	}

	/**
	 * Test class will inform about ending test 
	 * by calling this method
	 */
	public void signal_testEnded(int err_code){
		
	}
}
