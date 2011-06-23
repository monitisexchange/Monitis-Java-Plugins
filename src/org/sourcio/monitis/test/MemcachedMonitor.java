package org.sourcio.monitis.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.sourcio.monitis.Test.GenericCustomMonitorRunner;
import org.sourcio.monitis.Test.IGenericCustomMonitor;
import org.sourcio.monitis.beans.MonResult;
import org.sourcio.monitis.beans.MonResultParameter;
import org.sourcio.monitis.beans.MonitorParameter;
import org.sourcio.monitis.enums.DataType;
import org.sourcio.monitis.utils.TimeUtility;

public class MemcachedMonitor extends  IGenericCustomMonitor {
	
	private static final String apiKey = "4DURRI9JDEG5QN394DO14Q9C0D";
	private static final String secretKey = "1Q89OGT6BO95J4TH58A56S73T9";
    private static final String monitor_name = "Custom_monitor";//Name
    private static final String monitor_tag_value = "Memcached_monitor";//Monitor Group
    private static String servers = "localhost:11211";//local memcached server
    private static final long processingTime = 60;//default value - 60 sec (1 min)
    private static final long testDuration = 1000;//default value - 600 sec (10 min)
    private static Map<InetSocketAddress,Map<String,String>> settings = null;

	
    MemcachedClient cache = null;
    //storage of previous measured statistics
    Map<InetSocketAddress,Map<String,String>> stats_ = new HashMap<InetSocketAddress, Map<String, String>>();
    
    public MemcachedMonitor () throws Exception {
		cache = buildMemcachedClient(servers);
    }

    private MemcachedClient buildMemcachedClient(String servers) throws Exception {
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(servers));
		builder.setCommandFactory(new BinaryCommandFactory());// use binary protocol
		return builder.build();
    }

	public void signal_testEnded(int err_code){
		System.exit(err_code);
	}
	
	public String get_apiKey() {
		return apiKey;
	}
	
	public String get_secretKey() {
		return secretKey;
	}
	
	public String get_monitor_name() {
		return monitor_name;
	}
	
	public String get_monitor_tag_value() {
		return monitor_tag_value;
	}

	public long get_processingTime() {
		return processingTime * 1000;//ms

	}

	public long get_testDuration() {
		return testDuration * 1000;//ms
	}
	
	public boolean deleteMonitor() {
		return false;
	}
	
	public List<MonitorParameter> get_monitorParams() {
		return null;
	}

	public List<MonResultParameter> get_resultParams() {
		List<MonResultParameter> resultParams = new ArrayList<MonResultParameter>();
		resultParams.add(new MonResultParameter("server", "server", "", DataType.STRING));
		resultParams.add(new MonResultParameter("conns", "conns", "", DataType.INTEGER));
		resultParams.add(new MonResultParameter("conn", "conns [%]", "", DataType.FLOAT));
		resultParams.add(new MonResultParameter("get_hits", "gets [per/s]", "", DataType.INTEGER));
		resultParams.add(new MonResultParameter("get_misses", "misses [per/s]", "", DataType.INTEGER));
		resultParams.add(new MonResultParameter("reqs", "reqs [per/s]", "", DataType.INTEGER));
		resultParams.add(new MonResultParameter("hitratio", "hitratio [%]", "", DataType.FLOAT));
		resultParams.add(new MonResultParameter("bytes", "usage [MB]", "MB", DataType.INTEGER));
		resultParams.add(new MonResultParameter("usage", "usage [%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("curr_items", "items cached", "", DataType.INTEGER));
		resultParams.add(new MonResultParameter("bytes_read", "bytes read [per/s]", "", DataType.INTEGER));
		resultParams.add(new MonResultParameter("bytes_written", "bytes written [per/s]", "", DataType.INTEGER));
		resultParams.add(new MonResultParameter("evictions", "evictions [%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("threads", "threads", "", DataType.INTEGER));
		
		return resultParams;
	}

	public List<MonResult> get_results() {
		Map<InetSocketAddress,Map<String,String>> stats = null;
		int curr_connections, curr_connections_, maxconns;
		int threads, threads_;
		int curr_items, curr_items_;
		long get_hits, get_hits_;
		long get_misses, get_misses_;
		long limit_maxbytes, limit_maxbytes_;
		long bytes, bytes_;
		long bytes_read, bytes_read_;
		long bytes_written, bytes_written_;
		long evictions, evictions_;
		String res;
		
		try {
			if (settings == null) {// Get settings
				settings = cache.getStatsByItem("settings");
			}
			stats = cache.getStats();// Get current statistics
		}  catch (Exception e) {
			e.printStackTrace();
		}
		
        Iterator<InetSocketAddress> it = stats.keySet().iterator();
        StringBuffer buf = new StringBuffer();
		List<MonResult> results = new ArrayList<MonResult>();
		
        while (it.hasNext()) {
            SocketAddress s = it.next();
			results.add(new MonResult("server", s.toString()));
            buf.append("\n---- Statistics for server ").append(s.toString()).append("\n");
            buf.append(TimeUtility.format(TimeUtility.getNowByGMT().getTime(), "yyyy-MM-dd HH:mm:ss"));

			//Current results
            Map<String, String> stat = stats.get(s);
			curr_connections = Integer.parseInt(stat.get("curr_connections")); /*Number of open connections*/
			maxconns = Integer.parseInt(settings.get(s).get("maxconns")); /*Number of max connections*/
			threads = Integer.parseInt(stat.get("threads")); /*Number of worker threads requested*/
			get_hits = Long.parseLong(stat.get("get_hits")); /*Number of keys that have been requested and found present*/
			get_misses = Long.parseLong(stat.get("get_misses")); /*Number of items that have been requested and not found*/
			limit_maxbytes = Long.parseLong(stat.get("limit_maxbytes")); /*Number of bytes this server is allowed to use for storage*/
			bytes = Long.parseLong(stat.get("bytes")); /*Current number of bytes used to store items*/
			curr_items = Integer.parseInt(stat.get("curr_items")); /*Current number of items stored*/
			bytes_read = Long.parseLong(stat.get("bytes_read"));/*Total number of bytes read by this server from network*/
			bytes_written = Long.parseLong(stat.get("bytes_written"));/*Total number of bytes sent by this server to network*/
			evictions = Long.parseLong(stat.get("evictions"));/*Number of valid items removed from cache to free memory for new items*/
			
			//previous results
            if (stats_.size() <= 0) {
				curr_connections_ = threads_ = curr_items_ = 0;
				get_hits_ = get_misses_ = limit_maxbytes_ = bytes_ = bytes_read_ = bytes_written_ = evictions_ = 0;
            } else {
                Map<String, String> stat_ = stats_.get(s);
    			curr_connections_ = Integer.parseInt(stat_.get("curr_connections")); /*Number of open connections*/
    			threads_ = Integer.parseInt(stat_.get("threads")); /*Number of worker threads requested*/
    			get_hits_ = Long.parseLong(stat_.get("get_hits")); /*Number of keys that have been requested and found present*/
    			get_misses_ = Long.parseLong(stat_.get("get_misses")); /*Number of items that have been requested and not found*/
    			limit_maxbytes_ = Long.parseLong(stat_.get("limit_maxbytes")); /*Number of bytes this server is allowed to use for storage*/
    			bytes_ = Long.parseLong(stat_.get("bytes")); /*Current number of bytes used to store items*/
    			curr_items_ = Integer.parseInt(stat_.get("curr_items")); /*Current number of items stored*/
    			bytes_read_ = Long.parseLong(stat_.get("bytes_read"));/*Total number of bytes read by this server from network*/
    			bytes_written_ = Long.parseLong(stat_.get("bytes_written"));/*Total number of bytes sent by this server to network*/
    			evictions_ = Long.parseLong(stat_.get("evictions"));/*Number of valid items removed from cache to free memory for new items*/
            }
            
            //Number of open connections
			results.add(new MonResult("conns", String.valueOf(curr_connections)));
			buf.append("\tconns: ").append(String.valueOf(curr_connections));
			
 			// Percent of open connections to max connections
			res = String.format("%3.1f", (curr_connections * 100.0) / maxconns);
			results.add(new MonResult("conn", res));
			buf.append("\tconn: ").append(res);
 			
			//Number of keys that have been requested and found present per sec.
			res = String.valueOf((get_hits - get_hits_) / processingTime);
			results.add(new MonResult("get_hits", res));
 			buf.append("\tget_hits: ").append(res);
 			
 			//Number of items that have been requested and not found per sec.
			res = String.valueOf((get_misses - get_misses_) / processingTime);
			results.add(new MonResult("get_misses", res));
 			buf.append("\tget_misses: ").append(res);
 			
 			//Total number of keys that have been requested per sec.
			res = String.valueOf(((get_hits +  get_misses)-(get_hits_ +  get_misses_)) / processingTime);
			results.add(new MonResult("reqs", res));
			buf.append("\treqs: ").append(res);
			
			//Percent of number of keys that have been requested and found present to total number of keys that have been requested
			res = String.format("%3.1f", (get_hits < 1?1: get_hits)  * 100.0 / ((get_hits +  get_misses) < 1? 1:(get_hits +  get_misses)));
			results.add(new MonResult("hitratio", res));
			buf.append("\thitratio: ").append(res);

			//Current number of bytes used to store items [MB]
			res = String.format("%5.1f", bytes / 1024.0/1024.0);
			results.add(new MonResult("bytes", res));
 			buf.append("\tbytes: ").append(res);
 			
			//Current number of items stored
			results.add(new MonResult("curr_items", String.valueOf(curr_items)));
			buf.append("\tcurr_items: ").append(String.valueOf(curr_items));
 
			//Total number of bytes read by this server from network per sec.
			res = String.valueOf((bytes_read - bytes_read_) / processingTime);
			results.add(new MonResult("bytes_read", res));
 			buf.append("\tbytes_read: ").append(res);
 			
 			//Total number of bytes sent by this server to network per sec.
			res = String.valueOf((bytes_written - bytes_written_) / processingTime);
			results.add(new MonResult("bytes_written", res));
 			buf.append("\tbytes_written: ").append(res);
 			
 			// Percent of valid items removed from cache to free memory to current number of items stored
			res = String.format("%3.1f", evictions * 100.0 / (curr_items < 1?1:curr_items));
			results.add(new MonResult("evictions", res));
			buf.append("\tevictions: ").append(res);
			
			//Number of worker threads requested
			results.add(new MonResult("threads", String.valueOf(threads)));
			buf.append("\tthreads: ").append(String.valueOf(threads));
 
        }
        System.out.println(buf.toString());
        stats_.putAll(stats);
        
		try {
			String filename = "/home/shunanya/SVN_projects/Monitis/monitis_new_approach/trunk/QueueTest/ttt.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(filename, true));
			out.write(buf.toString());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return results;
	}

	public static void main(String[] args) {
		try {
			GenericCustomMonitorRunner tst = new GenericCustomMonitorRunner(new MemcachedMonitor());
		} catch (Exception e) {
			System.err.println("Exception while test custom monitor: "+ e.getMessage());
			System.exit(0);
		}		
	}

}
