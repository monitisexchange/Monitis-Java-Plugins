## Tomcat JMXMonitor ##

This Tocat monitor represents the implementation of a standalone [JMX client](https://github.com/monitisexchange/Monitis-Java-Plugins/tree/master/JMX-Monitor) which allows to get Mbeans attributes values from any Java application and send them into Monitis via the Monitis open API.  
This project uses the Java Management Extensions possibilities and the Monitis custom monitor approach for providing of a simple solution for users who wants to monitor Tomcat Java application.  
The current monitor is tunned to monitor the Apache Tomcat few important metrics.  

#### Monitored Application ####

You don't need to do any changes in Apache Tomcat at all.  
The only requirements is the following:  

  - The JMX agent should be enabled on the monitored application JVM (Tomcat) and be implemented on J2SE version 5 or higher.
  - The Apache Tomcat should be started by using the following parameters
 
        java -Dcom.sun.management.jmxremote.port=<port number>          <- Creates a remote JMX connector to listen through the specified port.
             -Dcom.sun.management.jmxremote.authenticate="true/false"   <- Use (or not) a passwords to access MBeans.
             -Dcom.sun.management.jmxremote="true/false"                <- Enables (or not) the JMX agent.
             -Dcom.sun.management.jmxremote.ssl="true/false"            <- Use (or not) use security access for monitoring.
             -Dcom.sun.management.jmxremote.local.only="true/false"     <- Allows (or not) establish any connection requests from local interfaces only.
             <application class>                                        <- Your application class that contains main method (for Tocat - "Bootstrap").



Notes:

  - JMX server doesn't have any default port. You can choose anyone free port. The first available port is set when 0 value is specified  as port.
  - After you have enabled the JMX agent for remote or local use, you can monitor Tomcat application using presented JMXMonitor.


#### The project contain the following sources: ####

        bin
            JMXMonitor_lib
              m_api.jar          Compiled Monitis API JAR file  
              *.jar              other libraries required for JMXMonitor
            JMXMonitor.jar
        properties
            log4j.xml          The configuration file for Log4j engine
            monitor.config     The configuration file for JMXMonitor
        src                    Folder that contain the source codes for JMXMonitor

Please notice that we are using in this project the cropped Open API Java library - m_api.jar (the custom monitor strongly needed functionality have been kept only)  

#### Tomcat Monitor configuration ####

The _monitor.config_ file is used to configure JMXMonitor. It should be prepared in JSON form and has the following structure.

   <pre markdown="1">
	{
	  "api":{
	      "server": "http://monitis.com",                        <- Monitis server URL that support Monitis Open API <i>(optional; the default value - http://monitis.com)</i>
	      "version": "2"                                         <- Open API version <i>(optional; the default value - 2)</i>
	  },
	  "user_account":{
	      "apiKey": "T5BAQQ46JPTGR6EBLFE28OSSQ",                 <- The personal API key that can be obtained from Monitis user account <b>(mandatory)</b>
	      "secretKey": "248VUB2FA3DST8J31A9U6D9OHT"              <- The personal secret key that can be obtained from Monitis user account <b>(mandatory)</b>
	  },
	  "debug": {
	      "file": "./mon.csv",                                   <- Put results into file while DEBUG mode instead of send them into Monitis
	      "turn_on": yes                                         <- Switch on/off debug mode
	  },
	  "monitor":{
  		"app_command": "Bootstrap",                          <- The monitored application start command 
  		"host": "127.0.0.1",                                 <- The monitored application host IP <i>(optional; the default value - "localhost")</i>
		"port": 8080,                                        <- The listen port for Tomcat servlet
  		"jmxport": 0,                                        <- The monitored application JMX port number <i>(optional; the default value - 0)</i>
  		"username": null,                                    <- The monitored applcation JMX access credentials (user name)
  		"password": null,                                    <- The monitored applcation JMX access credentials (user password)
		"name": "TomcatServer_127.0.0.1:8080",               <- The name for JMXMonitor to be register <b>(mandatory)</b>
		"tag": "jmx",                                        <- The tag for JMXMonitor to be register <b>(mandatory)</b>
		"type": "Java",                                      <- The type for JMXMonitor to be register <b>(mandatory)</b>
		"testDuration": "0",                                 <- The duration of monitoring [min] (0 - infinitely)
		"processingTime": "1",                               <- The periodicity of sending measuring data into Monitis [min]
		"params_separator": ":",                             <- The separator
		"result_params":[                                    <- The array of definitions for send parameters into Monitis (each element of array is JSON object)
		    {"attribute":"bytesSent","jmxObject":"Catalina:type=GlobalRequestProcessor,name=http-XXXX","format":"bytesSent:bytesSent:dif:2", "calculate":1},
		    {"attribute":"bytesReceived","jmxObject":"Catalina:type=GlobalRequestProcessor,name=http-XXXX","format":"bytesReceived:bytesReceived:dif:2", "calculate":1},
		    {"attribute":"processingTime","jmxObject":"Catalina:type=GlobalRequestProcessor,name=http-XXXX","format":"processingTime:processingTime:dif:2", "calculate":1},
		    {"attribute":"errorCount","jmxObject":"Catalina:type=GlobalRequestProcessor,name=http-XXXX","format":"errorCount:errorCount:dif:2", "calculate":1},
		    {"attribute":"maxTime","jmxObject":"Catalina:type=GlobalRequestProcessor,name=http-XXXX","format":"maxTime:maxTime::2", "calculate":0},
		    {"attribute":"requestCount","jmxObject":"Catalina:type=GlobalRequestProcessor,name=http-XXXX","format":"requestCount:requestCount:dif:2", "calculate":1},
		    {"attribute":"requestCount","jmxObject":"Catalina:type=GlobalRequestProcessor,name=http-XXXX","format":"requestCountps:requestCount:ps:2", "calculate":2},
		    {"attribute":"maxThreads","jmxObject":"Catalina:type=ThreadPool,name=http-XXXX","format":"maxThreads:maxThreads::2", "calculate":0},
		    {"attribute":"currentThreadsBusy","jmxObject":"Catalina:type=ThreadPool,name=http-XXXX","format":"currentThreadsBusy:currentThreadsBusy::2", "calculate":0},
		    {"attribute":"currentThreadCount","jmxObject":"Catalina:type=ThreadPool,name=http-XXXX","format":"currentThreadCount:currentThreadCount::2", "calculate":0}
		],
		"additional_result_params":[                         <- The array of definitions for send aditional parameters into Monitis (each element of array is JSON object)
			{"format":"details:Details::3"}              <- The data format in form required by <a href="http://monitis.com/api/api.html#addCustomMonitor">Open API</a>
			...                                          <- Additional parameters (JSON objects)
		]
	  }
	}

   </pre>

Note:  

  - The JMXMonitor is trying to establish a local connection with JMX of monitored application by using the application start command (if specified) or uses the defined jmxport number and host for both local/remote connections. So, you can specify one of them. If you are defining these both parameters then the JMXMonitor tries firstly to establish connection by using the application start command and if it is failed uses a port and host.
  - You can obtain the list of existing MBeans and their attributes by using e.g. JConsole (supplied with J2SE) or any other JMX browser (e.g. <a href="https://github.com/monitisexchange/Monitis-Java-Plugins/tree/master/JMX-Browser">JMX-Browser</a>).
  - If some of specified MBeans aren't accessible for some reason then the corresponding data will be ommitted and will not be sent into Monitis.
 
There are some peculiarities while composing of result parameters part in the configuration file  

  - Every line in the result_params array should be presented as a JSON object and therefore should be enclosed in the {} brackets
  - Every JSON object is presenting one measured metric and should have the following keywords
     - "jmxObject": "_The definition of JMX MBean object for monitoring_"
     - "attribute": "_The attribute name for monitoring_"
     - "key": "_The key name for monitoring (<font color=red>optional; required for Composite attribute only</font>)_"
     - "format":"_The data format in form required by <a href="http://monitis.com/api/api.html#addCustomMonitor">Open API</a>_"
     - "calculate":"_The calculation style of metric_"
        - 0 - do nothing and put the measured value in result set as it
        - 1 - calculate absolute difference between current and previous results
        - 2 - calculate rate of changes between current and previous results per sec
        - 3 - calculate percentage of difference between current and previous results

Please Note that every necessary metrics can be easily added into set of measurement by creating composition for new JSON formatted line in the "result_params" part.  

#### Testing and results ####

To check the correctness of Tomcat monitor workability, the wollowing environment was used

  - CPU:	Intel Core i5-2300 2.8 GHz x64
  - Memory:	4 GB
  - OS Name:    Linux-Ubuntu 11.10 (oneiric) with kernel 3.0.0-23-generic
  - JVM Version 1.6.0_26-b03
  - Apache Tomcat version 6.0.13 
  - WEB Application - Java PetStore (J2EE BluePrints Sample Application)
  - Apache Tomcat has been started with the following parameters

        java -Dcom.sun.management.jmxremote.port=0 
             -Dcom.sun.management.jmxremote.authenticate="false" 
             -Dcom.sun.management.jmxremote="true" 
             -Dcom.sun.management.jmxremote.ssl="false" 
             -Dcom.sun.management.jmxremote.local.only="false" 

  -  The Load simulator has been used to generate the quite enough load into server and request for various Web Application pages

After sometime there was accumulated enought numbers of data so the results can be viewed on the [Monitis dashboard](http://www.monitis.com) of user.  
To do so, user should login to Monitis his account and add a new custom monitor into dashboard (Monitors -> Manage Monitors -> Custom Monitors).  

<a href="http://i.imgur.com/0sphl"><img src="http://i.imgur.com/0sphl.png" title="TomcatMonitor test" /></a> .  


Also the Graphical representation of monitored data can be shown by switching to the corresponding view  

<a href="http://i.imgur.com/tjPGE"><img src="http://i.imgur.com/tjPGE.png" title="TomcatMonitor test" /></a> .  





