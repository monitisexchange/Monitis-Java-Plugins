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


/**
 * @author Drago Z Kamenov
 * This class is used by the JBoss7 Custom Monitor to execute management operation against JBoss 7's HTTP/JSON management interface
 */
public class ManagementClient {
	private String userName = "";
	private String password = "";
	private String host = "localhost";
	private int port = 9990;
	private Logger logger = Logger.getLogger(getClass());
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	
	/**
	 * Instantiate the ManagementClient
	 * @param userName
	 * @param password
	 */
	public ManagementClient(String userName, String password) {
		logger.debug("Instantiating new ManagementClient");
		this.userName = userName;
		this.password = password;
	}

	
	/** Execute a management operation against the JBoss server via HTTP, and perform authentication as needed
	 * @param urlCmd URL for management command, including parameters, but excluding host and port
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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
