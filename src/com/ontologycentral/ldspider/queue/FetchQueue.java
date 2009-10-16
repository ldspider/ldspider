package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.ontologycentral.ldspider.lookup.Redirects;
import com.ontologycentral.ldspider.tld.TldManager;

public class FetchQueue {
	private static final long serialVersionUID = 1L;

	Logger _log = Logger.getLogger(this.getClass().getName());

	Set<URI> _seen;
	
	Redirects _redirs;
	
//	Map<URI, String> _contype;

	Queue<Queue<URI>> _activeQ;
	Map<String, Queue<URI>> _activeM;
	
	Set<URI> _frontier;

	TldManager _tldm;
	
	public FetchQueue(TldManager tldm) {
		_tldm = tldm;

		_seen = new HashSet<URI>();
		_redirs = new Redirects();
//		_contype = new Hashtable<URI, String>();
		_frontier = new HashSet<URI>();
	}
	
//	public void setContentType(URI u, String ct) {
//		_contype.put(u, ct);
//	}

	public void setRedir(URI from, URI to) {
		_redirs.put(from, to);
		
		// fetch again
		add(from);
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
		
	public void schedule() {	
		_activeM = new HashMap<String, Queue<URI>>();
		_activeQ = new ConcurrentLinkedQueue<Queue<URI>>();

		for (URI u : _frontier) {
			add(u);
		}

		_log.info(_activeM.toString());

		for (Queue<URI> q : _activeM.values()) {
			_activeQ.add(q);
		}
		
		_frontier = new HashSet<URI>();
	}
	
	public int size() {
		int size = 0;
		
		if (_activeM != null) {
			for (Queue<URI> q : _activeM.values()) {
				size += q.size();
			}
		}
		
		return size;
	}
	
	public void put(URI u) {
		if (!(u.getScheme().equals("http"))) {
			_log.info(u.getScheme() + " != http, skipping " + u);
			return;
		}
		
		_frontier.add(u.normalize());
	}
	
	public synchronized URI poll() {
		URI next = null;
		
		Queue<URI> q = _activeQ.poll();
		if (q != null) {
			next = q.poll();
			if (!q.isEmpty()) {
				_activeQ.add(q);
			}
		}
		
		return next;
	}
	
	public synchronized void add(URI u) {
		String pld = _tldm.getPLD(u);
		if (pld != null) {	
			Queue<URI> q = _activeM.get(pld);
			if (q == null) {
				q = new ConcurrentLinkedQueue<URI>();
				_activeM.put(pld, q);
			}
			q.add(u);
		} else {
			_log.info("pld is null " + u);
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		if (_activeM != null) {
			for (Queue<URI> q : _activeM.values()) {
				sb.append(q.toString());
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}
}