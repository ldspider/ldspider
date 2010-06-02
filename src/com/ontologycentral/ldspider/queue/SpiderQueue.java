package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.tld.TldManager;



public abstract class SpiderQueue {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public abstract URI poll();
	public abstract int size();
	
	protected Set<URI> _seenRound = null;
	protected Set<URI> _redirsRound = null;
	
	protected TldManager _tldm;
	protected Redirects _redirs;
	
	public SpiderQueue(TldManager tldm) {
		_tldm = tldm;
		
		_redirs = new Redirects();
	}
	
	/**
	 * Schedule URIs in Frontier (i.e. put URIs in Frontier into the queue for the next round)
	 */
	public void schedule(Frontier f) {
		if (_seenRound != null) {
			f.removeAll(_seenRound);
		}
		if (_redirsRound != null) {
			f.addAll(_redirsRound);
		}
		
		_seenRound = Collections.synchronizedSet(new HashSet<URI>());
		_redirsRound = Collections.synchronizedSet(new HashSet<URI>());
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
		_redirsRound.add(to);
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
	
	public Redirects getRedirects() {
		return _redirs;
	}

	public void setRedirects(Redirects redirs) {
		_redirs = redirs;		
	}
}
