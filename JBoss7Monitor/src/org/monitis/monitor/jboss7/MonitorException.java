package org.monitis.monitor.jboss7;

/** This is a simple Exception class to wrap various lower-level exceptions
 * @author Drago Z Kamenov
 *
 */
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
