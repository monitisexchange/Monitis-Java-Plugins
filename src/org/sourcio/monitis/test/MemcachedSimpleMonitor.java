package org.sourcio.monitis.test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
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

public class MemcachedSimpleMonitor extends  IGenericCustomMonitor {
	
	private static final String apiKey = "4DURRI9JDEG5QN394DO14Q9C0D";
	private static final String secretKey = "1Q89OGT6BO95J4TH58A56S73T9";
    private static final String monitor_name = "Memcahed_monitor";
    private static final String monitor_tag_value = "Custom_memcached_monitor";
    private static String servers = "localhost:11211";//local memcached server
	
    MemcachedClient cache = null;
    
    public MemcachedSimpleMonitor () throws Exception {
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
		return 60000; // 1 min

	}

	public long get_testDuration() {
		return 600000;// 10 min
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
		resultParams.add(new MonResultParameter("reqs", "reqs", "", DataType.INTEGER));
		resultParams.add(new MonResultParameter("hitratio", "hitratio[%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("conn", "conn[%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("storage", "storage[%]", "%", DataType.FLOAT));
		resultParams.add(new MonResultParameter("items", "items", "", DataType.INTEGER));
		
		return resultParams;
	}

	public List<MonResult> get_results() {
		Map<InetSocketAddress,Map<String,String>> stats = null;
		try {
			stats = cache.getStats();
		}  catch (Exception e) {
			e.printStackTrace();
		}
		
        Iterator<InetSocketAddress> it = stats.keySet().iterator();
        StringBuffer buf = new StringBuffer();
		List<MonResult> results = new ArrayList<MonResult>();
        while (it.hasNext()) {
            SocketAddress s = it.next();
			results.add(new MonResult("server", s.toString()));
            buf.append("---- Statistics for server ").append(s.toString()).append("\n");
			
            Map<String, String> stat = stats.get(s);
			long get_hits = Long.parseLong(stat.get("get_hits")); /*Number of keys that have been requested and found present*/
			long get_misses = Long.parseLong(stat.get("get_misses")); /*Number of items that have been requested and not found*/
			int curr_connections = Integer.parseInt(stat.get("curr_connections")); /*Number of open connections*/
			long bytes = Long.parseLong(stat.get("bytes")); /*Current number of bytes used to store items*/
			long limit_maxbytes = Long.parseLong(stat.get("limit_maxbytes")); /*Number of bytes this server is allowed to use for storage*/
			int curr_items = Integer.parseInt(stat.get("curr_items")); /*Current number of items stored*/
			
			long reqs = get_hits +  get_misses;
			results.add(new MonResult("reqs", String.valueOf(reqs)));
			buf.append("\treqs: ").append(String.valueOf(reqs));
			
			reqs = reqs < 1? 1:reqs;
			get_hits = get_hits < 1?1: get_hits;
			String hitratio = String.format("%3.1f", get_hits  * 100.0 / reqs);
			results.add(new MonResult("hitratio", hitratio));
			buf.append("\thitratio: ").append(hitratio);

			String conn = String.format("%3.1f", curr_connections * 100.0 / 1024);
			results.add(new MonResult("conn", conn));
			buf.append("\tconn: ").append(conn);

			String storage = String.format("%3.1f", bytes * 100.0 / limit_maxbytes);
			results.add(new MonResult("storage", storage));
			buf.append("\tstorage: ").append(storage);

			results.add(new MonResult("items", String.valueOf(curr_items)));
 			buf.append("\titems: ").append(String.valueOf(curr_items));
        }
        System.out.println(buf.toString());

		return results;
	}

	public static void main(String[] args) {
		try {
			GenericCustomMonitorRunner tst = new GenericCustomMonitorRunner(new MemcachedSimpleMonitor());
		} catch (Exception e) {
			System.err.println("Exception while test custom monitor: "+ e.getMessage());
			System.exit(0);
		}		
	}

}
