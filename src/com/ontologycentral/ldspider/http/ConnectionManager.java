package com.ontologycentral.ldspider.http;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
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
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.http.internal.HttpRequestRetryHandler;
import com.ontologycentral.ldspider.http.internal.ResponseGzipUncompress;

public class ConnectionManager {

    private final static Logger _log = Logger.getLogger(ConnectionManager.class.getName());
    
    private DefaultHttpClient _client;

    
    public ConnectionManager(String proxyHost, int proxyPort, String puser, String ppassword, int connections) {
    	// general setup
    	SchemeRegistry supportedSchemes = new SchemeRegistry();

    	// Register the "http" and "https" protocol schemes, they are
    	// required by the default operator to look up socket factories.
    	supportedSchemes.register(new Scheme("http", 
    			PlainSocketFactory.getSocketFactory(), 80));
    	supportedSchemes.register(new Scheme("https", 
    			SSLSocketFactory.getSocketFactory(), 443));

    	// prepare parameters
    	HttpParams params = new BasicHttpParams();
    	HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    	HttpProtocolParams.setContentCharset(params, "UTF-8");
    	HttpProtocolParams.setUseExpectContinue(params, true);
    	
    	// we deal with redirects ourselves
    	HttpClientParams.setRedirecting(params, false);

    	//connection params 
    	params.setParameter(CoreConnectionPNames.SO_TIMEOUT, CrawlerConstants.SOCKET_TIMEOUT);
    	params.setParameter(CoreConnectionPNames.TCP_NODELAY, true);
    	params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CrawlerConstants.CONNECTION_TIMEOUT);

    	ConnManagerParams.setMaxTotalConnections(params, connections);
    	ClientConnectionManager cm = new ThreadSafeClientConnManager(params, supportedSchemes);

    	_client = new DefaultHttpClient(cm, params);
    	_client.addResponseInterceptor(new ResponseGzipUncompress());

    	// check if we have a proxy
    	if (proxyHost != null) {
    		HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
    		_client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    	}	
    }
    
    public void setRetries(int no) {
    	//set the retry handler
    	if (no > 0) {
    		HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler(no);
    		_client.setHttpRequestRetryHandler(retryHandler);
    	}
    }

    public void shutdown() {
    	_client.getConnectionManager().shutdown();

    }

    public HttpResponse connect(HttpGet get) throws ClientProtocolException, IOException {
    	return _client.execute(get);
    }
}