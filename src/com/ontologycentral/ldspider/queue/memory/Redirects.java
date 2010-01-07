package com.ontologycentral.ldspider.queue.memory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

public class Redirects {
	Logger _log = Logger.getLogger(this.getClass().getName());

	Map<URI, URI> _map;
	
	public Redirects() {
		_map = Collections.synchronizedMap(new Hashtable<URI, URI>());
	}

	public boolean put(URI from, URI to) {
		if (_map.containsKey(from)) {
			_log.info("URI " + from + " already redirects to " + _map.get(from));
			return false;
		}
		
		_map.put(from, to);
		return true;
	}
	
	public URI getRedirect(URI from) {
		if (from.getFragment() != null) {
			try {
				URI to = new URI(from.getScheme(), null, from.getAuthority(), from.getPort(), from.getPath(), from.getQuery(), null);
				return to;
			} catch (URISyntaxException e) {
				_log.info(e.getMessage() + " " + from);
			}			
		}

		URI to = _map.get(from);
		if (to != null) {
			return to;
		}
		
		return from;
	}
}