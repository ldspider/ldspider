package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
}
