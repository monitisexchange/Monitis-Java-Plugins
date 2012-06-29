package org.monitis.monitor.jboss7;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;


/** This class is used by the JBoss7 Custom Monitor to execute management operation against JBoss 7's HTTP/JSON management interface
 * @author Drago Z Kamenov
 */
public class ManagementClient {
	private String userName = "";
	private String password = "";
	private String host = "localhost";
	private int port = 9990;
	private Logger logger = Logger.getLogger(getClass());
	
	/** A getter method for JBoss 7 admin username
	 * @return user admin username
	 */
	public String getUserName() {
		return userName;
	}

	/** Use this to set the JBoss 7 admin username
	 * @param userName admin username
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/** Get the JBoss 7 admin password
	 * @return admin password
	 */
	public String getPassword() {
		return password;
	}

	/** Sets the jboss 7 admin password
	 * @param password admin password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	
	/** Instantiate the ManagementClient
	 * @param userName JBoss admin username (used for HTTP authentication)
	 * @param password JBoss admin password
	 */
	public ManagementClient(String userName, String password) {
		logger.debug("Instantiating new ManagementClient");
		this.userName = userName;
		this.password = password;
	}

	
	/** Execute a management operation against the JBoss server via HTTP, and perform authentication as needed. The response is then returned to the caller
	 * @param urlCmd URL for management command, including parameters, but excluding schema, host and port number
	 * @return JSONObject containing the response
	 */
	public JSONObject executeOp(String urlCmd) {
		logger.debug("Executing command " + urlCmd);
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(host, port),
                    new UsernamePasswordCredentials(userName, password));
            
            URL url = new URL("http", host, port, urlCmd);
            logger.debug("Command URL is:" + url);
            
            HttpGet httpget = new HttpGet(url.toString());
            
            logger.debug("executing request" + httpget.getRequestLine());
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
          
            if (entity == null) {
            	logger.error("Http response did not contain an entity");
            }
            BufferedReader is = new BufferedReader(new InputStreamReader(entity.getContent()));
            String line; 
            StringBuffer buff  =  new StringBuffer();
            while((line = is.readLine()) != null) { 
            	buff.append(line);
            }
            
            JSONObject jsonResp = new JSONObject(buff.toString());

            EntityUtils.consume(entity);

            return jsonResp;
        } catch(Exception e) { 
        	throw new MonitorException("Management operation failed", e);
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

	/** Gets the host name of the monitored JBoss 7 server
	 * @return host name
	 */
	public String getHost() {
		return host;
	}

	/** Sets the hostname for the monitored JBoss 7 server
	 * @param host host name or IP address
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/** Gets JBoss 7 HTTP management port
	 * @return port number
	 */
	public int getPort() {
		return port;
	}

	/** Sets the JBoss 7 HTTP management port
	 * @param port port number (usually 9990)
	 */
	public void setPort(int port) {
		this.port = port;
	}

}
