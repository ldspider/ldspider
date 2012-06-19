package com.ontologycentral.ldspider.http.robot;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

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
	
	public Robot(ConnectionManager cm, ErrorHandler eh, URI host) {
		URI robotsOnHost;
		try {
			robotsOnHost = new URI(host.getScheme(), host.getAuthority(), "/robots.txt", null, null);
		} catch (URISyntaxException e) {
			_log.fine(e.getMessage() + " " + host);
			return;
		}

		HttpGet hget = new HttpGet(robotsOnHost);

		long time1 = System.currentTimeMillis();
		long bytes = -1;
		int status = 0;
//		String type = null;

		Header[] headers = null;
		
		try {
			HttpResponse hres = cm.connect(hget);
			HttpEntity hen = hres.getEntity();

			status = hres.getStatusLine().getStatusCode();

			headers = hres.getAllHeaders();
			
//			Header ct = hres.getFirstHeader("Content-Type");
//			if (ct != null) {
//				type = hres.getFirstHeader("Content-Type").getValue();
//			}

			if (status == 200) {
				if (hen != null) {
					_nrc = new NoRobotClient(CrawlerConstants.USERAGENT_LINE);
					String content = EntityUtils.toString(hen);
					_log.finer(content);
					try {
						if (!((host.getPath() == null || host.getPath().equals(
								""))
								&& host.getQuery() == null && host
									.getFragment() == null))
							// If the URI host comes for whatever reason with
							// path, query, or fragment, strip it.
							_nrc.parse(
									content,
									(new URI(host.getScheme(), host
											.getAuthority(), null, null, null))
											.toURL());
						else
							_nrc.parse(content, host.toURL());
					} catch (NoRobotException e) {
						_log.info("no robots.txt for " + host);
					}
				} else {
					_nrc = null;
				}
			} else {
				_log.fine("no robots.txt for " + host);
				_nrc = null;
			}

			if (hen != null) {
				bytes = hen.getContentLength();
				hen.consumeContent();
			} else {
				hget.abort();
			}
		} catch (Exception e) {
			eh.handleError(robotsOnHost, e);
			hget.abort();			
		}

		if (status != 0) {
			eh.handleStatus(robotsOnHost, status, headers, (System.currentTimeMillis()-time1), bytes);
		}
	}

	public boolean isUrlAllowed(URL uri) {
		try{
			if (_nrc == null) {
				_log.fine("_nrc == null ");
    			return true;
    		}

    		return _nrc.isUrlAllowed(uri);
		} catch(Exception ex){
			ex.printStackTrace();
			return true;			
		}
    }
}