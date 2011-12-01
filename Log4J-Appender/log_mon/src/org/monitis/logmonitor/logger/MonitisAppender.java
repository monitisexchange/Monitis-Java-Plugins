package org.monitis.logmonitor.logger;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.monitis.GenericCustomMonitor.GenericCustomMonitorRunner;
import org.monitis.logmonitor.LogMonitor;

public class MonitisAppender extends AppenderSkeleton {
	private LogWriter log_writer = new LogWriter();
	
	/** Creates a new instance of MonitisAppender */
	public MonitisAppender() {
		super();
	}
	
	/**
	*This method is called as a part of the appender initialization process
	*/
	@Override
	public void activateOptions() {
		try {
			new LogMonitor(this, log_writer);
		} catch (Exception e) {
			System.err.println("Exception while calling Monitis custom monitor: "+ e.getMessage());
			System.err.println("Monitis Appender cannot be used.");
//			System.exit(0);
		}		
	}

	/**
	 * This method is overridden from the super class and prints the logging
	 * message to the logging window. NOTE: If there is no layout specified for
	 * this Appender, no message will be displayed. The logging information is
	 * formatted according to the layout and the conversion pattern specified.
	 * 
	 * @param loggingEvent
	 *            encapsulates the logging information.
	 */
	@Override
	protected void append(LoggingEvent loggingEvent) {
		if (this.layout != null) {
			this.append(this.layout.format(loggingEvent));
		}
	}

	public void setFilter (Filter filter){
		super.addFilter(filter);
	}
	
	private void append (String event){
		try {
			log_writer.write(event);
		} catch (IOException e) {
			LogLog.debug(e.getMessage());
		}
	}
	
	@Override
	public void close() {
		closed = true;
		
	}

	/** This method is overridden from the super class and always
	* returns true to indicate that a Layout is required for
	* this appender. If not specified in the conf. file, then it
	* will not print any message in the log window.
	*/
	@Override
	public boolean requiresLayout() {
		return true;
	}

	public Writer getWriter(){
		return log_writer;
	}
	
	//*********Monitis Constants**********
	private String monitisApiKey = null;
	private String monitisSecretKey = null;
	private String monitisMonitorName = null;
	private String monitisMonitorTag = null;
	private long monitisSendInterval = 0;
	
	public String getMonitisApiKey() {
		return monitisApiKey;
	}

	public void setMonitisApiKey(String monitisApiKey) {
		this.monitisApiKey = monitisApiKey;
	}

	public String getMonitisSecretKey() {
		return monitisSecretKey;
	}

	public void setMonitisSecretKey(String monitisSecretKey) {
		this.monitisSecretKey = monitisSecretKey;
	}

	public String getMonitisMonitorName() {
		return monitisMonitorName;
	}

	public void setMonitisMonitorName(String monitisMonitorName) {
		this.monitisMonitorName = monitisMonitorName;
	}

	public String getMonitisMonitorTag() {
		return monitisMonitorTag;
	}

	public void setMonitisMonitorTag(String monitisMonitorTag) {
		this.monitisMonitorTag = monitisMonitorTag;
	}

	public long getMonitisSendInterval() {
		return monitisSendInterval;
	}

	public void setMonitisSendInterval(long monitisSendInterval) {
		this.monitisSendInterval = monitisSendInterval;
	}
	
}
