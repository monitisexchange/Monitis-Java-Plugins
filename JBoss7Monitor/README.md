#### The JBoss 7 Monitor

This project implements a Monitis Custom monitor that is intended to monitor your JBoss 7 application server
It is fully implemented on Java and uses [Monitis Java SDK](https://github.com/monitisexchange/Monitis-Java-SDK) that provides the latest version (3.0) of Monitis Open API 

The project folder contains the following

        build.xml - an Ant build file should you need to compile the monitor yourself

        src
          org.monitis.monitor.jboss7
            JBoss7Monitor.java        The actual monitor code
            ManagementClient.java     A simple client to connect to JBoss 7 HTTP/JSON management interface
            ManagementException.java  A simple exception class used by the monitor
            log4j.properties          simple Log4j configuration file

        properties
            monitor.config  - A sample configuration file. It includes settings to monitor a JDBC data source. Be sure to change the
                              data source name to suit your specific app server configuration; and to specify your specific API key and Secret Key

        lib
          m_api.jar   - latest (but abridged) version of Monitis Open API
          moitis.jar  - full version of Monitis Open API
          ... plus some other other Monitis and third-party dependencies

        dist
          jboss7-monitor-*.jar  - the monitor bundled as a jar file

Building the monitor from source code
        Although the monitor includes functional binaries in a jar file, you may want to make changes and rebuild the monitor code. (You will need the [Ant tool v.1.7.1 or higher](http://ant.apache.org/bindownload.cgi) )

        1. Download the code from github
          git clone https://github.com/monitisexchange/Monitis-Java-Plugins.git

        2. Navigate to the project's root directory (where build.xml is located)
       
        3. Run ant from the command prompt:
          ant
        
Running the monitor:
        1. Navigate to the dist/ folder
        2. Start the monitor:
          java -jar jboss7-monitor-20120628.jar
    
Configuration File Settings

        apikey    - your ApiKey (can be obtained from your Monitis account - Tools->API->API Key) e.g. T5BAQQ46JPTGR6EBLFE28OSSQ"
        secretkey - your SecretKey that can be obtained the same way
        username  - your JBoss 7 admin username
        password  - your JBoss 7 server admin password
        name      - name for the new monitor 

