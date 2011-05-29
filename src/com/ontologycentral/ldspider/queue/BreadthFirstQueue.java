package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.frontier.DiskFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.tld.TldManager;

public class BreadthFirstQueue extends SpiderQueue {
	private static final long serialVersionUID = 1L;

	private static final  Logger _log = Logger.getLogger(BreadthFirstQueue.class.getName());

	Map<String, Queue<URI>> _queues;
	Queue<String> _current;
	
	long _time;
	
	int _maxuris;
	
	int _maxplds;
	
	/**
	 * 
	 * @param tldm
	 * @param maxuris
	 * @param maxplds
	 */
	public BreadthFirstQueue(TldManager tldm, int maxuris, int maxplds) {
		super(tldm);

		_maxuris = maxuris;
		if (_maxuris == -1) {
			_maxuris = Integer.MAX_VALUE-1;
		}
		
		_maxplds = maxplds;
		if (_maxplds == -1) {
			_maxplds = Integer.MAX_VALUE-1;
		}

		_current = new ConcurrentLinkedQueue<String>();
	}

	
	/**
	 * Put URIs from frontier to queue
	 * 
	 * @param maxuris - cut off number of uris per pld
	 */
	public synchronized void schedule(Frontier f) {	
		_log.info("start scheduling...");

		long time = System.currentTimeMillis();
		
		super.schedule(f);

		_queues = Collections.synchronizedMap(new HashMap<String, Queue<URI>>());

		Iterator<URI> it = f.iterator();
		while (it.hasNext()) {
			URI u = it.next();
			if (!checkSeen(u)) {
				addDirectly(u);
			}
			it.remove();
		}

		// maxuris means maximum uris per pay-level-domain
		for (String pld : _queues.keySet()) {
			Queue<URI> q = _queues.get(pld);
			
			// HACK to avoid hanging at slow servers
			int maxuris = _maxuris;
			for (String s : CrawlerConstants.SITES_SLOW) {
				if (s.equals(pld)) {
					maxuris = maxuris/CrawlerConstants.SLOW_DIV;
				}
			}
			
			if (q.size() > maxuris) {
				int n = 0;
				ConcurrentLinkedQueue<URI> nq = new ConcurrentLinkedQueue<URI>();
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

		// maxplds means keep only the max number of plds with the largest amount of uris
		List<String> lipld = getSortedQueuePlds();
		_log.info("sorted pld list " + lipld.toString());
		
		if (_maxplds != -1) {
			for (int i = _maxplds; i < lipld.size(); i++) {
				String pld = lipld.get(i);
				_queues.remove(pld);
				
				_log.fine("removing " + pld);
			}
		}
		
		_current.addAll(_queues.keySet());
		
		//_current.addAll(lipld);
		
		// now just forgets what's happened in the previous round; means that we might
		// starve of URIs but helps the crawler move on
		if (f instanceof DiskFrontier) {
			f.reset();
		}
		_time = System.currentTimeMillis();
		
		_log.info("scheduling " + _current.size() + " plds done in " + (_time - time) + " ms");
		_log.info(toString());
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
		
		long time = System.currentTimeMillis();
		
		URI next = null;

		int empty = 0;

		long time1 = 0l;

		do {	
			time1 = System.currentTimeMillis();

			// randomly start from the beginning of the queue to spread out lookupt to large sites
			if (_current.isEmpty() || (time1 - _time) > CrawlerConstants.MAX_DELAY) {
				// queue is empty, done for this round
				if (size() == 0) {
					return null;
				}
							
				if ((time1 - _time) < CrawlerConstants.MIN_DELAY) {
					try {
						_log.info("delaying queue " + CrawlerConstants.MIN_DELAY + " ms ...");
						Thread.sleep(CrawlerConstants.MIN_DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				_log.info("queue turnaround in " + (time1-_time) + " ms");

				_time = System.currentTimeMillis();

				List<String> lipld = getSortedQueuePlds();
				
				_current.addAll(lipld);
			}

			String pld = _current.poll();
			Queue<URI> q = _queues.get(pld);
			
			if (q != null && !q.isEmpty()) {
				next = q.poll();
				
				if (checkSeen(next)) {
					next = null;
				} else {			
					setSeen(next);
				}
			} else {
				empty++;
			}
		} while (next == null && empty < _queues.size());
		
		time1 = System.currentTimeMillis();
		
		_log.fine("poll for " + next + " done in " + (time1 - time) + " ms");

		return next;
	}
	
	List<String> getSortedQueuePlds() {
		List<String> li = new ArrayList<String>();
		
		for (String pld : _queues.keySet()) {
			if (!_queues.get(pld).isEmpty()) {
				li.add(pld);
			}
		}
				
		Collections.sort(li, new PldCountComparator(_queues));
		
		return li;
	}
	
	/**
	 * Add URI directly to queues.
	 * 
	 * @param u
	 */
	synchronized void addDirectly(URI u) {
		try {
			u = Frontier.normalise(u);
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
				//_current.add(pld);
			}
			q.add(u);
		}
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