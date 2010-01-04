package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.tld.TldManager;

public class FetchQueue {
	Logger _log = Logger.getLogger(this.getClass().getName());

	TldManager _tldm;

	Set<URI> _seen;
	Set<URI> _frontier;
	
	Redirects _redirs;

	Map<String, Queue<URI>> _queues;
	Queue<String> _current;

	long _time;
		
	public FetchQueue(TldManager tldm) {
		_tldm = tldm;

		_seen = Collections.synchronizedSet(new HashSet<URI>());
		_redirs = new Redirects();
		_frontier = Collections.synchronizedSet(new HashSet<URI>());
		
		_current = new ConcurrentLinkedQueue<String>();
	}
	
	/**
	 * Put URIs from frontier to queue
	 * 
	 * @param maxuris - cut off number of uris per pld
	 */
	public synchronized void schedule(int maxuris) {	
		_log.info("start scheduling...");

		long time = System.currentTimeMillis();

		_queues = Collections.synchronizedMap(new HashMap<String, Queue<URI>>());

		for (URI u : _frontier) {
			if (!getSeen(u)) {
				addDirectly(u);
			}
		}

		for (String pld : _queues.keySet()) {
			Queue<URI> q = _queues.get(pld);
			if (q.size() > maxuris) {
				int n = 0;
				Queue<URI> nq = new ConcurrentLinkedQueue<URI>();
				for (URI u: q) {
					nq.add(u);
					n++;
					if (n >= maxuris) {
						break;
					}
				}
				q = nq;
				
				_queues.put(pld, q);
			}
		}
		
		_current.addAll(_queues.keySet());
		
		_time = System.currentTimeMillis();
		
		_frontier = new HashSet<URI>();
		
		_log.info("scheduling done in " + (_time - time) + " ms");
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

	public URI normalise(URI u) throws URISyntaxException {
		String path = u.getPath();
		if (path == null || path.length() == 0) {
			path = "/";
		}
		
		if (u.getHost() == null) {
			throw new URISyntaxException("no host in ", u.toString());
		}
		
		URI norm = new URI(u.getScheme(),
				u.getUserInfo(), u.getHost().toLowerCase(), u.getPort(),
				path, u.getQuery(),
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
			if (_current.isEmpty()) {
				// queue is empty, done for this round
				if (size() == 0) {
					return null;
				}
				
				long time1 = System.currentTimeMillis();
				
				if ((time1 - _time) < CrawlerConstants.DELAY) {
					try {
						_log.info("delaying queue " + CrawlerConstants.DELAY + " ms ...");
						Thread.sleep(CrawlerConstants.DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				_log.info("queue turnaround in " + (time1-_time) + " ms");

				_time = System.currentTimeMillis();
				
				_current.addAll(_queues.keySet());
			}

			String pld = _current.poll();
			Queue<URI> q = _queues.get(pld);
			
			if (q != null && !q.isEmpty()) {
				next = q.poll();
				if (getSeen(next)) {
					next = null;
				} else {
					setSeen(next);
				}
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
		
		if (from.equals(to)) {
			_log.info("redirected to same uri " + from);
			return;
		}
		
		_redirs.put(from, to);
		
		// allow to poll from again from queue
		_seen.remove(from);
		
		// fetch again, this time redirects are taken into account
		addDirectly(from);
	}
	
	/**
	 * Add URI directly to queues.
	 * 
	 * @param u
	 */
	synchronized void addDirectly(URI u) {
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
				_current.add(pld);
			}
			q.add(u);
		}
	}

	public URI handleRedirect(URI from) {
		URI to = _redirs.getRedir(from);
		if (from != to) {
			_log.info("redir from " + from + " to " + to);
			_seen.add(to);
			return to;
		}
		
		return from;
	}
	
	boolean getSeen(URI u) {
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
	
	void setSeen(URI u) {
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