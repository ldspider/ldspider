package com.ontologycentral.ldspider.lookup;

import java.io.InputStream;
import java.net.URI;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.semanticweb.yars.util.Callbacks;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.http.ConnectionManager;
import com.ontologycentral.ldspider.http.Headers;
import com.ontologycentral.ldspider.queue.FetchQueue;
import com.ontologycentral.ldspider.robot.Robots;

public class LookupThread implements Runnable {
	Logger _log = Logger.getLogger(this.getClass().getSimpleName());

	FetchQueue _q;
	Callbacks _cbs;
	FetchFilter _ff;
	
	Robots _robots;
	ErrorHandler _eh;
	ConnectionManager _hclient;

	public LookupThread(ConnectionManager hc, FetchQueue q, Callbacks cbs, Robots robots, ErrorHandler eh, FetchFilter ff) {
		_hclient = hc;
		_q = q;
		_cbs = cbs;
		_robots = robots;
		_ff = ff;
		_eh = eh;
	}
	
	public void run() {
		_log.info("starting thread ...");

		URI u = _q.poll();

		while (u != null) {
			URI lu = _q.getRedirects().getRedir(u);

			if (u != lu) {
				_log.info("redir from " + u + " to " + lu);
			}

			if (_robots.accessOk(lu)) {
				if (!_q.getSeen(lu)) {
					HttpGet hget = new HttpGet(lu);
					hget.setHeaders(CrawlerConstants.HEADERS);

					try {
						HttpResponse hres = _hclient.connect(hget);

						HttpEntity hen = hres.getEntity();

						int status = hres.getStatusLine().getStatusCode();

						_log.info("lookup on " + lu + " status " + status);

						// write headers in RDF
						Headers h = new Headers(lu, status, hres.getAllHeaders(), _cbs);
						
						if (status == HttpStatus.SC_OK) {
							if (_ff.fetchOk(lu, status, hen)) {
								InputStream is = hen.getContent();

								RDFXMLParser rxp = new RDFXMLParser(is, true, true, lu.toString(), _cbs);
								
								if (hen != null) {
									hen.consumeContent();
								}
							} else {
								_log.info("not allowed " + lu);
								hget.abort();
							}
							_q.setSeen(lu);
						} else if (status == HttpStatus.SC_SEE_OTHER) {
							Header[] loc = hres.getHeaders("location");
							_log.info("redirecting to " + loc[0].getValue());
							URI to = new URI(loc[0].getValue());

							_q.setRedirect(lu, to);

							if (hen != null) {
								hen.consumeContent();
							}
						} else {
							_log.info("status code " + status + " for " + lu);
							hget.abort();
							_q.setSeen(lu);
						}
						
						_eh.handleStatus(lu, status, hen.getContentLength());
					} catch (Exception e) {
						hget.abort();
						_eh.handleError(lu, e);
					}
				} else {
					_log.info("uri " + u + " seen before");
				}
			} else {
				_log.info("access denied per robots.txt for " + u);
			}

			u = _q.poll();
		}
	}
}