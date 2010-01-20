package com.ontologycentral.ldspider.queue.memory.ranked;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.queue.Redirects;
import com.ontologycentral.ldspider.queue.SpiderQueue;
import com.ontologycentral.ldspider.tld.TldManager;

public class RankQueue extends SpiderQueue {
	Logger _log = Logger.getLogger(this.getClass().getName());

	TldManager _tldm;

	Set<URI> _seen;
	
	Frontier _frontier;
	Redirects _redirs;

	Map<String, Queue<URI>> _queues;
	Queue<String> _current;

	long _mindelay, _maxdelay;
	long _mintime, _maxtime;
	
	static Queue<String> POISON = new ConcurrentLinkedQueue<String>();
	
	public RankQueue(TldManager tldm) {
		_tldm = tldm;

		_seen = Collections.synchronizedSet(new HashSet<URI>());
		_redirs = new Redirects();
		_frontier = new Frontier();
		
		_current = new ConcurrentLinkedQueue<String>();
		
		_mindelay = CrawlerConstants.MIN_DELAY;
		_maxdelay = CrawlerConstants.MAX_DELAY;
	}
	
	public void setMinDelay(int delay) {
		_mindelay = delay;
	}
	
	public void setMaxDelay(int delay) {
		_maxdelay = delay;
	}
	
	/**
	 * Put URIs from frontier to queue
	 * 
	 * @param maxuris - cut off number of uris per pld
	 */
	public synchronized void schedule() {	
		_log.info("start scheduling...");

		long time = System.currentTimeMillis();

		_queues = Collections.synchronizedMap(new HashMap<String, Queue<URI>>());

		Iterator<URI> it = _frontier.getRanked();
		
		while (it.hasNext()) {
			URI u = it.next();
			if (!checkSeen(u)) {
				addDirectly(u);
			} else {
				_frontier.remove(u);
			}
		}
	
		_current.addAll(getQueuePlds());
		
		_mintime = _maxtime = System.currentTimeMillis();
		
		_log.info("scheduling " + size() + " uris done in " + (_mintime - time) + " ms");
	}
	
	/**
	 * Add URI to frontier
	 * 
	 * @param u
	 */
	public boolean addFrontier(URI u) {
		if (super.addFrontier(u) == true) {
			_frontier.add(u);
			return true;
		}
		
		return false;
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
			long time1 = System.currentTimeMillis();

			if (_current.isEmpty()) {
				// queue is empty, done for this round
				if (size() == 0) {
					_log.info("queue size is 0: " + toString());
					return null;
				}
				if (_current == POISON) {
					return null;
				}
		
				if ((time1 - _mintime) < _mindelay) {
					_log.info("fetching plds too fast, rescheduling, queue size " + size());
					_log.info(toString());
					_current = POISON;
					return null;
				}
				
				_log.info("queue turnaround in " + (time1-_mintime) + " ms");

				_mintime = _maxtime = System.currentTimeMillis();
				
				_current.addAll(getQueuePlds());
			} else if ((time1 - _maxtime) > _maxdelay) {
				_log.info("skipped to start of queue in " + (time1-_maxtime) + " ms, queue size " + size());

				_maxtime = System.currentTimeMillis();
				
				_current.addAll(getQueuePlds());				
			}

			String pld = _current.poll();
			Queue<URI> q = _queues.get(pld);
			
			if (q != null && !q.isEmpty()) {
				next = q.poll();

				setSeen(next);
				_frontier.remove(next);
			} else {
				empty++;
			}
		} while (next == null && empty < _queues.size());
		
		_log.info("polled " + next);

		return next;
	}
	
	/**
	 * Set the redirect.
	 */
	public void setRedirect(URI from, URI to, int status) {
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
		addFrontier(to);
	}
	
	/**
	 * Return redirected URI (if there's a redirect)
	 * otherwise return original URI.
	 * 
	 * @param from
	 * @return
	 */
	URI obtainRedirect(URI from) {
		URI to = _redirs.getRedirect(from);
		if (from != to) {
			_log.info("redir from " + from + " to " + to);
			return to;
		}
		
		return from;
	}
	
	boolean checkSeen(URI u) {
		if (u == null) {
			throw new NullPointerException("u cannot be null");
		}
		
		return _seen.contains(u);
	}
	
	void setSeen(URI u) {
		if (u != null) {
			_seen.add(u);
		}
	}
	
	List<String> getQueuePlds() {
		List<String> li = new ArrayList<String>();
		
		li.addAll(_queues.keySet());
		
		Collections.sort(li, new PldCountComparator(_queues));
		
		return li;
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
		
		for (String pld : getQueuePlds()) {
			Queue<URI> q = _queues.get(pld);
			sb.append(pld);
			sb.append(": ");
			sb.append(q.size());
			sb.append("\n");
		}
		
		return sb.toString();
	}
}

class PldCountComparator implements Comparator<String> {
	Map<String, Queue<URI>> _map;
	
	public PldCountComparator(Map<String, Queue<URI>> map) {
		_map = map;
	}
	
	public int compare(String arg0, String arg1) {
		return _map.get(arg1).size() - _map.get(arg0).size();
	}
}