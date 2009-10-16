package com.ontologycentral.ldspider.lookup;

import java.io.InputStream;
import java.net.URI;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.semanticweb.yars.util.Callbacks;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.queue.FetchQueue;
import com.ontologycentral.ldspider.robot.Robots;

public class LookupThread implements Runnable {
	Logger _log = Logger.getLogger(this.getClass().getName());

	FetchQueue _q;
	Callbacks _cbs;
	FetchFilter _ff;
	
	Robots _robots;
	ErrorHandler _eh;
	HttpClient _hclient;


	public LookupThread(HttpClient hclient, FetchQueue q, Callbacks cbs, Robots robots, ErrorHandler eh,  FetchFilter ff) {
		_hclient = hclient;
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

					HttpContext hcon = new BasicHttpContext();

					try {
						HttpResponse hres = _hclient.execute(hget, hcon);

						int status = hres.getStatusLine().getStatusCode();

						_eh.handleStatus(lu, status);

						_log.info("lookup on " + lu + " status " + status);

						if (status == 200) {

							HttpEntity hen = hres.getEntity();

							if (_ff.fetchOk(lu, status, hen)) {
								InputStream is = hen.getContent();

								RDFXMLParser rxp = new RDFXMLParser(is, true, true, lu.toString(), _cbs);
								// while(rxp.hasNext()) rxp.next();
								//							    
								//							    for (Node[] nx : cbs.getSet()) {
								//								//do something with the output
								//								//_call.processStatement(nx);
								//								//extract and select links
								//								//this callback is storing all links to a set
								//								linkCallback.processStatement(nx);
								//							    }

								//notify the callbacks that the parsing of the document is done

								//									_log.info("Extracted "+linkCallback.getLinks().size()+" links");		
								//									for (URI l : linkCallback.getLinks()) {
								//										if (_q.getSeen(l) == false) {
								//											_q.put(l);
								//										}
								//									}
							} else {
								_log.info("not allowed " + lu);
								hget.abort();
							}
							_q.setSeen(lu);
					} else if (status == 303) {
						Header[] loc = hres.getHeaders("location");
						_log.info("redirecting to " + loc[0].getValue());
						URI to = new URI(loc[0].getValue());

						_q.setRedir(lu, to);
						lu = to;

							HttpEntity hen = hres.getEntity();
							hen.consumeContent();
						} else {
							hget.abort();
							_q.setSeen(lu);
						}
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