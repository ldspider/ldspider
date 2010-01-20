package com.ontologycentral.ldspider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.content.CallbackDummy;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerDummy;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterAllow;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDefault;
import com.ontologycentral.ldspider.http.ConnectionManager;
import com.ontologycentral.ldspider.http.LookupThread;
import com.ontologycentral.ldspider.http.robot.Robots;
import com.ontologycentral.ldspider.queue.SpiderQueue;
import com.ontologycentral.ldspider.queue.disk.BDBQueue;
import com.ontologycentral.ldspider.queue.memory.BreadthFirstQueue;
import com.ontologycentral.ldspider.queue.memory.LoadBalancingQueue;
import com.ontologycentral.ldspider.tld.TldManager;

public class Crawler {
	Logger _log = Logger.getLogger(this.getClass().getName());

	Callback _output;
	LinkFilter _links;
	ErrorHandler _eh;
	FetchFilter _ff;
	ConnectionManager _cm;
	
	Robots _robots;
	TldManager _tldm;

	SpiderQueue _queue = null;
	
	int _threads;
	
	public Crawler() {
		this(CrawlerConstants.DEFAULT_NB_THREADS);
	}
	
	public Crawler(int threads) {
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
		    _tldm = new TldManager(_cm);
		} catch (Exception e) {
			_log.info("cannot get tld file online " + e.getMessage());
			try {
				_tldm = new TldManager();
			} catch (IOException e1) {
				_log.info("cannot get tld file locally " + e.getMessage());
			}
		}

		_eh = new ErrorHandlerDummy();

	    _robots = new Robots(_cm);
	    _robots.setErrorHandler(_eh);
		
		_output = new CallbackDummy();
		_ff = new FetchFilterAllow();
	}
	
	public void setFetchFilter(FetchFilter ff) {
		_ff = ff;
	}
	
	public void setErrorHandler(ErrorHandler eh) {
		_eh = eh;
		
		if (_robots != null) {
			_robots.setErrorHandler(eh);
		}
		if (_links != null) {
			_links.setErrorHandler(eh);
		}
	}
	
	public void setOutputCallback(Callback cb) {
		_output = cb;
	}
	
	public void setLinkFilter(LinkFilter links) {
		_links = links;
	}
	
	public void evaluate(Frontier frontier, int depth) {
		if (_queue == null) {
			_queue = new LoadBalancingQueue(_tldm);
		}
		
		if (_links == null) {
			_links = new LinkFilterDefault(frontier);
		}
		
		_queue.schedule(frontier);
		
		for (int curRound = 0; curRound <= depth; curRound++) {
			List<Thread> ts = new ArrayList<Thread>();

			for (int j = 0; j < _threads; j++) {
				LookupThread lt = new LookupThread(_cm, _queue, _output, _links, _robots, _eh, _ff);
				ts.add(new Thread(lt,"LookupThread-"+j));		
			}

			_log.info("Starting threads round " + curRound + " with " + _queue.size() + " uris");
			_log.info(_queue.toString());
			
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

			_queue.schedule(frontier);
		}
	}
	
	/**
	 * Crawl with the default in-mem queue
	 * 
	 * @param seeds
	 * @param rounds
	 * @param maxuris
	 */
	public void evaluate(Frontier frontier, int depth, int maxuris) {
		_queue = new BreadthFirstQueue(_tldm, maxuris);
		
		evaluate(frontier, depth);
	}
	
	/**
	 * 
	 * @param seeds
	 * @param rounds
	 * @param maxuris
	 * @param queuelocation - usage of a on-disk queue if specified, otherwise in-mem queue
	 */
	public void evaluate(Frontier frontier, int depth, int maxuris, String queueLocation) {
		if (_queue == null) {
			try {
				_queue = new BDBQueue(_tldm, queueLocation, maxuris);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				_log.severe(e.getClass().getSimpleName()+" "+e.getMessage());
				return;
			}
		}
		
	    evaluate(frontier, depth);

	    if (_queue instanceof BDBQueue) {
			((BDBQueue)_queue).close();
		}
	}
	
	public void close() {
		_cm.shutdown();
		_eh.close();
	}
}
