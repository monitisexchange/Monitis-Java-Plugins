package org.monitis.monitor.jmx;

public class JmxException extends Exception {

 	private static final long serialVersionUID = 530166642134176183L;

	public JmxException() {
        super();
    }

    /**
     * 
     * @param message Message.
     * @param cause Cause.
     */
    public JmxException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param message Message.
     */
    public JmxException(String message) {
        super(message);
    }

    /**
     * 
     * @param cause Cause.
     */
    public JmxException(Throwable cause) {
        super(cause);
    }

}
