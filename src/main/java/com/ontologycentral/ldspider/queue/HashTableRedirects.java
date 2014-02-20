package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

public class HashTableRedirects implements Redirects {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger _log = Logger.getLogger(Redirects.class.getName());

	Map<URI, URI> _map;
	
	public HashTableRedirects() {
		_map = Collections.synchronizedMap(new Hashtable<URI, URI>());
	}

	public void put(URI from, URI to) {
		if (_map.containsKey(from)) {
			_log.info("URI " + from + " already redirects to " + _map.get(from));
		}
		
		_map.put(from, to);
	}
	
	public URI getRedirect(URI from) {
		URI to = _map.get(from);
		if (to != null) {
			return to;
		}
		
		return from;
	}
}
