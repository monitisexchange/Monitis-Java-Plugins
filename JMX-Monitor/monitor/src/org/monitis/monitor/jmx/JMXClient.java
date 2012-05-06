package org.monitis.monitor.jmx;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class JMXClient {
	private HashMap<MBeanServerConnection, JMXConnector> connections = new HashMap<MBeanServerConnection, JMXConnector>();

	public String getJMXlocalConnectorAddress(String command){
		List<VirtualMachineDescriptor> vms = VirtualMachine.list();
		String connectorAddress = null;
	    VirtualMachine vm = null;
	    JMXConnector connector = null;
		for (VirtualMachineDescriptor desc : vms) {
			connectorAddress = null;
			vm = null;
		    try {
		        vm = VirtualMachine.attach(desc);
		    } catch (AttachNotSupportedException e) {
		        continue;
		    } catch (IOException e) {
				System.out.println("VirtualMachine.attach exeption: "+e.toString());
			}
			try {
				Properties props = vm.getAgentProperties();
				props.list(System.out);
				String cmd = props.getProperty("sun.java.command");//.toLowerCase();
				System.out.println("cmd = "+cmd);
				if (!(cmd.equals(command) || cmd.contains(command))){
					continue;
				}
				connectorAddress = props.getProperty("com.sun.management.jmxremote.localConnectorAddress");
			} catch (Exception e){
				System.out.println("VirtualMachine propertiesh exception: "+e.toString());		    	
		    }
		    if (connectorAddress == null) {
		        continue;
		    } else {
		    	break;
		    }
		}
		return connectorAddress;
	}

	/**
	 * Open a connection to a MBean server.
	 * 
	 * @param serviceUrl
	 *            Service URL, e.g. service:jmx:rmi://HOST:PORT/jndi/rmi://HOST:PORT/jmxrmi
	 * @param username
	 *            Username
	 * @param password
	 *            Password
	 * @return MBeanServerConnection if successful.
	 * @throws IOException
	 */
	public MBeanServerConnection openConnection(JMXServiceURL serviceUrl, String username, String password) throws IOException {
		JMXConnector connector;
		HashMap<String, Object> environment = new HashMap<String, Object>();
		environment.put("jmx.remote.x.client.connection.check.period", 5000);// Add environment variable to check for dead connections.
		if (username != null && password != null) {
			environment = new HashMap<String, Object>();
			environment.put(JMXConnector.CREDENTIALS, new String[] { username, password });
			connector = JMXConnectorFactory.connect(serviceUrl, environment);
		} else {
			connector = JMXConnectorFactory.connect(serviceUrl, environment);
		}
		MBeanServerConnection connection = connector.getMBeanServerConnection();
		connections.put(connection, connector);
		return connection;
	}

	/**
	 * Close JMX connection.
	 * 
	 * @param connection
	 *            Connection.
	 * @throws IOException
	 */
	public void closeConnection(MBeanServerConnection connection) throws IOException {
		JMXConnector connector = connections.remove(connection);
		if (connector != null)
			connector.close();
	}

	/**
	 * Get object name object.
	 * 
	 * @param connection
	 *            MBean server connection.
	 * @param objectName
	 *            Object name string.
	 * @return Object name object.
	 * @throws InstanceNotFoundException
	 *             If object not found.
	 * @throws MalformedObjectNameException
	 *             If object name is malformed.
	 * @throws NagiosJmxPluginException
	 *             If object name is not unique.
	 * @throws IOException
	 *             In case of a communication error.
	 */
	public ObjectName getObjectName(MBeanServerConnection connection, String objectName)
			throws InstanceNotFoundException, MalformedObjectNameException, JmxException, IOException {
		ObjectName objName = new ObjectName(objectName);
		if (objName.isPropertyPattern() || objName.isDomainPattern()) {
			Set<ObjectInstance> mBeans = connection.queryMBeans(objName, null);

			if (mBeans.size() == 0) {
				throw new InstanceNotFoundException();
			} else if (mBeans.size() > 1) {
				throw new JmxException("Object name not unique: objectName pattern matches " + mBeans.size() + " MBeans.");
			} else {
				objName = mBeans.iterator().next().getObjectName();
			}
		}
		return objName;
	}

	/**
	 * Query MBean object.
	 * 
	 * @param connection
	 *            MBean server connection.
	 * @param objectName
	 *            Object name.
	 * @param attributeName
	 *            Attribute name.
	 * @param attributeKey
	 *            Attribute key.
	 * @return Value.
	 * @throws InstanceNotFoundException, IntrospectionException, ReflectionException
	 * 			IOException, AttributeNotFoundException, MBeanException
	 * 			MalformedObjectNameException, JmxException
	 */
	public Object query(MBeanServerConnection connection, String objectName, String attributeName, String attributeKey)
			throws InstanceNotFoundException, IntrospectionException,
			ReflectionException, IOException, AttributeNotFoundException,
			MBeanException, MalformedObjectNameException, JmxException {
		Object value = null;
		ObjectName objName = getObjectName(connection, objectName);

		Object attribute = connection.getAttribute(objName, attributeName);
		if (attribute instanceof CompositeDataSupport) {
			CompositeDataSupport compositeAttr = (CompositeDataSupport) attribute;
			value = compositeAttr.get(attributeKey);
		} else {
			value = attribute;
		}
		return value;
	}

	/**
	 * Query MBean composite object.
	 * 
	 * @param connection
	 *            MBean server connection.
	 * @param objectName
	 *            Object name.
	 * @param attributeName
	 *            Attribute name.
	 * @param attributeKeys
	 *            Attribute keys array.
	 * @return Values array.
	 * @throws InstanceNotFoundException, IntrospectionException, ReflectionException
	 * 			IOException, AttributeNotFoundException, MBeanException
	 * 			MalformedObjectNameException, JmxException
	 */
	public Object[] query(MBeanServerConnection connection, String objectName, String attributeName, String[] attributeKeys)
			throws InstanceNotFoundException, IntrospectionException,
			ReflectionException, IOException, AttributeNotFoundException,
			MBeanException, MalformedObjectNameException, JmxException {
		Object[] values = null;
		ObjectName objName = getObjectName(connection, objectName);

		Object attribute = connection.getAttribute(objName, attributeName);
		if (attribute instanceof CompositeDataSupport) {
			CompositeDataSupport compositeAttr = (CompositeDataSupport) attribute;
			values = compositeAttr.getAll(attributeKeys);
		} else {
			values = new Object[1];
			values[0] = attribute;
		}
		return values;
	}

	/**
	 * Invoke an operation on MBean.
	 * 
	 * @param connection
	 *            MBean server connection.
	 * @param objectName
	 *            Object name.
	 * @param operationName
	 *            Operation name.
	 * @throws InstanceNotFoundException, IOException 
	 * @throws MalformedObjectNameException
	 * @throws MBeanException
	 * @throws ReflectionException
	 * @throws NagiosJmxPluginException
	 */
	public Object invoke(MBeanServerConnection connection, String objectName, String operationName) throws InstanceNotFoundException,
			IOException, MalformedObjectNameException, MBeanException,
			ReflectionException, JmxException {
		ObjectName objName = getObjectName(connection, objectName);
		return connection.invoke(objName, operationName, null, null);
	}

	/**
	 * Parse value as clazz.
	 * 
	 * @param clazz
	 *            Class.
	 * @param value
	 *            Value.
	 * @return Value parsed as Number of type clazz.
	 * @throws NagiosJmxPluginException
	 *             If clazz is not supported or value can't be parsed.
	 */
	Number parseAsNumber(Class<? extends Number> clazz, String value)
			throws JmxException {
		Number result;
		try {
			if (Double.class.equals(clazz)) {
				result = Double.valueOf(value);
			} else if (Integer.class.equals(clazz)) {
				result = Integer.valueOf(value);
			} else if (Long.class.equals(clazz)) {
				result = Long.valueOf(value);
			} else if (Short.class.equals(clazz)) {
				result = Short.valueOf(value);
			} else if (Byte.class.equals(clazz)) {
				result = Byte.valueOf(value);
			} else if (Float.class.equals(clazz)) {
				result = Float.valueOf(value);
			} else if (BigInteger.class.equals(clazz)) {
				result = new BigInteger(value);
			} else if (BigDecimal.class.equals(clazz)) {
				result = new BigDecimal(value);
			} else {
				throw new NumberFormatException("Can't handle object type ["
						+ value.getClass().getName() + "]");
			}
		} catch (NumberFormatException e) {
			throw new JmxException("Error parsing threshold " + "value ["
					+ value + "]. Expected [" + clazz.getName() + "]", e);
		}
		return result;
	}

}
