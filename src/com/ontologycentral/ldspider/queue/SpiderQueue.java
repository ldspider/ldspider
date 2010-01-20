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

//	public abstract boolean addFrontier(URI u);
	public abstract URI poll();
	public abstract void setRedirect(URI from, URI to, int status);
	public abstract int size();
	
	protected Set<URI> _seenRound = null;
	protected Set<URI> _redirsRound = null;
	
	protected TldManager _tldm;
	
	public SpiderQueue(TldManager tldm) {
		_tldm = tldm;
	}
	
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
	
//	String[] _blacklist = { ".txt", ".html", ".jpg", ".pdf", ".htm", ".png", ".jpeg", ".gif" };
//	
//	protected Frontier _frontier;
//
//	/**
//	 * Add URI to frontier
//	 * 
//	 * @param u
//	 */
//	public boolean addFrontier(URI u) {
//		if (u == null || u.getScheme() == null) {
//			return false;
//		}
//		
//		if (!(u.getScheme().equals("http"))) {
//			_log.info(u.getScheme() + " != http, skipping " + u);
//			return false;
//		}
//		
//		try {
//			u = normalise(u);
//		} catch (URISyntaxException e) {
//			_log.info(u +  " not parsable, skipping " + u);
//			return false;
//		}
//		
//		for (String suffix : _blacklist) {
//			if (u.getPath().endsWith(suffix)) {
//				_log.info("suffix blacklisted");
//				return false;
//			}
//		}
//
//		return true;
//	}
}
