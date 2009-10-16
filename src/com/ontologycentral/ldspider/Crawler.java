package com.ontologycentral.ldspider;

import java.net.URI;
import java.util.Collection;

import org.apache.http.client.HttpClient;
import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.hooks.content.CallbackDummy;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerDummy;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterDefault;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDefault;
import com.ontologycentral.ldspider.lookup.LookupManager;
import com.ontologycentral.ldspider.queue.FetchQueue;
import com.ontologycentral.ldspider.robot.Robots;

public class Crawler {
	LookupManager _lm;
	Callback _output;
	LinkFilter _links;
	ErrorHandler _eh;
	FetchFilter _ff;
	
	public Crawler() {
		HttpClient hc = ConnectionManager.getHttpClient();
		Robots robots = new Robots(hc);
		
		_lm = new LookupManager(hc, robots);
		
		_output = new CallbackDummy();
		_eh = new ErrorHandlerDummy();
		_links = new LinkFilterDefault(_eh);
		_ff = new FetchFilterDefault();
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
		FetchQueue q = new FetchQueue(ConnectionManager.getTldManager());

		for (URI u : seeds) {
			q.put(u);
		}
		
		q.schedule();
		
		_lm.fetch(q, rounds, threads, _output, _links, _ff, _eh);
	}
	
	public void close() {
		;
	}
}
