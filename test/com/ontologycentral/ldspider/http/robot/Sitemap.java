package com.ontologycentral.ldspider.http.robot;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;


import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.http.ConnectionManager;

/**
 * 
 * @author andhar
 *
 */
		
public class Sitemap {
	Logger _log = Logger.getLogger(this.getClass().getName());
	
	com.ontologycentral.ldspider.http.robot.Sitemap _sm;
	
	public Sitemap(ConnectionManager cm, ErrorHandler eh, String host) {
		URI u;
		try {
			u = new URI( "http://" + host + "/sitemap.xml" );
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
					_sm = new com.ontologycentral.ldspider.http.robot.Sitemap(u.toURL());
					String content = EntityUtils.toString(hen);
					_log.fine(content);
					SitemapParser sp = new SitemapParser();
					sp.processSitemap(_sm, type, content);
				} else {
					_sm = null;
				}
			} else {
				_log.info("no sitemap.xml for " + host);
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

	public List<URI> getURIs() {
		List<URI> li = new ArrayList<URI>();
		
		if (_sm != null) {
			Collection<SitemapUrl> col = _sm.getUrlList();
			for (SitemapUrl su : col) {
				URI u;
				try {
					u = su.getUrl().toURI();
					li.add(u);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    	}
		
		return li;
    }
}