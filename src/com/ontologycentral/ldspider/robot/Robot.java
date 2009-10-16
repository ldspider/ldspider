package com.ontologycentral.ldspider.robot;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.osjava.norbert.NoRobotClient;
import org.osjava.norbert.NoRobotException;

import com.ontologycentral.ldspider.CrawlerConstants;

/**
 * 
 * @author andhar
 *
 */
		
public class Robot {
	Logger _log = Logger.getLogger(this.getClass().getName());

	NoRobotClient _nrc = null;
	
	public Robot(HttpClient hc, String host) {
    	try {
    		URI u = new URI( "http://" + host + "/robots.txt" );

    		HttpGet hget = new HttpGet(u);
			HttpContext hcon = new BasicHttpContext();
			HttpResponse hres = hc.execute(hget, hcon);
			int status = hres.getStatusLine().getStatusCode();
			if (status == 200) {
				HttpEntity hen = hres.getEntity();
				if (hen != null) {
					_nrc = new NoRobotClient(CrawlerConstants.USERAGENT);
					String content = EntityUtils.toString(hen);
					_log.fine(content);
					_nrc.parse(content, new URL("http://" + host + "/"));
				} else {
					_nrc = null;
				}
			} else {
				_log.info("no robots.txt for " + host);
				_nrc = null;
			}
		} catch (NoRobotException e) {
			_log.fine(e.getMessage());
			_nrc = null;
		} catch (URISyntaxException e) {
			_log.fine(e.getMessage());
			_nrc = null;
		} catch (ClientProtocolException e) {
			_log.fine(e.getMessage());
			_nrc = null;
		} catch (IOException e) {
			_log.fine(e.getMessage());
			_nrc = null;
		}
	}
	
    public boolean isUrlAllowed(URL uri) {
    	if (_nrc == null) {
    		return true;
    	}

    	return _nrc.isUrlAllowed(uri);
    }
}