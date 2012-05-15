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

import org.semanticweb.yars.tld.TldManager;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.frontier.DiskFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.frontier.RankedFrontier;
import com.ontologycentral.ldspider.frontier.SortingDiskFrontier;

public class BreadthFirstQueue extends RedirectsFavouringSpiderQueue {
	private static final long serialVersionUID = 1L;

	private static final  Logger _log = Logger.getLogger(BreadthFirstQueue.class.getName());

	Map<String, Queue<URI>> _queues;
	Queue<String> _current;
	
	/**
	 * Point in time of the last schedule or the last queue turnaround.
	 */
	long _time;

	/**
	 * Maxuris means maximum uris per pay-level-domain.
	 */
	int _maxuris;

	/**
	 * Maxplds means keep only the max number of plds with the largest amount of
	 * uris.
	 */
	int _maxplds;

	/**
	 * If there are only _minActPlds left in the queue, the hop should be over.
	 * This is to avoid pld starvation.
	 */
	int _minActPlds;

	/**
	 * Scheduled frontiers should equal hops + 1, i.e. the 0st hop = the initial
	 * seedlist = 1st scheduledFrontier.
	 */
	int _scheduledFrontiers;

	/**
	 * If _minActPlds has been reached and thus we should finish up soon.
	 */
	boolean _minReached;

	/**
	 * If the minActPlds limit should already apply in the downloading of the
	 * seedlist.
	 */
	boolean _minActPldsAlready4Seedlist;

	/**
	 * 
	 * @param tldm
	 * @param maxuris
	 * @param maxplds
	 */
	public BreadthFirstQueue(TldManager tldm, Redirects redirs, int maxuris, int maxplds, int minActPlds, boolean minActPldsAlready4Seedlist) {
		super(tldm, redirs);

		_maxuris = maxuris;
		if (_maxuris == -1) {
			_maxuris = Integer.MAX_VALUE-1;
		}
		
		_maxplds = maxplds;
		if (_maxplds == -1) {
			_maxplds = Integer.MAX_VALUE-1;
		}

		_minActPlds = minActPlds;

		_current = new ConcurrentLinkedQueue<String>();

		_queues = Collections
				.synchronizedMap(new HashMap<String, Queue<URI>>());

		_minReached = false;

		_scheduledFrontiers = 0;
		
		_minActPldsAlready4Seedlist = minActPldsAlready4Seedlist;

	}
	
	/**
	 * Put URIs from frontier to queue
	 * 
	 */
	public synchronized void schedule(Frontier f) {	
		_log.info("start scheduling...");

		_minReached = false;
		
		long time = System.currentTimeMillis();
		
		// commented out because in super, there is actually nothing happening.
		// Thus, I made schedule() abstract there.
		// super.schedule(f);

		_queues.clear();

		Iterator<URI> it = f.iterator();
		while (it.hasNext()) {
			URI u = it.next();
			if (!checkSeen(u)) {
				add(u, true);
			}
//			it.remove();
		}

		if (_minActPlds < 0)
			for (String pld : _queues.keySet()) {
				Queue<URI> q = _queues.get(pld);

				// HACK to avoid hanging at slow servers
				int maxuris = _maxuris;
				for (String s : CrawlerConstants.SITES_SLOW) {
					if (s.equals(pld)) {
						maxuris = maxuris / CrawlerConstants.SLOW_DIV;
					}
				}

				if (q.size() > maxuris) {
					int n = 0;
					ConcurrentLinkedQueue<URI> nq = new ConcurrentLinkedQueue<URI>();
					for (URI u : q) {
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

		List<String> lipld = getQueuePlds(_minActPlds < 0);
		_log.info("sorted pld list (sorted only if maximum for plds or uris has been set) " + lipld.toString());
		
		if (_maxplds < Integer.MAX_VALUE - 1) {
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
		
		// Marks all URIs that have been scheduled as scheduled in the RankedFrontier.
		if (f instanceof RankedFrontier)
			f.reset();
		
		// Marks all URIs that have been scheduled as scheduled in the SortingDiskFrontier.
		if (f instanceof SortingDiskFrontier)
			f.reset();
		
		++_scheduledFrontiers;

		_time = System.currentTimeMillis();

		_log.info("scheduling " + _current.size() + " plds done (" + size()
				+ " URIs) in " + (_time - time) + " ms. This was schedule No. "
				+ _scheduledFrontiers);
		_log.info(toString());
	}
		
	/**
	 * Poll a URI, one PLD after another. If queue turnaround is smaller than
	 * DELAY, wait for DELAY ms to avoid overloading servers. Note that if there
	 * are redirects to be processed, they are already returned by
	 * {@link RedirectsFavouringSpiderQueue}. If {@link #_minActPlds} is used (>-1),
	 * plds with many URIs are not favoured, otherwise they are.
	 * 
	 * @return URI
	 */
	protected synchronized URI pollInternal() {
		if (_current == null) {
			return null;
		}
		
		URI next = null;
		
		long time = System.currentTimeMillis();
		
		int empty = 0;

		long time1 = 0l;

		do {	
			time1 = System.currentTimeMillis();

			// randomly start from the beginning of the queue to spread out lookupt to large sites
			if (_current.isEmpty() || (_minActPlds < 0 && (time1 - _time) > CrawlerConstants.MAX_DELAY))
			{                       // ^^ only consider max delay if minActPLDs is disabled 
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

				List<String> lipld = getQueuePlds(_minActPlds < 0);
				
				_current.addAll(lipld);
				
				if (_minActPlds > -1 && _current.size() < _minActPlds && (_minActPldsAlready4Seedlist || _scheduledFrontiers > 1)) {
					_log.info("The minimum number of active PLDs has been reached. Finishing this round...");
					_minReached = true;
				}
			}

			if (_minReached)
				return null;
			
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
		return getQueuePlds(true);
	}
	
	List<String> getQueuePlds(boolean sorted) {
		List<String> li = new ArrayList<String>();

		for (String pld : _queues.keySet()) {
			if (!_queues.get(pld).isEmpty()) {
				li.add(pld);
			}
		}
		if (sorted)
			Collections.sort(li, new PldCountComparator(_queues));

		return li;
	}
	
	/**
	 * Add URI queue.
	 * 
	 * @param u
	 * @param uriHasAlreadyBeenProcessed
	 *            if the URI has already been frontier.normalise()d or
	 *            frontier.process()ed.
	 */
	public synchronized void add(URI u, boolean uriHasAlreadyBeenProcessed) {
		if (!uriHasAlreadyBeenProcessed)
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
				// _current.add(pld);
			}
			q.add(u);
		}
	}

	public int size() {
		int size = super.size();
		
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
		sb.append("Plus ");
		sb.append(_redirectsQueue.size());
		sb.append(" redirects.\n");
		
		return sb.toString();
	}

}