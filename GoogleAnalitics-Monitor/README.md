#### The Google Analitics Monitor

This repository contains the implementation of Monitis Custom monitor that is intended to monitor your Google Analitics Account data.  
It is fully implemented on Java and use [Monitis Java SDK](https://github.com/monitisexchange/Monitis-Java-SDK) that wraps the latest version (3.0) Monitis Open API functionality.

The Repository contains the following

        src
          org.monitis.google
            AnalyticsClient.java    Google analitics cusom monitor implementation
            GoogleMonitor.java      Wrapper for Monitis SDK
            GoogleMonitorTest.java  Sample test
        lib
          monitis-java-sdk.jar       The latest (3.0) version of Monitis Java SDK for Monitis Open API  
          gdata-analytics-2.1.jar    Google analitics support library
          gdata-core-1.0.jar	     Google core data support library

 The lib folder contains no other necessary libraries that is depend on monitis-java-sdk.jar. They can be taken from [Monitis Java SDK](https://github.com/monitisexchange/Monitis-Java-SDK)


If you want to test it,  
you have to have firstly the account in the Monitis,   
next you should put some constants in the GoogleMonitorTest.java (12 - 17 lines)  
and replace them by your desired values.  


        apikey         // ApiKey that can be obtained from your Monitis account e.g. T5BAQQ46JPTGR6EBLFE28OSSQ"
        secretkey      // SecretKey that can be obtained from your Monitis account e.g. 248VUB2FA3DST8J31A9U6D9OHT
        username       // google account user name e.g. "mygoogle@gmail.com"
        password       // google account password
        accountname    // google account name e.g "www.mysite.us"
        profilename    // google account profile name e.g. "*.www.mysite.us (Master Profile)"

That is all.  

Please note that we are presenting the sample approach only and naturally you can freely modify the code to reach your desired result.  
For more explanations you can look through article [Google Analytics With Monitis Dashboard](http://blog.monitis.com/index.php/2012/02/23/google-analytics-with-monitis-dashboard/)


