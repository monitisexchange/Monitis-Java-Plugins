## The Java Memcached monitor

The project presents the implementation of Monitis Custom Monitor that has aim to evaluate health state of the Memcached server.  
It is fully implemented on Java and use Monitis Java SDK that wraps the Monitis Open API functionality.

#### The Repository contains the following ####

        src
          org/.../test
            MemcachedMonitor.java    Memcached cusom monitor implementation
        lib
          m_api.jar                  Truncated version of Monitis Java SDK for Monitis Open API  
                                        (contains only the custom monitor functionality part)
          xmemcached-1.3.2.jar       Java client for Memcached
          .....                      Other necessary libraries
        properties
          monitor.config             Monitor configuration file in JSON format

#### The Memcached monitor measures the following metrics ####

  - Percent of open connections to max connections   
    (conn = curr_connections / maxconns)
  - Percent of items that have been requested and not found to total number of get commands  
    (get_miss = get_misses / (get_hits + get_misses))
  - Percent of items that have been requested to delete and not found to total number of delete commands  
    (delete_miss = delete_misses / (delete_hits + delete_misses))
  - Percent of items that have been requested to increase and not found to total number of increase commands  
    (incr_miss = incr_misses / (incr_hits + incr_misses))
  - Percent of items that have been requested to decrease and not found to total number of decrease commands  
    (decr_miss = decr_misses / (decr_hits + decr_misses))
  - Percent of current number of bytes used to store items to the max accessible bytes  
    (mem_usage = bytes / limit_maxbytes)
  - Percent of valid items removed from cache to free memory to current number of items stored  
    (evictions = (1 - evictions / curr_items))

If you want to test it,  
you have to have firstly the account in the Monitis,   
next you should put some parameters in the monitor.conf configuration file  
or replace them by your desired values.

#### Monitor configuration ####

The _monitor.config_ file is used to configure Monitor. It should be prepared in JSON form and has the following structure.

   <pre>

	{
	  "api":{
	      "server": "http://monitis.com",                   <- Monitis server URL that support Monitis Open API <i>(optional; the default value - http://monitis.com)</i>
	      "version": "2"                                    <- Open API version <i>(optional; the default value - 2)</i>
	  },
	  "user_account":{
	      "apiKey": "T5BAQQ46JPTGR6EBLFE28OSSQ",            <- The personal API key that can be obtained from Monitis user account <b>(mandatory)</b>
	      "secretKey": "248VUB2FA3DST8J31A9U6D9OHT"         <- The personal secret key that can be obtained from Monitis user account <b>(mandatory)</b>
	  },  
	  "monitor":{
	  		"memcached_server": "127.0.0.1:11211",       <- Memcached server access URL <i>(optional; the default value - "localhost:11211")</i>
			"name": "Memcached_10.137.25.186-127.0.0.1:11211",   <- The name for Monitor to be register <b>(mandatory)</b>
			"tag": "memcached",                          <- The tag for Monitor to be register <b>(mandatory)</b>
			"type": "Java",                              <- The type for Monitor to be register <b>(mandatory)</b>
			"testDuration": "0",                         <- The duration of monitoring [min] (0 - infinitely) <i>(optional; the default value - 10)</i>
			"processingTime": "1",                       <- The periodicity of sending measuring data into Monitis [min] <i>(optional; the default value - 1)</i>
			"params_separator": ":",                     <- The separator used for separation result/additional_result params definition parts
			"result_params":[                            <- The array of definitions for send parameters into Monitis (each element of array is definition in form required by Monitis Open API)
				"status:status::3",                  <- The definition of parameters for monitoring
				"conn:conns:prc:4",
				"get_miss:get_miss:prc:4",
				"delete_miss:delete_miss:prc:4",
				"incr_miss:incr_miss:prc:4",
				"decr_miss:decr_miss:prc:4",
				"mem_usage:mem_usage:prc:4",
				"evictions:evictions:prc:4",
				"in_kbps:in_kbps::4",
				"out_kbps:out_kbps::4",
				"reqs:reqs:per/s:2",
				"uptime:uptime::3"
			],
			"additional_result_params":[                 <- The array of definitions for send aditional parameters into Monitis (each element of array is definition in form required by Monitis Open API)
				"details:Details::3"
			]
	  }
	}

   </pre>

