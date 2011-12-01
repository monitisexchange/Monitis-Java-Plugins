package org.monitis.logmonitor.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.monitis.logmonitor.logger.LogWriter;
import org.monitis.logmonitor.utils.Config;

public class LogServer extends Thread {
	 // host variable defines the list of allowed hosts that have permission to
	 // send log records to the LogServer. It can be specified as Java pattern
	 // (e.g "(10.137.25.55|localhost|127.0.0.1)") 
	 // or be defined simply as "INADDR_ANY" (if there is no any restriction for hosts)
	private static String host = Config.getConfigStringValue("server.allowed.host", "INADDR_ANY");
	private Pattern host_pattern = null;
	 // Server listen port (default value is defined as 4560)
	private static int port = Config.getConfigIntValue("server.port", 4560);
    private Logger logger =Logger.getLogger("log_server");
	private LogWriter writer;

	/**
	 * Formal constructor
	 * 
	 * @param writer
	 *            LogWriter class that should accumulate filtered log records
	 */
	public LogServer(LogWriter writer) {
		this.writer = writer;
		this.host_pattern = Pattern.compile(host, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public void run() {
		logger.debug("DEBUG: Listening on port " + port);
		ServerSocket server = null;
		try {
			server = new ServerSocket(port, 1000);// create ServerSocket
		} catch (IOException e1) {
			logger.debug("DEBUG: Exception while creating server socket - " + e1.getMessage());
			Thread.currentThread().interrupt();
		}
		while (true) {
			try {
				// wait for a connection
				logger.debug("Waiting to accept a new client.");

				Socket connection = server.accept(); // connection to client

				String iad = connection.getInetAddress().getHostName();

				logger.debug("Connected to client at " + iad);
				logger.debug("Starting new logger node.");

				if (host.indexOf("INADDR_ANY") >= 0 || host_pattern.matcher(iad).find()) {
					new LogNode(connection, writer).start();
				} else {
					logger.warn("Unknown client is connected - rejecting.");
				}
			} catch (Exception e) {
				logger.warn("Server terminated connection (" + e.getMessage() + ")");
			}
		}
	}

	public static void main(String[] args) {
		new LogServer(null).start();
	}
}
