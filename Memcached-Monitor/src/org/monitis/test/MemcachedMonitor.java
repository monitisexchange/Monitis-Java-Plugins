package org.monitis.test;

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

import org.json.JSONArray;
import org.monitis.GenericCustomMonitor.GenericCustomMonitorRunner;
import org.monitis.GenericCustomMonitor.IGenericCustomMonitorWrapper;
import org.monitis.beans.MonResult;
import org.monitis.utils.MConfig;
import org.monitis.utils.TimeUtility;
import org.monitis.utils.Utils;

public class MemcachedMonitor extends  IGenericCustomMonitorWrapper {
	
    MemcachedClient cache = null;
	private static String servers;
	private static Map<InetSocketAddress,Map<String,String>> settings = null;
	private static String additionalResult = "details";
	private static List<String> status = null;
    //storage of previous measured statistics
    Map<InetSocketAddress,Map<String,String>> stats_ = new HashMap<InetSocketAddress, Map<String, String>>();
    
    public MemcachedMonitor () throws Exception {
    	MConfig conf = MConfig.getConfig("/properties/api_config.json");
    	servers = conf.getConfigStringValue("monitor.memcached_server", "localhost:11211");
		cache = buildMemcachedClient(servers);
		System.out.println("cache = "+cache);
    }

    private MemcachedClient buildMemcachedClient(String servers) throws Exception {
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(servers));
		builder.setCommandFactory(new BinaryCommandFactory());// use binary protocol
		return builder.build();
    }

    @Override
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
		long bytes_read, bytes_read_;
		long bytes_written, bytes_written_;
		long uptime;
		long lres;
		long duration = get_processingTime()/1000; //[sec]
		String res;
		double dres; 
		
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
		status = new ArrayList<String>();
		
        while (it.hasNext()) {
            SocketAddress s = it.next();
//			results.add(new MonResult("server", s.toString()));
            buf.append("\n---- Statistics for server ").append(s.toString()).append("\n");
            buf.append(TimeUtility.format(TimeUtility.getNowByGMT().getTime(), "yyyy-MM-dd HH:mm:ss"));

			//Current results
            Map<String, String> stat = stats.get(s);
            uptime = Long.parseLong(stat.get("uptime"));//
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
			bytes_read = Long.parseLong(stat.get("bytes_read"));/*Total number of bytes read by this server from network*/
			bytes_written = Long.parseLong(stat.get("bytes_written"));/*Total number of bytes sent by this server to network*/
//			threads = Integer.parseInt(stat.get("threads")); /*Number of worker threads requested*/
			
			//previous results
            if (stats_.size() <= 0) {
				curr_connections_ = /*threads_ =*/ curr_items_ = 0;
				get_hits_ = get_misses_ = limit_maxbytes_ = bytes_ = bytes_read_ = bytes_written_ = evictions_ = 0;
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
    			bytes_read_ = Long.parseLong(stat_.get("bytes_read"));/*Total number of bytes read by this server from network*/
    			bytes_written_ = Long.parseLong(stat_.get("bytes_written"));/*Total number of bytes sent by this server to network*/
//    			threads_ = Integer.parseInt(stat_.get("threads")); /*Number of worker threads requested*/
            }
            
// 			Percent of open connections to max connections
            dres = (curr_connections * 100.0) / maxconns;
			res = String.format("%3.1f", dres);
			results.add(new MonResult("conn", res));
			if (dres > 95){
				status.add(additionalResult+":"+"WARN - The number of connections reached to maximum allowed");
			}
			buf.append("\tconn: ").append(res);
 			
//			Percent of items that have been requested and not found to total number of get commands 
			lres = (get_hits + get_misses);
			dres = ((get_misses - get_misses_) * 100.0) / (lres<1?1:lres);
			res = String.format("%3.1f", dres);
			results.add(new MonResult("get_miss", res));
			if (dres > 5){
				status.add(additionalResult+":"+"WARN - too many requested to get but non-found (lost) records");
			}
			buf.append("\tget_miss: ").append(res);
			
//			Percent of items that have been requested to delete and not found to total number of delete commands
			lres = (delete_hits + delete_misses);
			dres = (delete_misses * 100.0) / (lres<1?1:lres);
			res = String.format("%3.1f", dres);
			results.add(new MonResult("delete_miss", res));
			if (dres > 10){
				status.add(additionalResult+":"+"WARN - too many requested to delete but already deleted records");
			}
			buf.append("\tdelete_miss: ").append(res);

//			Percent of items that have been requested to increase and not found to total number of increase commands
			lres = (incr_hits + incr_misses);
			dres = (incr_misses * 100.0) / (lres<1?1:lres);
			res = String.format("%3.1f", dres);
			results.add(new MonResult("incr_miss", res));
			if (dres > 5){
				status.add(additionalResult+":"+"WARN - too many requested to increase but non-found (lost) records");
			}			
			buf.append("\tincr_miss: ").append(res);
			
//			Percent of items that have been requested to decrease and not found to total number of decrease commands
			lres = (decr_hits + decr_misses);
			dres = (decr_misses * 100.0) / (lres<1?1:lres);
			res = String.format("%3.1f", dres);
			results.add(new MonResult("decr_miss", res));
			if (dres > 5){
				status.add(additionalResult+":"+"WARN - too many requested to decrease but non-found (lost) records");
			}			
			buf.append("\tdecr_miss: ").append(res);
			
//			Percent of current number of bytes used to store items to the max accessible bytes
			dres = (bytes * 100.0) / limit_maxbytes;
			res = String.format("%3.1f", dres);
			results.add(new MonResult("mem_usage", res));
			if (dres > 95){
				status.add(additionalResult+":"+"WARN - the memory usage reached to critical point");
			}			
			buf.append("\tmem_usage: ").append(res);
			
//			Percent of valid items removed from cache to free memory to current number of items stored
			dres = (100.0 *(evictions - evictions_) / (curr_items < 1?1:curr_items));
			res = String.format("%3.1f", dres);
			results.add(new MonResult("evictions", res));
			if (dres > 5){
				status.add(additionalResult+":"+"WARN - Perhaps, the memory limit is to small");
			}			
			buf.append("\tevictions: ").append(res);
			
// 			Total number of keys that have been requested per sec.
			res = String.valueOf(((get_hits +  get_misses)-(get_hits_ +  get_misses_)) / duration);
			results.add(new MonResult("reqs", res));
			buf.append("\treqs: ").append(res);
			
// 			outbound written KB per sec
			res = String.valueOf((bytes_written - bytes_written_) / duration / 1000);
			results.add(new MonResult("out_kbps", res));
			buf.append("\tout_kbps: ").append(res);
			
// 			inbound written KB per sec
			res = String.valueOf((bytes_read - bytes_read_) / duration / 1000);
			results.add(new MonResult("in_kbps", res));
			buf.append("\tin_kbps: ").append(res);

// 			Memcached Uptime
			res = Utils.formatTimestamp(uptime);
			results.add(new MonResult("uptime", res));
			
			if (status == null || status.size() <= 0){
				results.add(new MonResult("status", "OK"));
			} else {
				results.add(new MonResult("status", "NOK"));				
			}
        }
        System.out.println(buf.toString());
        stats_.putAll(stats);
        
//		try {
//			String filename = "./ttt.txt";
//			BufferedWriter out = new BufferedWriter(new FileWriter(filename, true));
//			out.write(buf.toString());
//			out.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		return results;
	}

	@Override
	public JSONArray get_additionalResults() {
		JSONArray jarr = null;
		if (status != null && status.size() > 0){
			jarr = new JSONArray(status);
		} 
		return jarr;
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
