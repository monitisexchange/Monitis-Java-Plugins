package org.monitis.monitor.jboss7;

public class MonitorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 462328427934025605L;

	public MonitorException(String msg, Exception e) {
		super(msg,e);
	}
	
	public MonitorException(String msg) { 
		super(msg);
	}

}
