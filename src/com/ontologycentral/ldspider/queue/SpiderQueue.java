package com.ontologycentral.ldspider.queue;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.ontologycentral.ldspider.frontier.DiskFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.tld.TldManager;



public abstract class SpiderQueue implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final Logger _log = Logger.getLogger(SpiderQueue.class.getName());

	public abstract URI poll();
	public abstract int size();
	
	protected Set<URI> _seen;

	protected Set<URI> _seenRound = null;
	//protected Set<URI> _redirsRound = null;
	
	protected TldManager _tldm;
	protected Redirects _redirs;
	
	public SpiderQueue(TldManager tldm) {
		_tldm = tldm;
		
		_redirs = new Redirects();
		
		_seen = Collections.synchronizedSet(new HashSet<URI>());
	}
	
	/**
	 * Schedule URIs in Frontier (i.e. put URIs in Frontier into the queue for the next round)
	 */
	public void schedule(Frontier f) {
		if (_seenRound != null) {
			if (!(f instanceof DiskFrontier)) {
				f.removeAll(_seenRound);
			}
		}
//		if (_redirsRound != null) {
//			f.addAll(_redirsRound);
//		}
		
		_seenRound = Collections.synchronizedSet(new HashSet<URI>());
//		_redirsRound = Collections.synchronizedSet(new HashSet<URI>());
	}
	
	/**
	 * Set a redirect (303)
	 * @param from
	 * @param to
	 * @param status
	 */
	public void setRedirect(URI from, URI to, int status) {
		try {
			to = Frontier.normalise(to);
		} catch (URISyntaxException e) {
			_log.info(to +  " not parsable, skipping " + to);
			return;
		}
		
		if (from.equals(to)) {
			_log.info("redirected to same uri " + from);
			return;
		}
		
		_redirs.put(from, to);
//		_redirsRound.add(to);
		
		if (checkSeen(to) == false) {
			_log.info("adding " + to + " directly to queue");
			addDirectly(to);
		}
	}
		
	abstract void addDirectly(URI u);

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
	
	public Redirects getRedirects() {
		return _redirs;
	}

	public void setRedirects(Redirects redirs) {
		_redirs = redirs;		
	}

	public void addSeen(URI u) {
		_seen.add(u);
	}
	
	public Set<URI> getSeen() {
		return _seen;
	}
	
	public void setSeen(Set<URI> seen) {
		_seen = seen;
	}
	
	public boolean checkSeen(URI u) {
		if (u == null) {
			throw new NullPointerException("u cannot be null");
		}
		
		return _seen.contains(u);
	}
	
	void setSeen(URI u) {
		if (u != null) {
			_seen.add(u);
			_seenRound.add(u);
		}
	}
}
