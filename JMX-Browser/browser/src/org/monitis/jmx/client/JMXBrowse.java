package org.monitis.jmx.client;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXServiceURL;

import org.monitis.util.MConfig;
import org.monitis.util.Utils;

public class JMXBrowse {

	private String java_command;
	private String host;
	private int port;
	private String username = null;
	private String password = null;
	private String format = null;
	private String file = null;
	private String separator;
	private boolean number_only = false;
	private static String[] line = {"Object", "Attribute", "Key", "Value"};
	private final static String config_path = "properties/jmxconfig.json"; 
	private boolean isDebug = false;

	public void init() throws Exception{
		MConfig conf = MConfig.getConfig(config_path);
		if (isDebug)		
			System.out.println(conf.getConfigForPrinting());
		
		java_command = conf.getConfigStringValue("object.command");
		host = conf.getConfigStringValue("object.access.host", "localhost");
		port = conf.getConfigIntValue("object.access.port", 0);
		username = conf.getConfigStringValue("object.access.username");
		password = conf.getConfigStringValue("object.access.password");
		format = conf.getConfigStringValue("output.format", "csv");
		file = conf.getConfigStringValue("output.path")+System.getProperty("file.separator")+"jmx."+format;
		separator = conf.getConfigStringValue("output.separator", ",");
		number_only = conf.getConfigBooleanValue("output.number_only");
		isDebug = conf.getConfigBooleanValue("test.debug");
		if (isDebug)
			System.out.println(getPrintParams());
	}
	
	private String getPrintParams(){
		StringBuilder strb = new StringBuilder();
		strb.append("******* Input Parameters*********");
		strb.append("\njava_command: "+java_command);
		strb.append("\nhost: "+host).append(":"+port);
		strb.append("\nusername: "+username).append("\tpassword: "+password);
		strb.append("\noutput: "+file).append(" ("+new File(file).getAbsolutePath()+")");
		strb.append("\tseparator: "+separator);
		return strb.toString();		
	}
	
	public void execute() {
		MBeanServerConnection connection = null;
		SimpleClient client = new SimpleClient();
		JMXServiceURL serviceUrl = null;
				
		if (java_command != null) {
			try {
				String JMXConnectionAddress = CheckEnvironment.getJMXlocalConnectorAddress(java_command, isDebug);
				if (JMXConnectionAddress != null && JMXConnectionAddress.length() > 0) {
					serviceUrl = new JMXServiceURL(JMXConnectionAddress);
				} else {
					System.err.println("Couldn't find the JMXConnectionAddress for " + java_command);
					System.exit(1);
				}
				if (serviceUrl == null && host != null && host.length() > 0 && port != 0) {
					// int pid = Integer.parseInt( ( new File("/proc/self")).getCanonicalFile().getName() );
					// System.out.println("pid = "+pid);
					//
					serviceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
				}
				System.out.println("serviceURL = " + serviceUrl);
				if (serviceUrl == null) {
					System.err.println("Couldn't connect to required object by JMX. ");
				} else {
					connection = client.openConnection(serviceUrl, username, password);
				} 
			} catch (Exception e){
				System.err.println(e);
				System.exit(1);
			}
		}
		
		Set<ObjectInstance> beans = null;
		try {// get all mbeans
			beans = connection.queryMBeans(null, null);
		}catch(Exception e){
			System.err.println("Exception while get all MBeans ("+e.getMessage()+")");
			System.exit(1);
		}
		if (file != null) {
			new File(file).delete();// remove the previously created file
			System.out.println("\nResults of MBeans browsing was saved in "+file);
			if (format.equals("csv")){// put header line 
				Utils.putIntoCSV(file, true, separator, line);
			} 
		} else {
			System.out.println(line[0] + ", " + line[1] + ", " + line[2] + ", " + line[3]);
		}

		try{
			for (ObjectInstance instance : beans) {
				ObjectName object_name = instance.getObjectName();
				MBeanInfo info = null;
				try{
					info = connection.getMBeanInfo(object_name);
				} catch(Exception e){
					continue;
				}
				String object = object_name.getDomain() + ":" + object_name.getKeyPropertyListString();
				Object value;

				MBeanAttributeInfo[] attr = info.getAttributes();
				if (attr.length > 0) {
					for (int i = 0; i < attr.length; i++) {
						try {
							if (attr[i].isReadable()) { 
								try {
									value = client.query(connection, object, attr[i].getName(), null);
								} catch (Exception e) {
									continue;
								}
								String type = attr[i].getType().toLowerCase();
								if (type.contains("compositedata")){// Composite attribute
									Set<String> keys = ((CompositeDataSupport) value).getCompositeType().keySet();
//									System.out.println("Composite = ");
									for (String key : keys) {
										Object key_value = ((CompositeDataSupport) value).get(key);
										type = key_value.getClass().getSimpleName().toLowerCase();
//										System.out.println("Type = "+key_value.getClass().getSimpleName());
										if (number_only && !(type.contains("int") || type.contains("long") || type.contains("float") || type.contains("double"))) {
											continue;
										} else {
											line[0] = object;
											line[1] = attr[i].getName();
											line[2] = key;
											line[3] = String.valueOf(key_value);
											if (file != null)
												if (format.equals("csv")) {// put next line values
													Utils.putIntoCSV(file, true, separator, line);
												} else {
													Utils.putAsJson(file, true, line);
												}
											else
												System.out.println(line[0] + ", " + line[1] + ", " + line[2] + ", " + line[3]);
										}
									}
								} else if (number_only && !(type.contains("int") || type.contains("long") || type.contains("float") || type.contains("double"))) {// Numeric attribute
									continue;
								} else {
									line[0] = object;
									line[1] = attr[i].getName();
									line[2] = "";
									line[3] = String.valueOf(value);
									if (file != null)
										if (format.equals("csv")) {// put next line values
											Utils.putIntoCSV(file, true, separator, line);
										} else {
											Utils.putAsJson(file, true, line);
										}
									else {
										System.out.println(line[0] + ", " + line[1] + ", " + line[2] + ", " + line[3]);
									}
								}
							} 
						} catch (Exception ex) {/*ignore*/}
					}
				}
			}
		} catch (Exception ex) {
			System.err.println("Exception: " + ex.toString());
		} finally {
			if (connection != null)
				try {
					client.closeConnection(connection);
				} catch (IOException e) {/*ignore*/}
		}		
	}
	
	public static void main(String[] args) throws Exception {
		JMXBrowse br = new JMXBrowse();
		br.init();
		br.execute();	
		System.exit(0);
	}

}
