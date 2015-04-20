## The JMXBrowser ##

The JMXBroser represents the implementation of a stabdalone JMX client which allows to browses all Mbeans and their attributes values from any Java application.  
It can be used to investigate the content of JMX server of any Java application and choose desired MBeans to use, for instance, for configuring the [JMXMonitor](https://github.com/monitisexchange/Monitis-Java-Plugins/tree/master/JMX-Monitor).  
This project uses the Java Management Extensions possibilities for providing of a simple solution for users who wants to browse content of any Java application JMX.  

#### The project contain the following sources: ####

        browser                  JMXBrowser project folder 
            bin
              JMXBrowse.jar      Compiled version of JMXBrowser
              *.jar              libraries required by JMXBrowser  
            lib
              *.jar              libraries required by JMXBrowser  
            src                  Folder that contain the source codes for JMXBrowser  
            properties
              jmxconfig.json     The configuration file for JMXBrowser

        test                     The test Java application (sample - used for testing purpose only)
            JMXServer.jar        The test application
            jmxserver.sh         The script that start the test application

#### JMXBrowser configuration ####

The _jmxconfig.json_ file is used to configure JMXBrowser. It should be prepared in JSON form and has the following structure.

   <pre markdown="1">
	{
	  "object":{
	  	  "command": "JMXServer.jar",   <- The tested application start command 
	      "access":{
			  "host": "127.0.0.1",  <- The tested application host IP <i>(optional; the default value - "localhost")</i>
			  "port": 7199,         <- The tested application JMX port number <i>(optional; the default value - 0)</i>
			  "username": null,     <- The tested applcation JMX access credentials (user name; optional)
			  "password": null      <- The monitored applcation JMX access credentials (user password; optional)
	      }
	  },
	  "test": {
	      "debug": false                    <- Do (on true) or no print additional debug information
	  },
	  "output": {
	      "path": "tmp/jmx.csv",            <- The relative or absolute path for browsing results output file (
	      "separator": "|"                  <- The separator that will separates fields on each line
	  }
	}

   </pre>

Note:  

  - The JMXBrowser is trying to establish a local connection with JMX of tested application by using the application start command (if specified) or uses the defined port number and host for both local/remote connections. So, you can specify one of them. If you are defining these both parameters then the JMXBrowser tries firstly to establish connection by using the application start command and if it is failed uses a port and host.
  - If some of enumerated MBeans aren't accessible for some reason then the corresponding data will be silently omitted.
 
#### Testing and results ####

To check the correctness of JMXBrowser workability, the embedded test application has been started with the following parameters

        java -Dcom.sun.management.jmxremote.port=0 
             -Dcom.sun.management.jmxremote.authenticate="false" 
             -Dcom.sun.management.jmxremote="true" 
             -Dcom.sun.management.jmxremote.ssl="false" 
             -Dcom.sun.management.jmxremote.local.only="false" 
             -jar JMXServer.jar

This allow 
Next, you should run the JMXbrowser

        java -jar JMXBroser.jar

JMXBrowser will read the configuration, try to connect and enumerate all MBeans on specified Java application  
and put measured data into specified output file (if defined) or onto console otherwise.  

   <pre markdown="1">
	Object|Attribute|Key|Value                                            <- header separated by specified separator
	java.lang:type=Compilation|Name||HotSpot 64-Bit Tiered Compilers      <- measuring MBeans data (simple MBean)
	java.lang:type=Compilation|CompilationTimeMonitoringSupported||true
	java.lang:type=OperatingSystem|OpenFileDescriptorCount||12
	java.lang:type=OperatingSystem|CommittedVirtualMemorySize||1309061120
	java.lang:type=OperatingSystem|FreePhysicalMemorySize||158765056
	java.lang:type=Memory|Verbose||false
	java.lang:type=Memory|HeapMemoryUsage|committed|60948480              <- measuring MBeans data (Composite Mbean has a key)
	java.lang:type=Memory|HeapMemoryUsage|init|63511680
	java.lang:type=Memory|HeapMemoryUsage|max|904134656
	java.lang:type=Memory|HeapMemoryUsage|used|2335768
	java.lang:type=GarbageCollector,name=PS Scavenge|LastGcInfo|GcThreadCount|4
	java.lang:type=GarbageCollector,name=PS Scavenge|LastGcInfo|duration|1
	java.lang:type=GarbageCollector,name=PS Scavenge|LastGcInfo|endTime|8685386
	java.lang:type=GarbageCollector,name=PS Scavenge|LastGcInfo|id|3
	.....
   </pre>
  
Notice that the MBean's simple attributes (in contrast of composite attributes) doesn't contain any key  
and the corresponding line in the output file will contain nothing (blank) between two separators on place of key. 




