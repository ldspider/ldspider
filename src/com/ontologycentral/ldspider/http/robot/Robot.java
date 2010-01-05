package com.ontologycentral.ldspider.http.robot;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.osjava.norbert.NoRobotClient;
import org.osjava.norbert.NoRobotException;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.http.ConnectionManager;

/**
 * 
 * @author andhar
 *
 */
		
public class Robot {
	Logger _log = Logger.getLogger(this.getClass().getName());

	NoRobotClient _nrc = null;
	
	public Robot(ConnectionManager cm, ErrorHandler eh, String host) {
		URI u;
		try {
			u = new URI( "http://" + host + "/robots.txt" );
		} catch (URISyntaxException e) {
			_log.info(e.getMessage() + " " + host);
			return;
		}

		HttpGet hget = new HttpGet(u);

		long time1 = System.currentTimeMillis();
		long bytes = -1;
		int status = 0;
		String type = null;

		try {
			HttpResponse hres = cm.connect(hget);
			HttpEntity hen = hres.getEntity();

			status = hres.getStatusLine().getStatusCode();
			
			Header ct = hres.getFirstHeader("Content-Type");
			if (ct != null) {
				type = hres.getFirstHeader("Content-Type").getValue();
			}

			if (status == HttpStatus.SC_OK) {
				if (hen != null) {
					_nrc = new NoRobotClient(CrawlerConstants.USERAGENT);
					String content = EntityUtils.toString(hen);
					_log.fine(content);
					try {
						_nrc.parse(content, new URL("http://" + host + "/"));
					} catch (NoRobotException e) {
						_log.info("no robots.txt for " + host);
					}
				} else {
					_nrc = null;
				}
			} else {
				_log.info("no robots.txt for " + host);
				_nrc = null;
			}

			if (hen != null) {
				bytes = hen.getContentLength();
				hen.consumeContent();
			} else {
				hget.abort();
			}
		} catch (Exception e) {
			eh.handleError(u, e);
			hget.abort();			
		}

		if (status != 0) {
			eh.handleStatus(u, status, type, (System.currentTimeMillis()-time1), bytes);
		}
	}

	public boolean isUrlAllowed(URL uri) {
		if (_nrc == null) {
    		return true;
    	}

    	return _nrc.isUrlAllowed(uri);
    }
}