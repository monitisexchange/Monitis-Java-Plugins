package org.monitis.logmonitor.server;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.monitis.logmonitor.logger.LogWriter;

public class LogNode extends Thread {
	private Socket socket;
	private LogWriter writer;
	private ObjectInputStream ois;
    private Logger logger =Logger.getLogger("log_node");

	/**
	 * Formal constructor
	 * 
	 * @param socket
	 *            listen binded socket
	 * @param writer
	 *            LogWriter class that should accumulate filtered log records
	 * @throws Exception
	 */
	public LogNode(Socket socket, LogWriter writer) throws Exception {
		this.socket = socket;
		this.writer = writer;

		if (socket == null || writer == null) {
			throw new Exception("Invalide parameters while calling Node");
		} else {
			try {
				ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				logger.debug("DEBUG: ObjectInputStream = " + ois);
			} catch (InterruptedIOException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				throw e;// System.out.println("Could not open ObjectInputStream to "+socket+"("+e.getMessage()+")");
			}
		}
	}

	private String retrieveMessage(LoggingEvent event) {
		String ret = null;
		if (event != null) {
			ret = event.getLevel().toString() + "-"
					+ (new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(event.getTimeStamp()))) 
					+ ":" + event.getMessage();
		}
		return ret;
	}

	@Override
	public void run() {
		LoggingEvent event;

		try {
			if (ois != null) {
				while (true) {
					event = (LoggingEvent) ois.readObject();// read an event from the wire
					String msg = retrieveMessage(event);
					if (msg != null) {
						System.out.println("DEBUG: -->receive - "+msg);
						writer.write(msg);
					}
				}
			}

		} catch (EOFException ex) {
			logger.warn("DEBUG-run: Caught EOFException. (" + ex.getMessage() + ")");
		} catch (Exception e) {
			logger.warn("DEBUG-run: Caught Exception (" + e.getMessage() + ") - closing conneciton.");
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (Exception e) {
					logger.warn("Could not close connection." + "(" + e.getMessage() + ")");
				}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (InterruptedIOException e) {
					Thread.currentThread().interrupt();
				} catch (IOException ex) {
				}
			}
		}
	}

}
