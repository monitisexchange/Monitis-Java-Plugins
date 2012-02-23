package org.monitis.google;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import org.monitis.exception.MonitisException;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.AccountEntry;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class AnalyticsClient {
	
	private String tableid;
	public static int MAXACCOUNTS = 50;

	private AnalyticsService analyticsService;
	public AnalyticsClient(String username, String password, String accountname, String profilename) throws MonitisException {
		analyticsService = new AnalyticsService("GoogleMonitor");
		try {
			analyticsService.setUserCredentials(username, password);
			URL queryUrl;
			queryUrl = new URL(
				    "https://www.google.com/analytics/feeds/accounts/default?max-results=" + MAXACCOUNTS);
			// Make request to the API, using AccountFeed class as the second parameter.
			AccountFeed accountFeed;
			accountFeed = analyticsService.getFeed(queryUrl, AccountFeed.class);
			for (AccountEntry entry : accountFeed.getEntries()) {
				if(entry.getProperty("ga:accountName").equals(accountname) && 
						entry.getTitle().getPlainText().equals(profilename))
					tableid = entry.getTableId().getValue();
				
			}
		//analyticsService.
		} catch (AuthenticationException e) {
			throw new MonitisException(e, e.getMessage());
		} catch(ServiceException e) {
			throw new MonitisException(e, e.getMessage());
		} catch(MalformedURLException e) {
			throw new MonitisException(e, e.getMessage());
		} catch(IOException e) {
			throw new MonitisException(e, e.getMessage());
		} 
	}
	
	public String[] getValues(String startdate, String enddate, String metrics) throws MonitisException {
		if (!tableid.isEmpty()) {
			// Create a query using the DataQuery Object.
			  DataQuery query;
			try {
				query = new DataQuery(new URL(
				      "https://www.google.com/analytics/feeds/data"));
			} catch (MalformedURLException e) {
				throw new MonitisException(e, e.getMessage());
			}
			  query.setStartDate(startdate);
			  query.setEndDate(enddate);
			  query.setMetrics(metrics);
			  query.setIds(tableid);
			  try {
				DataFeed dataFeed = analyticsService.getFeed(query.getUrl(), DataFeed.class);
				DataEntry entry = dataFeed.getEntries().get(0);
				String[] metric = metrics.split(",");
				String[] results = new String[metric.length];
				for(int i= 0; i < metric.length; i++) {
					results[i] = entry.stringValueOf(metric[i]);
				}
				return results;
			
			} catch (ServiceException e) {
				throw new MonitisException(e, e.getMessage());
			} catch (IOException e) {
				throw new MonitisException(e, e.getMessage());
			}
		}
		return null;
	}
}
