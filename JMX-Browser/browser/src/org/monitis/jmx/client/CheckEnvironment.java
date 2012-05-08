package org.monitis.jmx.client;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * Browses through all JVMs on the host
 */
public class CheckEnvironment {

	/**
	 * Browses through all JVMs on the host having a goal to detect JMX connection address
	 * 
	 * @param command
	 *            Start command of Java application
	 * @param isDebug
	 *            prints debug information when this value is true
	 * @return JMX connection address on success
	 */
	public static String getJMXlocalConnectorAddress(String command, boolean isDebug){
		List<VirtualMachineDescriptor> vms = VirtualMachine.list();
		String connectorAddress = null;
	    VirtualMachine vm = null;
		if (isDebug) 
			System.out.println("*******Java Virtual Machines*******");
		
		for (VirtualMachineDescriptor desc : vms) {
			connectorAddress = null;
			vm = null;
		    try {
		        vm = VirtualMachine.attach(desc);
		    } catch (AttachNotSupportedException e) {
		        continue;
		    } catch (IOException e) {
				System.err.println("VirtualMachine.attach exeption: "+e.toString());
			}
			try {
				Properties props = vm.getAgentProperties();
				if (isDebug) 
					props.list(System.out);
				String cmd = props.getProperty("sun.java.command");
				if (!command.equalsIgnoreCase(cmd)){
					continue;
				}
				connectorAddress = props.getProperty("com.sun.management.jmxremote.localConnectorAddress");
			} catch (Exception e){
				System.err.println("VirtualMachine propertiesh exception: "+e.toString());		    	
		    }
		    if (connectorAddress == null) {
		        continue;
		    } else {
		    	break;
		    }
		}
		if (isDebug) 
			System.out.println("****************************");

		return connectorAddress;
	}

	
//	public static void main(String[] args) {
//		String cmd = "TestServer";
//		System.out.println("JMXConnectorAddress for \""+cmd+"\" = "+getJMXlocalConnectorAddress(cmd, true));
//	}

}
