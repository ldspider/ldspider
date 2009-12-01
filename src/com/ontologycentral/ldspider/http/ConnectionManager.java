package com.ontologycentral.ldspider.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
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
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.ontologycentral.ldspider.tld.TldManager;

public class ConnectionManager {

    private final static Logger _log = Logger.getLogger(ConnectionManager.class.getName());
    
    private HttpHost _proxy = null;
    private int MAX_CONNECTION = 100;
    private DefaultHttpClient _client;
    private Integer SOCKET_TIMEOUT = 500;
    private Integer CONNECTION_TIMEOUT = 1000;
    private HttpRequestRetryHandler _retryHandler;

    
    public ConnectionManager(String proxyHost, int proxyPort) {

//	String proxyHost = "localhost";
//	int proxyPort = 3128;
	    
	//check if we have a proxy
	if(proxyHost != null)
	    _proxy = new HttpHost(proxyHost, proxyPort, "http");
	
	//geeneral setup
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

        //connection params 
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, SOCKET_TIMEOUT);
        params.setParameter(CoreConnectionPNames.TCP_NODELAY, true);
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        
        ConnManagerParams.setMaxTotalConnections(params, MAX_CONNECTION);
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, supportedSchemes);
        
        _client = new DefaultHttpClient(cm, params);
        _client.addRequestInterceptor(new RequestAcceptEncoding());
        _client.addResponseInterceptor(new ResponseGzipUncompress());
        
        _client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, _proxy);
        
       //set the retry handler
        _retryHandler = new HttpRequestRetryHandler();
        _client.setHttpRequestRetryHandler(_retryHandler);
        
                
    }
    
    public HttpResponse connect(URI uri) throws ClientProtocolException, IOException{
	HttpGet get = new HttpGet(uri);
	
	return _client.execute(get);
    }
    
    
    public void shutdow() {
	_client.getConnectionManager().shutdown();

    }

    public HttpResponse connect(URI lu, Header[] headers) throws ClientProtocolException, IOException {
	HttpGet get = new HttpGet(lu);
	get.setHeaders(headers);
	
	
	return _client.execute(get);
    }

    public HttpResponse connect(HttpGet get) throws ClientProtocolException, IOException {
	return _client.execute(get);
    }
}