package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.lookup.Redirects;
import com.ontologycentral.ldspider.tld.TldManager;

public class FetchQueue {
	Logger _log = Logger.getLogger(this.getClass().getName());

	TldManager _tldm;

	Set<URI> _seen;
	Set<URI> _frontier;
	
	Redirects _redirs;

	Map<String, Queue<URI>> _queues;
	Iterator<String> _current;
	long _time;
		
	public FetchQueue(TldManager tldm) {
		_tldm = tldm;

		_seen = Collections.synchronizedSet(new HashSet<URI>());
		_redirs = new Redirects();
		_frontier = Collections.synchronizedSet(new HashSet<URI>());
	}
	
	/**
	 * Put URIs from frontier to queue
	 * 
	 * @param maxuris - cut off number of uris per pld
	 */
	public void schedule(int maxuris) {	
		_queues = new HashMap<String, Queue<URI>>();

		for (URI u : _frontier) {
			add(u);
		}

		for (String pld : _queues.keySet()) {
			Queue<URI> q = _queues.get(pld);
			if (q.size() > maxuris) {
				int n = 0;
				Queue<URI> nq = new ConcurrentLinkedQueue<URI>();
				for (URI u: q) {
					nq.add(u);
					n++;
					if (n > maxuris) {
						break;
					}
				}
				q = nq;
				
				_queues.put(pld, q);
			}
		}
		
		_current = _queues.keySet().iterator();
		
		_time = System.currentTimeMillis();
		
		_frontier = new HashSet<URI>();
	}
	
	/**
	 * Add URI to frontier
	 * 
	 * @param u
	 */
	public void addFrontier(URI u) {
		if (u == null || u.getScheme() == null) {
			return;
		}
		
		if (!(u.getScheme().equals("http"))) {
			_log.info(u.getScheme() + " != http, skipping " + u);
			return;
		}
		
		try {
			u = normalise(u);
		} catch (URISyntaxException e) {
			_log.info(u +  " not parsable, skipping " + u);
			return;
		}
		
		_frontier.add(u);
	}

	private URI normalise(URI u) throws URISyntaxException {
		URI norm = new URI(u.getScheme(),
				u.getUserInfo(), u.getHost().toLowerCase(), u.getPort(),
				u.getPath(), u.getQuery(),
				u.getFragment());

		return norm.normalize();
	}
	
	/**
	 * Poll a URI, one PLD after another.
	 * If queue turnaround is smaller than DELAY, wait for DELAY ms to
	 * avoid overloading servers.
	 * 
	 * @return URI
	 */
	public synchronized URI poll() {
		if (_current == null) {
			return null;
		}
		
		URI next = null;

		int empty = 0;
				
		do {	
			if (!_current.hasNext()) {
				long time1 = System.currentTimeMillis();
				
				_log.info("queue turnaround in " + (time1-_time) + " ms");
				if ((time1 - _time) < CrawlerConstants.DELAY) {
					try {
						_log.info("delaying queue...");
						Thread.sleep(CrawlerConstants.DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				_time = time1;
				
				_current = _queues.keySet().iterator();
			}

			String pld = _current.next();
			Queue<URI> q = _queues.get(pld);
			
			if (q != null && !q.isEmpty()) {
				next = q.poll();
			} else {
				empty++;
			}
		} while (next == null && empty < _queues.size());

		return next;
	}
	
	public void setRedirect(URI from, URI to) {
		try {
			to = normalise(to);
		} catch (URISyntaxException e) {
			_log.info(to +  " not parsable, skipping " + to);
			return;
		}
		
		_redirs.put(from, to);
		
		// fetch again, this time redirects are taken into account
		add(from);
	}
	
	/**
	 * Add URI directly to queues.
	 * 
	 * @param u
	 */
	private synchronized void add(URI u) {
		try {
			u = normalise(u);
		} catch (URISyntaxException e) {
			_log.info(u +  " not parsable, skipping " + u);
			return;
		}

		String pld = _tldm.getPLD(u);
		if (pld != null) {	
			Queue<URI> q = _queues.get(pld);
			if (q == null) {
				q = new ConcurrentLinkedQueue<URI>();
				_queues.put(pld, q);
			}
			if (!q.contains(u)) {
				q.add(u);
			}
		} else {
			_log.info("pld is null " + u);
		}
	}

	public Redirects getRedirects() {
		return _redirs;
	}
	
	public boolean getSeen(URI u) {
		if (_seen.contains(u)) {
			return true;
		}
		
		URI to = null;
		while ((to = _redirs.getRedir(u)) != null && (to != u)) {
			if (_seen.contains(to)) {
				return true;
			}
			u = to;
		}
		
		return false;
	}
	
	public void setSeen(URI u) {
		_seen.add(u);
	}
	
	public int size() {
		int size = 0;
		
		for (Queue<URI> q : _queues.values()) {
			size += q.size();
		}
		
		return size;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (String pld : _queues.keySet()) {
			Queue<URI> q = _queues.get(pld);
			sb.append(pld);
			sb.append(": ");
			sb.append(q.size());
			sb.append("\n");
		}
		
		return sb.toString();
	}
}