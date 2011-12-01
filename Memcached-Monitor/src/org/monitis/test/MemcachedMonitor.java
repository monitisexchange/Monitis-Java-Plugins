package org.monitis.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.monitis.GenericCustomMonitor.GenericCustomMonitorRunner;
import org.monitis.GenericCustomMonitor.IGenericCustomMonitor;
import org.monitis.beans.MonResult;
import org.monitis.beans.MonResultParameter;
import org.monitis.beans.MonitorParameter;
import org.monitis.enums.DataType;
import org.monitis.utils.TimeUtility;

public class MemcachedMonitor extends  IGenericCustomMonitor {
	
	private static final String apiKey = "2PE0HVI4DHP34JACKCAE37IOD4";		// <- replace by your API key (can be get from your Monitis account)
	private static final String secretKey = "7OI90FU3C3DA8ENLNJ0JGGOGO0";	// <- replace by your Secure key (can be get from your Monitis account)
    private static final String monitor_name = "Custom_monitor";			// <- replace by your desired monitor name
    private static final String monitor_tag_value = "Memcached_monitor";	// <- replace by your desired monitor Group
    private static String servers = "localhost:11211";						// <- replace by your monitored Memcached server connection parameters
    private static final long processingTime = 60;							//default value - 60 sec (1 min)
    private static final long testDuration = 600;							//default value - 600 sec (10 min)
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
		resultParams.add(new MonResultParameter("conn", "conns [%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("get_miss", "get_miss [%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("delete_miss", "delete_miss [%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("incr_miss", "incr_miss [%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("decr_miss", "decr_miss [%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("mem_usage", "mem_usage [%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("evictions", "evictions [%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("reqs", "reqs [per/s]", "", DataType.INTEGER));
		
		return resultParams;
	}

	public List<MonResult> get_results() {
		Map<InetSocketAddress,Map<String,String>> stats = null;
		int curr_connections, curr_connections_, maxconns;
		int curr_items, curr_items_;
		long get_hits, get_hits_;
		long get_misses, get_misses_;
		long limit_maxbytes, limit_maxbytes_;
		long delete_hits, delete_hits_;
		long delete_misses, delete_misses_;
		long incr_hits, incr_hits_;
		long incr_misses, incr_misses_;
		long decr_hits, decr_hits_;
		long decr_misses, decr_misses_;
		long evictions, evictions_;
		long bytes, bytes_;
//		int threads, threads_;
//		long bytes_read, bytes_read_;
//		long bytes_written, bytes_written_;
		long lres;
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
			get_hits = Long.parseLong(stat.get("get_hits")); /*Number of keys that have been requested and found present*/
			get_misses = Long.parseLong(stat.get("get_misses")); /*Number of items that have been requested and not found*/
			delete_hits = Long.parseLong(stat.get("delete_hits")); /*Number of keys that have been deleted and found present*/
			delete_misses = Long.parseLong(stat.get("delete_misses"));/*Number of items that have been deleted and not found*/
			incr_hits = Long.parseLong(stat.get("incr_hits")); /*Number of keys that have been incremented and found present*/
			incr_misses = Long.parseLong(stat.get("incr_misses"));/*Number of items that have been incremented and not found*/
			decr_hits = Long.parseLong(stat.get("decr_hits")); /*Number of keys that have been decremented and found present*/
			decr_misses = Long.parseLong(stat.get("decr_misses"));/*Number of items that have been decremented and not found*/
			limit_maxbytes = Long.parseLong(stat.get("limit_maxbytes")); /*Number of bytes this server is allowed to use for storage*/
			bytes = Long.parseLong(stat.get("bytes")); /*Current number of bytes used to store items*/
			curr_items = Integer.parseInt(stat.get("curr_items")); /*Current number of items stored*/
			evictions = Long.parseLong(stat.get("evictions"));/*Number of valid items removed from cache to free memory for new items*/
//			bytes_read = Long.parseLong(stat.get("bytes_read"));/*Total number of bytes read by this server from network*/
//			bytes_written = Long.parseLong(stat.get("bytes_written"));/*Total number of bytes sent by this server to network*/
//			threads = Integer.parseInt(stat.get("threads")); /*Number of worker threads requested*/
			
			//previous results
            if (stats_.size() <= 0) {
				curr_connections_ = /*threads_ =*/ curr_items_ = 0;
				get_hits_ = get_misses_ = limit_maxbytes_ = bytes_ = /*bytes_read_ = bytes_written_ =*/ evictions_ = 0;
            } else {
                Map<String, String> stat_ = stats_.get(s);
    			curr_connections_ = Integer.parseInt(stat_.get("curr_connections")); /*Number of open connections*/
    			get_hits_ = Long.parseLong(stat_.get("get_hits")); /*Number of keys that have been requested and found present*/
    			get_misses_ = Long.parseLong(stat_.get("get_misses")); /*Number of items that have been requested and not found*/
				delete_hits_ = Long.parseLong(stat_.get("delete_hits")); /*Number of keys that have been deleted and found present*/
				delete_misses_ = Long.parseLong(stat_.get("delete_misses"));/*Number of items that have been deleted and not found*/
				incr_hits_ = Long.parseLong(stat_.get("incr_hits")); /*Number of keys that have been incremented and found present*/
				incr_misses_ = Long.parseLong(stat_.get("incr_misses"));/*Number of items that have been incremented and not found*/
				decr_hits_ = Long.parseLong(stat_.get("decr_hits")); /*Number of keys that have been decremented and found present*/
				decr_misses_ = Long.parseLong(stat_.get("decr_misses"));/*Number of items that have been decremented and not found*/
    			limit_maxbytes_ = Long.parseLong(stat_.get("limit_maxbytes")); /*Number of bytes this server is allowed to use for storage*/
    			bytes_ = Long.parseLong(stat_.get("bytes")); /*Current number of bytes used to store items*/
    			curr_items_ = Integer.parseInt(stat_.get("curr_items")); /*Current number of items stored*/
    			evictions_ = Long.parseLong(stat_.get("evictions"));/*Number of valid items removed from cache to free memory for new items*/
//    			bytes_read_ = Long.parseLong(stat_.get("bytes_read"));/*Total number of bytes read by this server from network*/
//    			bytes_written_ = Long.parseLong(stat_.get("bytes_written"));/*Total number of bytes sent by this server to network*/
//    			threads_ = Integer.parseInt(stat_.get("threads")); /*Number of worker threads requested*/
            }
            
// 			Percent of open connections to max connections
			res = String.format("%3.1f", (curr_connections * 100.0) / maxconns);
			results.add(new MonResult("conn", res));
			buf.append("\tconn: ").append(res);
 			
//			Percent of items that have been requested and not found to total number of get commands 
			lres = (get_hits + get_misses);
			res = String.format("%3.1f", ((get_misses - get_misses_) * 100.0) / (lres<1?1:lres));
			results.add(new MonResult("get_miss", res));
			buf.append("\tget_miss: ").append(res);
			
//			Percent of items that have been requested to delete and not found to total number of delete commands
			lres = (delete_hits + delete_misses);
			res = String.format("%3.1f", (delete_misses * 100.0) / (lres<1?1:lres));
			results.add(new MonResult("delete_miss", res));
			buf.append("\tdelete_miss: ").append(res);

//			Percent of items that have been requested to increase and not found to total number of increase commands
			lres = (incr_hits + incr_misses);
			res = String.format("%3.1f", (incr_misses * 100.0) / (lres<1?1:lres));
			results.add(new MonResult("incr_miss", res));
			buf.append("\tincr_miss: ").append(res);
			
//			Percent of items that have been requested to decrease and not found to total number of decrease commands
			lres = (decr_hits + decr_misses);
			res = String.format("%3.1f", (decr_misses * 100.0) / (lres<1?1:lres));
			results.add(new MonResult("decr_miss", res));
			buf.append("\tdecr_miss: ").append(res);
			
//			Percent of current number of bytes used to store items to the max accessible bytes
			res = String.format("%3.1f", (bytes * 100.0) / limit_maxbytes);
			results.add(new MonResult("mem_usage", res));
			buf.append("\tmem_usage: ").append(res);
			
//			Percent of valid items removed from cache to free memory to current number of items stored
			res = String.format("%3.1f", (1.0 - (evictions / (curr_items < 1?1:curr_items))) * 100.0);
			results.add(new MonResult("evictions", res));
			buf.append("\tevictions: ").append(res);
			
 			//Total number of keys that have been requested per sec.
			res = String.valueOf(((get_hits +  get_misses)-(get_hits_ +  get_misses_)) / processingTime);
			results.add(new MonResult("reqs", res));
			buf.append("\treqs: ").append(res);
			
//          //Number of open connections
//			results.add(new MonResult("conns", String.valueOf(curr_connections)));
//			buf.append("\tconns: ").append(String.valueOf(curr_connections));
//						
//			//Number of keys that have been requested and found present per sec.
//			res = String.valueOf((get_hits - get_hits_) / processingTime);
//			results.add(new MonResult("get_hits", res));
// 			buf.append("\tget_hits: ").append(res);
//
//			//Number of items that have been requested and not found per sec.
//			res = String.valueOf((get_misses - get_misses_) / processingTime);
//			results.add(new MonResult("get_misses", res));
// 			buf.append("\tget_misses: ").append(res);
// 			
//			//Percent of number of keys that have been requested and found present to total number of keys that have been requested
//			res = String.format("%3.1f", (get_hits < 1?1: get_hits)  * 100.0 / ((get_hits +  get_misses) < 1? 1:(get_hits +  get_misses)));
//			results.add(new MonResult("hitratio", res));
//			buf.append("\thitratio: ").append(res);
//
//			//Current number of bytes used to store items [MB]
//			res = String.format("%5.1f", bytes / 1024.0/1024.0);
//			results.add(new MonResult("bytes", res));
// 			buf.append("\tbytes: ").append(res);
// 			
//			//Current number of items stored
//			results.add(new MonResult("curr_items", String.valueOf(curr_items)));
//			buf.append("\tcurr_items: ").append(String.valueOf(curr_items));
// 
//			//Total number of bytes read by this server from network per sec.
//			res = String.valueOf((bytes_read - bytes_read_) / processingTime);
//			results.add(new MonResult("bytes_read", res));
// 			buf.append("\tbytes_read: ").append(res);
// 			
// 			//Total number of bytes sent by this server to network per sec.
//			res = String.valueOf((bytes_written - bytes_written_) / processingTime);
//			results.add(new MonResult("bytes_written", res));
// 			buf.append("\tbytes_written: ").append(res);
// 			
// 			// Percent of valid items removed from cache to free memory to current number of items stored
//			res = String.format("%3.1f", evictions * 100.0 / (curr_items < 1?1:curr_items));
//			results.add(new MonResult("evictions", res));
//			buf.append("\tevictions: ").append(res);
//			
//			//Number of worker threads requested
//			results.add(new MonResult("threads", String.valueOf(threads)));
//			buf.append("\tthreads: ").append(String.valueOf(threads));
 
        }
        System.out.println(buf.toString());
        stats_.putAll(stats);
        
		try {
			String filename = "./ttt.txt";
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
			tst.runMonitor();
		} catch (Exception e) {
			System.err.println("Exception while testing custom monitor: "+ e.getMessage());
			System.exit(0);
		}		
	}

}
