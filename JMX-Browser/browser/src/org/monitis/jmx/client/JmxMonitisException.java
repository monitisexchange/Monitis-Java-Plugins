package org.monitis.jmx.client;
public class JmxMonitisException extends Exception {

    private static final long serialVersionUID = -2700585651949336674L;

    /**
     * C'tor.
     */
    public JmxMonitisException() {
        super();
    }

    /**
     * C'tor.
     * @param message Message.
     * @param cause Cause.
     */
    public JmxMonitisException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * C'tor.
     * @param message Message.
     */
    public JmxMonitisException(String message) {
        super(message);
    }

    /**
     * C'tor.
     * @param cause Cause.
     */
    public JmxMonitisException(Throwable cause) {
        super(cause);
    }

}
