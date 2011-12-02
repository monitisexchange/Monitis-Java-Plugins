## Improved Monitis Java API

Some additions to the existing Monitis Java API are made that allow 
to simply build almost any type of custom monitor.

The source code of additions are added and described. 
The corresponding examples has been added as well.

Basically, only two additional classes were added to the existing Monitis API

-	GenericCustomMonitorRunner.java  
    This class is responsible for adding of a new custom monitor to the existing Monitis monitors scope (if it doesn't yet exist), configure and run it. The necessary parameters and values should be provided by user class that extends the IGenericCustomMonitor abstract class.

-	IGenericCustomMonitor.java  
    Abstract class that provide all definitions for Generic Custom Monitor. User should extends it for implementing own custom monitor. (see attachments)

_Note that user should provide only one simple Java class and the desired custom monitor will be created instantly._

Look through the added examples for memcached server custom monitor that could give some more details (package - org/sourcio/monitis/test).

Notice you have to have all required libraries to be able to use this code

- Monitis existing Java API (<http://www.monitis.com/api/monitisapi.jar>)

	- All required by Monitis Java API libraries (some of Apache Commons libraries (Lang, Codec, etc.), JSON, Log4J, etc.)

- memcached client (<http://code.google.com/p/xmemcached>)

	- Simple Logging Facade for Java libraries(required by memcached client): slf4j-api and slf4j-simple (<http://www.slf4j.org/>)

The sources are organized as the following:

*  src/org/monitis/GenericCustomMonitor folder contains _GenericCustomMonitorRunner.java_ and _IGenericCustomMonitor.java_ classes.  
    They just provide improvement for Monitis Custom Monitors Java API. 
   

*  src/org/monitis/test folder contains two test classes _MemcachedSimpleMonitor.java_ and _MemcachedMonitor.java_.  
    These two classes are presenting two variations of custom monitors intended to monitoring for Memcached server  

_Please Note that the 'distr' folder contains the improved but cropped variant of Monitis Java API (strongly needed functionality for custom monitor have been kept only) and also all its depending libraries. So, you can use them instead of download._

