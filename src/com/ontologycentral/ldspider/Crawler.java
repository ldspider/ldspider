package com.ontologycentral.ldspider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.Callbacks;

import com.ontologycentral.ldspider.hooks.content.CallbackDummy;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerDummy;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterAllow;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDefault;
import com.ontologycentral.ldspider.http.ConnectionManager;
import com.ontologycentral.ldspider.lookup.LookupThread;
import com.ontologycentral.ldspider.queue.FetchQueue;
import com.ontologycentral.ldspider.queue.UriSrc;
import com.ontologycentral.ldspider.robot.Robots;
import com.ontologycentral.ldspider.tld.TldManager;

public class Crawler {
	Logger _log = Logger.getLogger(this.getClass().getName());

	Callback _output;
	LinkFilter _links;
	ErrorHandler _eh;
	FetchFilter _ff;
	ConnectionManager _cm;
	
	Robots _robots;
	UriSrc _urisrc;
	
	int _threads;
	
	public Crawler() {
		this(CrawlerConstants.DEFAULT_NB_THREADS);
	}
	
	public Crawler(int threads) {
		_urisrc = new UriSrc();

		_threads = threads;
		
		String phost = null;
		int pport = 0;		
		String puser = null;
		String ppassword = null;
		
		if (System.getProperties().get("http.proxyHost") != null) {
			phost = System.getProperties().get("http.proxyHost").toString();
		}
		if (System.getProperties().get("http.proxyPort") != null) {
			pport = Integer.parseInt(System.getProperties().get("http.proxyPort").toString());
		}
		
		if (System.getProperties().get("http.proxyUser") != null) {
			puser = System.getProperties().get("http.proxyUser").toString();
		}
		if (System.getProperties().get("http.proxyPassword") != null) {
			ppassword = System.getProperties().get("http.proxyPassword").toString();
		}
		
	    _cm = new ConnectionManager(phost, pport, puser, ppassword, threads*CrawlerConstants.MAX_CONNECTIONS_PER_THREAD);
	    _cm.setRetries(CrawlerConstants.RETRIES);
	    
	    try {
		    TldManager.init(_cm);
		} catch (URISyntaxException e) {
			_log.info(e.getMessage());
		    e.printStackTrace();
		} catch (IOException e) {
			_log.info(e.getMessage());
			e.printStackTrace();
		}
		
	    _robots = new Robots(_cm);
		
		_output = new CallbackDummy();
		_eh = new ErrorHandlerDummy();
		_links = new LinkFilterDefault(_eh);
		_ff = new FetchFilterAllow();
	}
	
	public void setFetchFilter(FetchFilter ff) {
		_ff = ff;
	}
	
	public void setErrorHandler(ErrorHandler eh) {
		_eh = eh;
	}
	
	public void setOutputCallback(Callback cb) {
		_output = cb;
	}
	
	public void setLinkSelectionCallback(LinkFilter links) {
		_links = links;
	}
	
	public void evaluate(Collection<URI> seeds, int rounds) {
		FetchQueue q = new FetchQueue(TldManager.getInstance());

		for (URI u : seeds) {
			q.put(u);
		}
		
		q.schedule();
		
		for (int curRound = 0 ; curRound <= rounds; curRound++) {
			List<Thread> ts = new ArrayList<Thread>();

			Callbacks cbs = new Callbacks(new Callback[] { _output, _links } );

			for (int j = 0; j < _threads; j++) {
				LookupThread lt = new LookupThread(_cm, q, cbs, _robots, _eh, _ff);
				ts.add(new Thread(lt,"LookupThread-"+j));		
			}

			_log.info("Starting threads round " + curRound + " with " + q.size() + " uris");
			
			for (Thread t : ts) {
				t.start();
			}

			for (Thread t : ts) {
				try {
					t.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

			for (URI u : _links.getLinks()) {
				if (q.getSeen(u) == false) {
					q.put(u);
				}
			}
			q.schedule();
		}
	}
	
	public void close() {
		_cm.shutdown();
	}
}
