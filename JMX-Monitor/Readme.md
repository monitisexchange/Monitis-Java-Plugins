## The JMXMonitor ##

The JMXMonitor represents the implementation of a standalone JMX client which allows to get Mbeans attributes values  
from any Java application and send them into Monitis via the Monitis open API.  
This project uses the Java Management Extensions possibilities and the Monitis custom monitor approach for providing  
of a simple solution for users who wants to monitor any Java application.  
The JMX agent consists of an MBean server and a set of services for handling MBeans that can supply accumulated data  
through a protocol adaptor or connector.  

#### Monitored Application ####

You don't need to do any changes in your application at all.  
The only requirements for monitored application is the following:  

  - The JMX agent should be enabled on the monitored application JVM and be implemented on J2SE version 5 or higher.
  - The monitored application should be started by using the following parameters
 
        java -Dcom.sun.management.jmxremote.port=<port number>          <- Creates a remote JMX connector to listen through the specified port.
             -Dcom.sun.management.jmxremote.authenticate="true/false"   <- Use (or not) a passwords to access MBeans.
             -Dcom.sun.management.jmxremote="true/false"                <- Enables (or not) the JMX agent.
             -Dcom.sun.management.jmxremote.ssl="true/false"            <- Use (or not) use security access for monitoring.
             -Dcom.sun.management.jmxremote.local.only="true/false"     <- Allows (or not) establish any connection requests from local interfaces only.
             <application class>                                        <- Your application class that contains main method.



Notes:

  - JMX server doesn't have any default port. You can choose anyone free port. The first available port is set when 0 value is specified as port.
  - After you have enabled the JMX agent for remote or local use, you can monitor your application using presented JMXMonitor.


#### The project contain the following sources: ####

        monitor                  JMXMonitor project folder  
            lib
              m_api.jar          Compiled Monitis API JAR file  
              *.jar              other libraries for JMXMonitor  
            src                  Folder that contain the source codes for JMXMonitor  
            properties
              monitor.config     The configuration file for JMXMonitor

        test                     The monitored application simulator (sample - used for testing purpose only)
            JMXServer.jar        The test application
            jmxserver.sh         The script that start the test application

Please notice that we are using in this project the cropped Open API Java library - m_api.jar (the custom monitor strongly needed functionality have been kept only)  

#### Monitor configuration ####

The _monitor.config_ file is used to configure JMXMonitor and choose metrics for monitoring.  
It should be prepared in JSON form and has the following structure.

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
	  	"file": "./mon.csv",                                 <- The file to keeping monitoring data in debug mode
	  	"turn_on": no                                        <- Turn ON/OFF debug mode (In debug mode the results is stored in the CSV file instead of sending into Monitis)
	  },
	  "monitor":{
  		"app_command": "Bootstrap",                          <- The monitored application start command 
  		"host": "127.0.0.1",                                 <- The monitored application host IP <i>(optional; the default value - "localhost")</i>
  		"port": 8080,                                        <- The monitored application listener port number <i>(optional; the default value - 0)</i>
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
			{ "jmxObject": "java.lang:type=Memory",      <- The definition of JMX MBean object for monitoring
			  "attribute": "NonHeapMemoryUsage",         <- The attribute name for monitoring
			  "key": "used",                             <- The key name for monitoring <i>(optional; required for Composite attribute only)</i>
			  "format":"nheap_mem_us:nheap_mem_us::2",   <- The data format in form required by <a href="http://monitis.com/api/api.html#addCustomMonitor">Open API</a> 
			  "calculate":x},                            <- The calculate method for metric. The x value can be one of following: 
                                                                        0 - nothing to do and send as is (default)
                                                                        1 - calculate difference between current and previous values
                                                                        2 - calculate difference per second between current and previous values 
                                                                        3 - calculate percentage of difference between current and previous values 
                                                                        4 - tranform to time format (days-hours.min.sec)
			...                                          <- Another parameters (JSON objects)
			... 
		],
		"additional_result_params":[                         <- The array of definitions for send aditional parameters into Monitis (each element of array is JSON object)
			{"format":"details:Details::3"}              <- The data format in form required by <a href="http://monitis.com/api/api.html#addCustomMonitor">Open API</a>
			...                                          <- Additional parameters (JSON objects)
		]
	  }
	}

   </pre>

Note:  

  - The JMXMonitor is trying to establish a local connection with JMX of monitored application by using the application start command (if specified)  
    or uses the defined port number and host for both local/remote connections. So, you can specify one of them.  
    If you are defining these both parameters then the JMXMonitor tries firstly to establish connection by using the application start command  
    and if it is failed uses a port and host.
  - You can obtain the list of existing MBeans and their attributes by using e.g. JConsole (supplied with JSE) or any other JMX browser.
  - If some of specified MBeans aren't accessible for some reason then the corresponding data will be ommitted and will not be sent into Monitis.
 

#### Testing and results ####

To check the correctness of monitor workability, the Tomcat Server with the famous test application ([PetStore](http://www.google.am/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&ved=0CBsQFjAA&url=http%3A%2F%2Fjava.sun.com%2Fdeveloper%2Freleases%2Fpetstore%2F&ei=EGA_UI-4L8nDtAam-IC4Ag&usg=AFQjCNGUpE0iKdFppRItKwx1U6_EuRjCZQ)) has been started with the following parameters

        java -Dcom.sun.management.jmxremote.port=0 
             -Dcom.sun.management.jmxremote.authenticate="false" 
             -Dcom.sun.management.jmxremote="true" 
             -Dcom.sun.management.jmxremote.ssl="false" 
             -Dcom.sun.management.jmxremote.local.only="false"
              ......
              org.apache.catalina.startup.Bootstrap start

Next, you should start the JMX monitor. After sometime there was accumulated enought numbers of data so the results can be viewed on the Monitis dashboard of user.  
To do so, user should login to Monitis his account and add a new custom monitor into dashboard.  

<a href="http://i.imgur.com/rSBK3"><img src="http://i.imgur.com/rSBK3.png" title="JMXMonitor test" /></a> .  


Also the Graphical representation of monitored data can be shown by switching to the corresponding view  

<a href="http://imgur.com/99kTo"><img src="http://i.imgur.com/99kTo.png" title="JMXMonitor test" /></a> .  






