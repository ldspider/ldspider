package com.ontologycentral.ldspider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.hooks.content.CallbackDummy;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerDummy;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterAllow;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDefault;
import com.ontologycentral.ldspider.http.ConnectionManager;
import com.ontologycentral.ldspider.lookup.LookupManager;
import com.ontologycentral.ldspider.queue.FetchQueue;
import com.ontologycentral.ldspider.robot.Robots;
import com.ontologycentral.ldspider.tld.TldManager;

public class Crawler {
	Logger _log = Logger.getLogger(this.getClass().getName());

	LookupManager _lm;
	Callback _output;
	LinkFilter _links;
	ErrorHandler _eh;
	FetchFilter _ff;
	private ConnectionManager _cm;
	
	public Crawler() {
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
		
	    _cm = new ConnectionManager(phost, pport, puser, ppassword, CrawlerConstants.RETRIES);
		
	    try {
		    TldManager.init(_cm);
		} catch (URISyntaxException e) {
			_log.info(e.getMessage());
		    e.printStackTrace();
		} catch (IOException e) {
			_log.info(e.getMessage());
			e.printStackTrace();
		}
		
	    Robots robots = new Robots(_cm);
		
		_lm = new LookupManager(_cm, robots);
		
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
	
	public void evaluate(Collection<URI> seeds, int rounds, int threads) {
		FetchQueue q = new FetchQueue(TldManager.getInstance());

		for (URI u : seeds) {
			q.put(u);
		}
		
		q.schedule();
		
		_lm.fetch(q, rounds, threads, _output, _links, _ff, _eh);
	}
	
	public void close() {
		_cm.shutdown();
	}
}
