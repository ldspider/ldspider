package com.ontologycentral.ldspider.lookup;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.Callbacks;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;
import com.ontologycentral.ldspider.http.ConnectionManager;
import com.ontologycentral.ldspider.queue.FetchQueue;
import com.ontologycentral.ldspider.queue.UriSrc;
import com.ontologycentral.ldspider.robot.Robots;

/**
 * 
 * @author andhar
 *
 */
public class LookupManager {
	Logger _log = Logger.getLogger(this.getClass().getName());
	
	final Robots _robots;
	UriSrc _urisrc;

	final ConnectionManager _hc;

	public LookupManager(final ConnectionManager cm, final Robots robots) {
		_urisrc = new UriSrc();
		
		_hc = cm;
		_robots = robots;
	}
	
//	public HttpClient getHttpClient() {
//		return _hc;
//	}
	
	/**
	 * Fetch the list of URIs in the initial queue
	 * 
	 * This is a breadth first crawl !!
	 * 
	 * @param queue
	 * @param rounds
	 * @param threads
	 * @param call
	 * @param cselect
	 * @return
	 */
	public void fetch(final FetchQueue queue, final int rounds, int threads, Callback content, LinkFilter links, FetchFilter ff, ErrorHandler eh) {
//		if (_linkSelect == null) {
//		    _linkSelect = new CallbackDummy();			
//		}
		//call.startDocument();
//		_linkSelect.startDocument();
		
		for (int curRound = 0 ; curRound <= rounds; curRound++) {
			List<Thread> ts = new ArrayList<Thread>();

			Callbacks cbs = new Callbacks(new Callback[] { content, links } );

			for (int j = 0; j < threads; j++) {
				LookupThread lt = new LookupThread(_hc, queue, cbs, _robots, eh, ff);
				ts.add(new Thread(lt,"LookupThread-"+j));		
			}

			_log.info("Starting threads round " + curRound + " with " + queue.size() + " uris");
		
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
			
			for (URI u : links.getLinks()) {
				if (queue.getSeen(u) == false) {
					queue.put(u);
				}
			}
			queue.schedule();
		}
		
		//call.endDocument();
//		cselect.endDocument();
		
		//return eh;
	}
}