package com.ontologycentral.ldspider.http;

import java.io.InputStream;
import java.net.URI;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.Callbacks;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.http.robot.Robots;
import com.ontologycentral.ldspider.queue.SpiderQueue;

public class LookupThread implements Runnable {
	Logger _log = Logger.getLogger(this.getClass().getSimpleName());

	SpiderQueue _q;
	Callback _content;
	Callback _links;
	FetchFilter _ff;
	
	Robots _robots;
	ErrorHandler _eh;
	ConnectionManager _hclient;

	public LookupThread(ConnectionManager hc, SpiderQueue q, Callback content, Callback links, Robots robots, ErrorHandler eh, FetchFilter ff) {
		_hclient = hc;
		_q = q;
		_content = content;
		_links = links;
		_robots = robots;
		_ff = ff;
		_eh = eh;
	}
	
	public void run() {
		_log.info("starting thread ...");

		URI lu = _q.poll();

		while (lu != null) {
			long time = System.currentTimeMillis();
			
//				URI lu = _q.obtainRedirect(u);

			long time1 = System.currentTimeMillis();
			long time2 = time1;
			long time3 = time1;
			long bytes = -1;
			int status = 0;
			String type = null;
			
			if (_robots.accessOk(lu)) {
				time2 = System.currentTimeMillis();

				HttpGet hget = new HttpGet(lu);
				hget.setHeaders(CrawlerConstants.HEADERS);
				
				try {
					HttpResponse hres = _hclient.connect(hget);

					HttpEntity hen = hres.getEntity();

					status = hres.getStatusLine().getStatusCode();

					Header ct = hres.getFirstHeader("Content-Type");
					if (ct != null) {
						type = hres.getFirstHeader("Content-Type").getValue();
					}
					
					_log.info("lookup on " + lu + " status " + status);

					// write headers in RDF
					Headers.processHeaders(lu, status, hres.getAllHeaders(), _content);

					if (status == HttpStatus.SC_OK) {				
						if (hen != null) {
							if (_ff.fetchOk(lu, status, hen) == true) {
								InputStream is = hen.getContent();

								Callbacks cbs = new Callbacks(new Callback[] { _content, _links } );
								RDFXMLParser rxp = new RDFXMLParser(is, true, true, lu.toString(), cbs);
								rxp = null;
							} else {
								_log.info("disallowed via fetch filter " + lu);
							}
						} else {
							_log.info("HttpEntity for " + lu + " is null");
						}
					} else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY || status == HttpStatus.SC_SEE_OTHER) { 
						// treating all redirects the same but shouldn't: 301 -> rename context URI, 302 -> keep original context URI, 303 -> spec inconclusive
						Header[] loc = hres.getHeaders("location");
						_log.info("redirecting (" + status + ") to " + loc[0].getValue());
						URI to = new URI(loc[0].getValue());

						// set redirect from original uri to new uri
						_q.setRedirect(lu, to, status);
						
						_eh.handleRedirect(lu, to, status);
					}

					if (hen != null) {
						bytes = hen.getContentLength();
					}
					
					hget.abort();
				} catch (Exception e) {
					hget.abort();
					_log.info("Exception " + e.getClass().getName());
					e.printStackTrace();
					_eh.handleError(lu, e);
				}
				
				time3 = System.currentTimeMillis();
				
				if (status != 0) {
					_eh.handleStatus(lu, status, type, (time3-time2), bytes);
				}
				
				_log.info(lu + " " + (time1-time) + " ms before lookup, " + (time2-time1) + " ms to check if lookup is ok, " + (time3-time2) + " ms for lookup");
			} else {
				_log.info("access denied per robots.txt for " + lu);
			}

			lu = _q.poll();
		}
	}
}