package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.util.logging.Logger;

public class DummyRedirects implements Redirects {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger _log = Logger.getLogger(DummyRedirects.class.getName());

	public void put(URI from, URI to) {
		return;
	}
	
	public URI getRedirect(URI from) {
		_log.info("GetRedirect has been called. Returning the from URI.");
		return from;
	}
}
