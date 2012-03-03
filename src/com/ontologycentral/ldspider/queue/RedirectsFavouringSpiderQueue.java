package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.semanticweb.yars.tld.TldManager;

public abstract class RedirectsFavouringSpiderQueue extends SpiderQueue {

	private static final long serialVersionUID = 4717435149503382210L;
	
	private static final  Logger _log = Logger.getLogger(RedirectsFavouringSpiderQueue.class.getName());
	
	Queue<URI> _redirectsQueue;

	public RedirectsFavouringSpiderQueue(TldManager tldm, Redirects redirs) {
		super(tldm, redirs);
		_redirectsQueue = new ConcurrentLinkedQueue<URI>();
	}

	@Override
	public URI poll() {
		URI u;
		do {
			u = _redirectsQueue.poll();
			if (u != null) {
				if (!checkSeen(u)) {
					_log.fine("polled " + u + " from redirects queue.");
					addSeen(u);
					return u;
				}
			}
		} while (!_redirectsQueue.isEmpty());
		
		return pollInternal();
		
//		if ((u = _redirects.poll()) != null) {
//			_log.fine("polled " + u + " from redirects queue.");
//			return u;
//		} else
//			return pollInternal();
	}

	abstract URI pollInternal();

	@Override
	public abstract void add(URI u, boolean uriHasAlreadyBeenProcessed);

	@Override
	void addRedirect(URI u) {
		if (!checkSeen(u))
			_redirectsQueue.add(u);
	}

	public int size() {
		return _redirectsQueue.size();
	}
	
}
