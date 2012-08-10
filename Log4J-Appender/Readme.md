## The Monitis Log4J Appender ##

The MonitisAppender represents the Java implementation of a Log4j appender which allows to send selectively log records   
from any Java application (that uses Log4J) into Monitis via the Monitis open API.  
This project uses the Log4j extension possibility and the Monitis custom monitor approach to provide a simple solution  
for users whose only alternative is to implement a polling function against the output of a standard Log4j appender.  

Requirements for monitored application is the following:

  - Aplication should use Log4J logging library
  - log_mon.jar have to be included in addition into the list of Aplication libraries
  - Log4J configuration should have XML format and be extended to specify settings for Monitis Appender e.g. like depicted below:
 
        <appender name="monitisAppender" class="org.monitis.logmonitor.logger.MonitisAppender">
                <param name="monitisApiKey" value="2PE0HVI4DHP34JACKCAE37IOD4" />
                <param name="monitisSecretKey" value="7OI90FU3C3DA8ENLNJ0JGGOGO0" />
                <param name="monitisMonitorName" value="Log_files_monitor" />
                <param name="monitisMonitorTag" value="Custom_logger_monitor" />
                <param name="monitisSendInterval" value="60000" />

                <layout class="org.apache.log4j.PatternLayout">
                    <param name="ConversionPattern" value="%d{ISO8601} %-5p [%t]: %m%n" />
                </layout>

                <filter class="org.monitis.logmonitor.logger.MonitisFilter">
                    <param name="filterPattern" value="(Error|Fatal|Warn*|Attention)" />
                    <param name="minAllowedLevel" value="WARN" />
                </filter>
        </appender>


where:

  - monitisApiKey is user personal API key that can be obtained from Monitis user account
  - monitisSecretKey is user personal secret key that can be obtained from Monitis user account
  - monitisMonitorName is user monitor unique name
  - monitisMonitorTag is tag value for user monitors
  - monitisSendInterval is time interval [ms] between sending monitored info into Monitis
  - ConversationPattern is the pattern that will be used by MonitisAppender to format records
  - filterPattern is a pattern-string for selection of log records that you want to monitor.   
    This should be composed as a Java RegExp and correspond to Java Pattern.matches requirements.  
    (default is no any pattern - so all records will be sent to Log Monitor)
  - minAllowedLevel is the log records level (ERROR, WARN, DEBUG, etc.) that you want to monitor
    (default value is WARN)

##### The project contain the following sources: #####

        log_mon                  MonitisAppender project folder  
            lib
              m_api.jar          Compiled Monitis API JAR file  
              log4j.jar          log-engine that was extended by MonitisAppender
              *.jar              other libraries for MonitisAppender  
            src                  Folder that contain the source codes for MonitisAppender  

        log_simulator            The monitored application simulator (sample - used for testing purpose only)
            distr
              simulator_lib      folder that contain monitored application libraries
                 log_mon.jar     Compiled MonitisAppender file
                 log4j.jar       log-engine
              simulator.jar      Compiled simulator application
              log4j.xml          log-engine configuration
            lib
              log4j.jar          
              log_mon.jar        Compiled MonitisAppender file
            src                  Folder that contain the source codes for sample application
            log4j.xml            Configuration for Log4j log-engine  

Please notice that we are using in this project the cropped Open API Java library - m_api.jar (custom monitor strongly needed functionality have been kept only)  

##### Testing and results #####

The application simulator has been started (example is depicted below)  

        ~$ cd /home/worksp_api/log_simulator/distr
        ~/worksp_api/log_simulator/distr$ java -jar simulator.jar

After sometime there was accumulated some numbers of data so the results can be viewed on the Monitis dashboard of user.  
To do so, user should login to Monitis his account and add a new custom monitor into dashboard.  


You can see the default view of monitored data.  

<a href="http://i1175.photobucket.com/"><img src="http://i1175.photobucket.com/albums/r634/hsimon2012/a22457a1.png" title="Log4j test" /></a>

By double-clicking on any line of default view you will see the detailed log records.  

<a href="http://i1175.photobucket.com/"><img src="http://i1175.photobucket.com/albums/r634/hsimon2012/2d3b3271.png" title="Log4j test" /></a>

Also the Graphical representation of monitored data can be shown by switching to the corresponding view.  

<a href="http://i1175.photobucket.com/"><img src="http://i1175.photobucket.com/albums/r634/hsimon2012/f5034d54.png" title="Log4j test" /></a>





