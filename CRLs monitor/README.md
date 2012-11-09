#### The CRLs Monitor

This repository contains the implementation of Monitis Custom monitor that is intended to monitor CRLs validity.  
It is fully implemented on Java and use [Monitis Java SDK](https://github.com/monitisexchange/Monitis-Java-SDK) that wraps the latest version (3.0) Monitis Open API functionality.

The Repository contains the following

        src
          org.monitis.monitor.crl
            CRLMonitor.java          CRL cusom monitor implementation
          org.monitis.util
            Utils.java               Utilities class
        lib
          m_api.jar                  The latest (3.0) version of Monitis Java SDK wrapper for Monitis Open API  
          crl.jar                    CRLs support library
          jsoup-1.7.1.jar            HTML parse data support library
          ....
        properties
          monitor.config             Configuration for monitor
          log4j.xml                  Configuration for logger
        bin
          crl_monitor.jar            CRL monitor executable jar file 

The lib folder contains also other libraries that is depend on m-api.jar.  

If you want to test it,  
you have to have firstly the account in the Monitis,   
next you can change the monitor.config and log4j.xml files  
and replace the commented below lines by your desired values.  
_Note: please don't change the  non-commented lines due to unpredictable effects_

<pre>

{
  "api":{
      "server": "http://new.monitis.com",
      "version": "2"
  },
  "user_account":{
      "apiKey": "T5BAQQ46JPTGR6EBLFE28OSSQ",       - ApiKey that can be obtained from your Monitis accoun
      "secretKey": "248VUB2FA3DST8J31A9U6D9OHT"    - SecretKey that can be obtained from your Monitis account
  },  
  "debug": {
      "file": "./mon.csv",                         - Path to debug file
      "turn_on": no                                - Debug option (if "yes" the measuring results will be put 
                                                             into local csv file instead of send into Monitis)
  },
  "monitor":{
      "crl_uri": "http://public.wisekey.com/crl/", - CRLs location path (you can put here both the path 
                                                          to the list of crls as well as to concrete CRL) 
      "name": "CRL monitor",                       - Name of Monitor
      "tag": "CRL",                                - Tag of Monitor
      "type": "Java",                              - Type of Monitor
      "multivalue":true,
      "testDuration": "0",                         - Test duration in minutes (0 for infinite test)
      "processingTime": "1",                       - Perodicity of testing in minutes
      "params_separator": ":",
      "result_params":[
         {"format":"status:status::3"},
         {"format":"accessible:accesible::3"},
         {"format":"valid:valid::3"},
         {"format":"nextUpdate:update::3"},
         {"format":"name:name::3:true"}
      ],
      "additional_result_params":[
         {"format":"details:Details::3"}
      ]
  }
}

</pre>

That is all.  
You can now run executable Jar file by using the following command  

        java -jar crl_monitor.jar

Please note that we are presenting the sample approach only and naturally you can freely modify the code to reach your desired result.  
For more explanations you can look through article [Monitis CRLs validation](http://blog.monitis.com/index.php/2012/11/10/monitis-crls-validation/)


