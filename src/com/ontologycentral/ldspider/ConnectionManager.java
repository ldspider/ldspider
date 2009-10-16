package com.ontologycentral.ldspider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.ontologycentral.ldspider.tld.TldManager;

/**
 * Connection Manager
 * @author juum [juergen@umbrich.net]
 *
 */
public class ConnectionManager {
	private final static Logger _log = Logger.getLogger("main class"); //this.getClass().getName());

	private static ClientConnectionManager _cm;
	private static HttpClient _hc; 

	private static void init(){
		if(_cm != null && _hc != null) return;

		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, CrawlerConstants.MAX_CONNECTIONS);

		HttpConnectionParams.setConnectionTimeout(params, CrawlerConstants.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, CrawlerConstants.SOCKET_TIMEOUT);

		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		if (System.getProperty("http.proxyHost") != null && System.getProperty("http.proxyPort") != null) {
			HttpHost proxy = new HttpHost(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
			params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);    	
		}

		HttpClientParams.setRedirecting(params, false);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		_cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		_hc = new DefaultHttpClient(_cm, params);

		_log.info("Initialised connection manager and HttpClient");
	}

	/**
	 * 
	 * @return - an instance of a DefaultHttpConnection 
	 */
	public static HttpClient getHttpClient() {
		init();

		return _hc;
	}

	/**
	 * Close all open connections and shutdown the ConnectionManager.
	 * Should be called at the end of your program to ensure a correct release and disconnect of all connections
	 */
	public static void shutdown(){
		//close expired connections
		_cm.closeExpiredConnections();

		//finally shut down the connection manager
		_cm.shutdown();
		_cm = null;

	}

	/**
	 * Sheila Kinsella's TLD extraction code. This code retrieves a list of know country and second-level country codes from the mozilla foundation 
	 * and creates a parser to extract top-level-domains (or pay-level-domains) for a given input {@link URI}.
	 * @param hc - HttpClient
	 * @return - an instance of a TldManager
	 */
	public static TldManager getTldManager() {
		TldManager tldm = null;
		HttpGet hget = null;

		try {
			URI tu = new URI("http://mxr.mozilla.org/mozilla-central/source/netwerk/dns/src/effective_tld_names.dat?raw=1");
			hget = new HttpGet(tu);
			HttpContext hcon = new BasicHttpContext();
			HttpResponse hres = getHttpClient().execute(hget, hcon);
			int status = hres.getStatusLine().getStatusCode();
			if (status == 200) {
				HttpEntity hen = hres.getEntity();
				if (hen != null) {
					tldm = new TldManager(hen.getContent());
				} else {
					_log.info("hen == null?");
					tldm = null;
				}
			} else {
				_log.info("status " + status + " for " + tu);
				tldm = null;
			}
		} catch (ClientProtocolException e) {
			_log.info("couldn't get PLDM " + e.getMessage());
			if (hget != null) {
				hget.abort();
			}
			tldm = null;
		} catch (IOException e) {
			_log.info("couldn't get PLDM " + e.getMessage());
			if (hget != null) {
				hget.abort();
			}
			tldm = null;
		} catch (URISyntaxException e) {
			_log.info("uri syntax exception " + e.getMessage());
		}
		return tldm;
	}
}
